package net.wuxianjie.springrestapi.shared.security.core;

import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.exceptions.ValidateException;
import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.shared.exception.ApiException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <a href="https://stackoverflow.com/questions/62065673/with-spring-security-how-do-i-determine-if-the-current-api-request-should-be-aut">With Spring Security how do i determine if the current api request should be authenticated or not? - Stack Overflow</a>
 */
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

  private static final RequestMatcher ALL_API_REQUEST_MATCHER;
  private static final RequestMatcher PUBLIC_API_REQUEST_MATCHER;

  static {
    // ?????? API
    final String[] requiresAuthenticationEndpoints = {"/api/**"};
    final List<RequestMatcher> apiMatchers = new ArrayList<>();
    for (final String pattern : requiresAuthenticationEndpoints) {
      apiMatchers.add(new AntPathRequestMatcher(pattern, null));
    }
    ALL_API_REQUEST_MATCHER = new OrRequestMatcher(apiMatchers);

    // API ????????????????????????
    final List<RequestMatcher> publicApiMatchers = new ArrayList<>();
    publicApiMatchers.add(new AntPathRequestMatcher("/api/v1/token", HttpMethod.POST.name()));
    publicApiMatchers.add(new AntPathRequestMatcher("/api/v1/token/*", HttpMethod.POST.name()));
    publicApiMatchers.add(new AntPathRequestMatcher("/api/*/public/**", null));
    publicApiMatchers.add(new AntPathRequestMatcher("/vod/**", HttpMethod.GET.name()));
    publicApiMatchers.add(new AntPathRequestMatcher("/dl/**", HttpMethod.GET.name()));
    PUBLIC_API_REQUEST_MATCHER = new OrRequestMatcher(publicApiMatchers);
  }

  private final HandlerExceptionResolver handlerExceptionResolver;
  private final UserDetailsService userDetailsService;
  private final TimedCache<String, CachedToken> usernameToToken;
  private final JwtTokenService jwtTokenService;

  @Override
  protected void doFilterInternal(
    final HttpServletRequest request,
    final HttpServletResponse response,
    final FilterChain filterChain
  ) throws ServletException, IOException {
    // 1. ?????????????????? REST API (????????????), ?????????????????????
    // 2. ??????????????? REST API, ?????? API ????????????, ????????????????????????
    final boolean isApiRequest = ALL_API_REQUEST_MATCHER.matches(request);
    final boolean isPublicApiRequest = PUBLIC_API_REQUEST_MATCHER.matches(request);
    if (!isApiRequest || isPublicApiRequest) {
      filterChain.doFilter(request, response);
      return;
    }

    // ?????? Authorization HTTP ??????????????????
    final String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (bearer == null) {
      filterChain.doFilter(request, response);
      return;
    }
    if (!bearer.startsWith("Bearer ")) {
      final RuntimeException exception = new RuntimeException("Authorization ????????? [" + bearer + "]");
      final ApiException apiException = new ApiException(HttpStatus.UNAUTHORIZED, "??????????????????", exception);
      handlerExceptionResolver.resolveException(request, response, null, apiException);
      return;
    }

    // ?????? JWT Token ?????????
    final String token = bearer.split("\\s+")[1].trim();
    try {
      jwtTokenService.validateToken(token);
    } catch (ValidateException e) {
      final ApiException apiException = new ApiException(HttpStatus.UNAUTHORIZED, "??????????????????", e);
      handlerExceptionResolver.resolveException(request, response, null, apiException);
      return;
    } catch (RuntimeException e) {
      final RuntimeException exception = new RuntimeException(
        "Token ?????? [" + token + "]; nested exception is " + ExceptionUtil.getMessage(e)
      );
      final ApiException apiException = new ApiException(HttpStatus.UNAUTHORIZED, "??????????????????", exception);
      handlerExceptionResolver.resolveException(request, response, null, apiException);
      return;
    }

    // ?????? JWT Token ????????????????????? Token ??????
    final String username = jwtTokenService.getUsername(token);
    final String type = jwtTokenService.getType(token);

    // ?????? Token ????????? Access Token
    if (!JwtTokenService.ACCESS_TOKEN_TYPE.equals(type)) {
      final RuntimeException exception = new RuntimeException("??? Access Token");
      final ApiException apiException = new ApiException(HttpStatus.UNAUTHORIZED, "??????????????????", exception);
      handlerExceptionResolver.resolveException(request, response, null, apiException);
      return;
    }

    // ?????? Access Token ???????????????????????? Token
    final CachedToken cachedToken = usernameToToken.get(username, false);
    if (cachedToken == null || !cachedToken.getAccessToken().equals(token)) {
      final RuntimeException exception = new RuntimeException("Access Token ?????????");
      final ApiException apiException = new ApiException(HttpStatus.UNAUTHORIZED, "??????????????????", exception);
      handlerExceptionResolver.resolveException(request, response, null, apiException);
      return;
    }

    // ???????????????????????????????????????????????? Spring Security ????????????
    try {
      final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        userDetails,
        null,
        userDetails == null ? List.of() : userDetails.getAuthorities()
      );

      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

      SecurityContextHolder.getContext().setAuthentication(authentication);
      filterChain.doFilter(request, response);
    } catch (UsernameNotFoundException e) {
      final ApiException apiException = new ApiException(HttpStatus.UNAUTHORIZED, "??????????????????", e);
      handlerExceptionResolver.resolveException(request, response, null, apiException);
    }
  }
}
