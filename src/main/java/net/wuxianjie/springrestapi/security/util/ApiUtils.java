package net.wuxianjie.springrestapi.security.util;

import cn.hutool.json.JSONUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiUtils {

  public static Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
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
    response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
    response.setStatus(httpStatus.value());

    final String json = JSONUtil.toJsonStr(error(message));
    response.getWriter().write(json);
  }
}
