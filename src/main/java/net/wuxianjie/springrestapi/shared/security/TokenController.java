package net.wuxianjie.springrestapi.shared.security;

import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.shared.operationlog.core.Log;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TokenController {

  private final TokenService tokenService;

  /**
   * 获取 Access Token，在私有 API 时需要在 HTTP 请求中携带 Access Token：
   *
   * <pre>{@code
   *   Authorization: Bearer {accessToken}
   * }
   * </pre>
   *
   * @param request <pre>{@code
   * {
   *   "username": "...", // 用户名，必填，长度 <= 100
   *   "password": "..." // 密码，必填，长度 <= 100
   * }
   * }</pre>
   * @return <pre>{@code
   * {
   *   "accessToken": "...", // 要获取的 Access Token
   *   "refreshToken": "...", // 用于刷新的 Refresh Token
   *   "expiresIn": 1800, // Access Token 的有效期（秒为单位，有效期 30 分钟）
   *   "username": "zhangsan", // 用户名
   *   "nickname": "张三", // 昵称
   *   "authorities": ["user"] // 权限列表
   * }
   * }</pre>
   */
  @Log("获取 Access Token")
  @PostMapping("token")
  public ResponseEntity<LinkedHashMap<String, Object>> getToken(@RequestBody @Valid final AuthRequest request) {
    return tokenService.getToken(request);
  }

  /**
   * 刷新 Access Token，刷新后旧 Access Token 将不可用。
   *
   * @param refreshToken {@code /api/v1/token/{refreshToken}}
   * @return <pre>{@code
   * {
   *   "accessToken": "...", // 要获取的 Access Token
   *   "refreshToken": "...", // 用于刷新的 Refresh Token
   *   "expiresIn": 1800, // Access Token 的有效期（秒为单位，有效期 30 分钟）
   *   "username": "zhangsan", // 用户名
   *   "nickname": "张三", // 昵称
   *   "authorities": ["user"] // 权限列表
   * }
   * }</pre>
   */
  @Log("刷新 Access Token")
  @PostMapping("token/{refreshToken}")
  public ResponseEntity<LinkedHashMap<String, Object>> refreshToken(@PathVariable final String refreshToken) {
    return tokenService.refreshToken(refreshToken);
  }
}
