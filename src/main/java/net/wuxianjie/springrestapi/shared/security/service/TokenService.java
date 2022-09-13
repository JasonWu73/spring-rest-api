package net.wuxianjie.springrestapi.shared.security.service;

import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.exceptions.ValidateException;
import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.shared.security.dto.AuthRequest;
import net.wuxianjie.springrestapi.shared.security.dto.CachedToken;
import net.wuxianjie.springrestapi.shared.security.dto.TokenDetails;
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
  private final UserDetailsService userDetailsService;
  private final TimedCache<String, CachedToken> usernameToToken;
  private final JwtTokenService jwtTokenService;

  public ResponseEntity<Map<String, Object>> getToken(final AuthRequest request) {
    // 通过 Spring Security 身份验证管理器进行身份验证并获取用户身份信息
    final String username = request.getUsername();
    final TokenDetails tokenDetails = authenticate(username, request.getPassword());

    // 创建 JWT
    final Map<String, Object> tokenResult = createToken(
      username,
      tokenDetails.getNickname(),
      AuthorityUtils.authorityListToSet(tokenDetails.getAuthorities())
    );

    // 加入缓存
    addTokenCache(username, tokenResult);

    return ResponseEntity.ok()
      .body(tokenResult);
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

    // 判断 Refresh Token 是否为当前有效的 Token
    final CachedToken cachedToken = usernameToToken.get(username, false);
    if (cachedToken == null || !cachedToken.getRefreshToken().equals(refreshToken)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "无效 Refresh Token");
    }

    // 获取 Token 详细信息
    final TokenDetails tokenDetails = (TokenDetails) userDetailsService.loadUserByUsername(username);

    // 创建 JWT
    final Map<String, Object> tokenResult = createToken(
      username,
      tokenDetails.getNickname(),
      AuthorityUtils.authorityListToSet(tokenDetails.getAuthorities())
    );

    // 加入缓存
    addTokenCache(username, tokenResult);

    return ResponseEntity.ok()
      .body(tokenResult);
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


  private Map<String, Object> createToken(
    final String username,
    final String nickname,
    final Set<String> authorities
  ) {
    final String accessToken = jwtTokenService.createToken(username, JwtTokenService.ACCESS_TOKEN_TYPE);
    final String refreshToken = jwtTokenService.createToken(username, JwtTokenService.REFRESH_TOKEN_TYPE);

    return new HashMap<>() {{
      put("accessToken", accessToken);
      put("refreshToken", refreshToken);
      put("expiresIn", JwtTokenService.EXPIRES_IN_SECONDS);
      put("username", username);
      put("nickname", nickname);
      put("authorities", authorities);
    }};
  }

  private void addTokenCache(final String username, final Map<String, Object> tokenResult) {
    final CachedToken cachedToken = new CachedToken();
    cachedToken.setAccessToken((String) tokenResult.get("accessToken"));
    cachedToken.setRefreshToken((String) tokenResult.get("refreshToken"));
    usernameToToken.put(username, cachedToken);
  }
}
