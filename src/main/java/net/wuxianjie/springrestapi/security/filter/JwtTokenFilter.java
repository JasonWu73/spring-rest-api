package net.wuxianjie.springrestapi.security.filter;

import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSignerUtil;
import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.security.config.SecurityProperties;
import net.wuxianjie.springrestapi.security.controller.TokenController;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

  private final SecurityProperties properties;
  private final UserDetailsService service;

  @Override
  protected void doFilterInternal(
    final HttpServletRequest request,
    final HttpServletResponse response,
    final FilterChain filterChain
  ) throws ServletException, IOException {
    // 获取 Authorization HTTP 请求头并验证
    final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (StrUtil.isBlank(header) || !header.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    // 获取 JWT 令牌并验证
    final String token = header.split(" ")[1].trim();
    try {
      JWTValidator
        .of(token)
        .validateAlgorithm(JWTSignerUtil.hs256(properties.getJwtKey().getBytes()))
        .validateDate();
    } catch (ValidateException e) {
      filterChain.doFilter(request, response);
      return;
    }

    // 获取用户身份并将其设置在 Spring Security 上下文中
    final JWT jwt = JWTUtil.parseToken(token);
    final String username = (String) jwt.getPayload("username");
    final String type = (String) jwt.getPayload("type");

    if (!TokenController.ACCESS_TOKEN_TYPE.equals(type)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      final UserDetails userDetails = service.loadUserByUsername(username);

      final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        userDetails,
        null,
        userDetails == null ? List.of() : userDetails.getAuthorities()
      );

      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (UsernameNotFoundException ignore) {
    } finally {
      filterChain.doFilter(request, response);
    }
  }
}
