package net.wuxianjie.springrestapi.shared.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.wuxianjie.springrestapi.shared.security.core.TokenDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiUtils {

  public static Optional<TokenDetails> getAuthentication() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
      // 匿名用户, 即无需身份验证的 API
      return Optional.empty();
    }
    return Optional.of((TokenDetails) authentication.getPrincipal());
  }

  public static LinkedHashMap<String, Object> error(final HttpStatus status, final String msg) {
    return new LinkedHashMap<>() {{
      put("timestamp", LocalDateTime.now());
      put("status", status.value());
      put("error", msg);
      put("path", ServletUtils.getHttpServletRequest().map(HttpServletRequest::getRequestURI).orElse(null));
    }};
  }
}
