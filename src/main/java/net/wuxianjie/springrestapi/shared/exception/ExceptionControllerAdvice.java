package net.wuxianjie.springrestapi.shared.exception;

import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.springrestapi.shared.security.util.ApiUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@ControllerAdvice
public class ExceptionControllerAdvice {

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, Object>> handleException(final ResponseStatusException e) {
    log.warn("{}", e.getMessage());
    return ResponseEntity.status(e.getStatus())
      .body(ApiUtils.error(e.getStatus(), e.getReason()));
  }
}
