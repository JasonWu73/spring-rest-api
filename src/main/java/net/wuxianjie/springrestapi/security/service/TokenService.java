package net.wuxianjie.springrestapi.security.service;

import cn.hutool.core.exceptions.ValidateException;
import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.security.dto.AuthRequest;
import net.wuxianjie.springrestapi.security.dto.TokenDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TokenService {

  private final AuthenticationManager authManager;
  private final JwtTokenService jwtTokenService;
  private final UserDetailsService userDetailsService;

  public ResponseEntity<Map<String, Object>> getToken(final AuthRequest request) {
    // 通过 Spring Security 身份验证管理器进行身份验证并获取用户身份信息
    final String username = request.getUsername();
    final TokenDetails token = authenticate(username, request.getPassword());

    // 创建并返回 JWT
    return createToken(
      username,
      token.getNickname(),
      AuthorityUtils.authorityListToSet(token.getAuthorities())
    );
  }

  public ResponseEntity<Map<String, Object>> refreshToken(final String refreshToken) {
    // 验证 JWT Token
    try {
      jwtTokenService.validateToken(refreshToken);
    } catch (ValidateException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "非法 Token", e);
    }

    // 解析 JWT Token 并获取用户名和 Token 类型
    final String username = jwtTokenService.getUsername(refreshToken);
    final String type = jwtTokenService.getType(refreshToken);

    // 验证 Token 是否为 Refresh Token
    if (!JwtTokenService.REFRESH_TOKEN_TYPE.equals(type)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "非 Refresh Token");
    }

    // 获取 Token 详细信息
    final TokenDetails token = (TokenDetails) userDetailsService.loadUserByUsername(username);

    // 创建并返回 JWT
    return createToken(
      username,
      token.getNickname(),
      AuthorityUtils.authorityListToSet(token.getAuthorities())
    );
  }

  private TokenDetails authenticate(final String username, final String password) throws ResponseStatusException {
    try {
      final Authentication authentication = authManager.authenticate(
        new UsernamePasswordAuthenticationToken(username, password)
      );

      return (TokenDetails) authentication.getPrincipal();
    } catch (AuthenticationException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    }
  }

  private ResponseEntity<Map<String, Object>> createToken(
    final String username,
    final String nickname,
    final Set<String> authorities
  ) {
    final String accessToken = jwtTokenService.createToken(username, JwtTokenService.ACCESS_TOKEN_TYPE);
    final String refreshToken = jwtTokenService.createToken(username, JwtTokenService.REFRESH_TOKEN_TYPE);

    return ResponseEntity.ok()
      .body(new HashMap<>() {{
        put("accessToken", accessToken);
        put("refreshToken", refreshToken);
        put("expiresIn", JwtTokenService.EXPIRES_IN_SECONDS);
        put("username", username);
        put("nickname", nickname);
        put("authorities", authorities);
      }});
  }
}
