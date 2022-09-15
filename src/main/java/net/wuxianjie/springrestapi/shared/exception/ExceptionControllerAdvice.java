package net.wuxianjie.springrestapi.shared.exception;

import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.springrestapi.shared.security.util.ApiUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@Slf4j
@ControllerAdvice
public class ExceptionControllerAdvice {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Map<String, Object>> handleApiException(final ApiException e) {
    // 若客户端请求异常，则以 WARN 级别记录异常消息
    final HttpStatus status = e.getStatus();
    if (status.is4xxClientError()) {
      log.warn("{}", e.getMessage());
    } else {
      // 若非客户端请求异常，则以 ERROR 级别记录异常栈
      log.warn("非客户端错误", e);
    }
    return ResponseEntity.status(status)
      .body(ApiUtils.error(status, e.getReason()));
  }
}
