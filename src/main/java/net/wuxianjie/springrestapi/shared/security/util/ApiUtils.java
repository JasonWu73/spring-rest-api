package net.wuxianjie.springrestapi.shared.security.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.wuxianjie.springrestapi.shared.security.dto.TokenDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
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
      put("path", getHttpServletRequest().map(HttpServletRequest::getRequestURI).orElse(null));
    }};
  }

  public static Optional<HttpServletRequest> getHttpServletRequest() {
    return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
      .map(attr -> (HttpServletRequest) attr.resolveReference(RequestAttributes.REFERENCE_REQUEST));
  }
}
