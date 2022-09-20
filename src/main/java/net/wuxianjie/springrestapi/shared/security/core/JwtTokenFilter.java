package net.wuxianjie.springrestapi.shared.security.core;

import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.exceptions.ValidateException;
import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.shared.exception.ApiException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

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
    // 获取 Authorization HTTP 请求头并验证
    final String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (bearer == null) {
      filterChain.doFilter(request, response);
      return;
    }
    if (!bearer.startsWith("Bearer ")) {
      final RuntimeException exception = new RuntimeException("Authorization 值错误 [" + bearer + "]");
      final ApiException apiException = new ApiException(HttpStatus.UNAUTHORIZED, "身份验证失败", exception);
      handlerExceptionResolver.resolveException(request, response, null, apiException);
      return;
    }

    // 获取 JWT Token 并验证
    final String token = bearer.split("\\s+")[1].trim();
    try {
      jwtTokenService.validateToken(token);
    } catch (ValidateException e) {
      final ApiException apiException = new ApiException(HttpStatus.UNAUTHORIZED, "身份验证失败", e);
      handlerExceptionResolver.resolveException(request, response, null, apiException);
      return;
    } catch (RuntimeException e) {
      final RuntimeException exception = new RuntimeException(
        "Token 错误 [" + token + "]; nested exception is " + ExceptionUtil.getMessage(e)
      );
      final ApiException apiException = new ApiException(HttpStatus.UNAUTHORIZED, "身份验证失败", exception);
      handlerExceptionResolver.resolveException(request, response, null, apiException);
      return;
    }

    // 解析 JWT Token 并获取用户名和 Token 类型
    final String username = jwtTokenService.getUsername(token);
    final String type = jwtTokenService.getType(token);

    // 验证 Token 是否为 Access Token
    if (!JwtTokenService.ACCESS_TOKEN_TYPE.equals(type)) {
      final RuntimeException exception = new RuntimeException("非 Access Token");
      final ApiException apiException = new ApiException(HttpStatus.UNAUTHORIZED, "身份验证失败", exception);
      handlerExceptionResolver.resolveException(request, response, null, apiException);
      return;
    }

    // 判断 Access Token 是否为当前有效的 Token
    final CachedToken cachedToken = usernameToToken.get(username, false);
    if (cachedToken == null || !cachedToken.getAccessToken().equals(token)) {
      final RuntimeException exception = new RuntimeException("Access Token 已失效");
      final ApiException apiException = new ApiException(HttpStatus.UNAUTHORIZED, "身份验证失败", exception);
      handlerExceptionResolver.resolveException(request, response, null, apiException);
      return;
    }

    // 将通过身份验证后的用户身份设置在 Spring Security 上下文中
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
      final ApiException apiException = new ApiException(HttpStatus.UNAUTHORIZED, "身份验证失败", e);
      handlerExceptionResolver.resolveException(request, response, null, apiException);
    }
  }
}
