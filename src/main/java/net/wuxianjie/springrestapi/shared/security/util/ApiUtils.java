package net.wuxianjie.springrestapi.shared.security.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.wuxianjie.springrestapi.shared.security.dto.TokenDetails;
import net.wuxianjie.springrestapi.shared.util.NetUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
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

  public static Map<String, Object> error(final HttpStatus status, final String errorMessage) {
    return new LinkedHashMap<>() {{
      put("timestamp", LocalDateTime.now());
      put("status", status.value());
      put("error", errorMessage);
      put("path", NetUtils.getHttpServletRequest().map(HttpServletRequest::getRequestURI).orElse(null));
    }};
  }

  public static void sendResponse(
    final HttpServletResponse response,
    final HttpStatus status,
    final String message,
    final ObjectMapper objectMapper
  ) throws IOException {
    // 此外必须要设置 JSON 响应体的字符编码
    // 默认使用 Tomcat 的默认字符编码（ISO-8859-1），这会导致响应体中文字符乱码
    response.setContentType("application/json;charset=UTF-8");
    response.setStatus(status.value());

    final String json = objectMapper.writeValueAsString(error(status, message));
    response.getWriter().write(json);
  }
}
