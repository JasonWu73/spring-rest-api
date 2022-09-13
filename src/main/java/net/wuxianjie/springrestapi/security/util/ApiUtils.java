package net.wuxianjie.springrestapi.security.util;

import cn.hutool.json.JSONUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.wuxianjie.springrestapi.security.dto.TokenDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiUtils {

  public static Optional<TokenDetails> getAuthentication() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof AnonymousAuthenticationToken) {
      // 匿名用户，即无需身份验证的 API
      return Optional.empty();
    }

    return Optional.of((TokenDetails) authentication.getPrincipal());
  }

  public static Map<String, Object> error(final String errorMessage) {
    return new HashMap<>() {{
      put("error", errorMessage);
    }};
  }

  public static void sendResponse(
    final HttpServletResponse response,
    final HttpStatus httpStatus,
    final String message
  ) throws IOException {
    // 此外必须要设置 JSON 响应体的字符编码
    // 默认使用 Tomcat 的默认字符编码（ISO-8859-1），这会导致响应体中文字符乱码
    response.setContentType("application/json;charset=UTF-8");
    response.setStatus(httpStatus.value());

    final String json = JSONUtil.toJsonStr(error(message));
    response.getWriter().write(json);
  }
}
