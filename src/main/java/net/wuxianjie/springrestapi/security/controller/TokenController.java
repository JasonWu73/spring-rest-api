package net.wuxianjie.springrestapi.security.controller;

import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.security.config.SecurityProperties;
import net.wuxianjie.springrestapi.security.dto.AuthRequest;
import net.wuxianjie.springrestapi.security.dto.TokenDetails;
import net.wuxianjie.springrestapi.security.util.ApiUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TokenController {

  public static final String ACCESS_TOKEN_TYPE = "access";
  private static final String REFRESH_TOKEN_TYPE = "refresh";
  private static final Integer EXPIRES_IN_SECONDS = 1800;

  private final SecurityProperties properties;
  private final AuthenticationManager authManager;

  /**
   * 获取 Access Token，在私有 API 时需要在 HTTP 请求中携带 Access Token：
   *
   * <pre>{@code
   *   Authorization: Bearer {{accessToken}}
   * }
   * </pre>
   *
   * @return <pre>{@code
   *   {
   *     "accessToken": "...", // 要获取的 Access Token
   *     "refreshToken": "...", // 用于刷新的 Refresh Token
   *     "expiresIn": 1800 // Access Token 的有效期（秒为单位，有效期 30 分钟）
   *   }
   * }</pre>
   */
  @PostMapping("token")
  public ResponseEntity<Map<String, Object>> getToken(@RequestBody @Valid AuthRequest request) {
    try {
      // 通过 Spring Security 身份验证管理器进行身份验证并获取用户身份信息
      final Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(
        request.getUsername(),
        request.getPassword()
      ));

      final TokenDetails token = (TokenDetails) authentication.getPrincipal();

      // 创建 JWT并返回
      final long currentTimeSeconds = System.currentTimeMillis() / 1000;
      final String accessToken = JWTUtil.createToken(
        new HashMap<>() {{
          put("username", token.getUsername());
          put("type", ACCESS_TOKEN_TYPE);
          put(JWTPayload.EXPIRES_AT, currentTimeSeconds + 5);
        }},
        properties.getJwtKey().getBytes()
      );

      final String refreshToken = JWTUtil.createToken(
        new HashMap<>() {{
          put("username", token.getUsername());
          put("type", REFRESH_TOKEN_TYPE);
          put(JWTPayload.EXPIRES_AT, currentTimeSeconds + 5);
        }},
        properties.getJwtKey().getBytes()
      );

      return ResponseEntity.ok()
        .body(new HashMap<>() {{
          put("accessToken", accessToken);
          put("refreshToken", refreshToken);
          put("expiresIn", EXPIRES_IN_SECONDS);
        }});
    } catch (AuthenticationException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiUtils.error(e.getMessage()));
    }
  }
}
