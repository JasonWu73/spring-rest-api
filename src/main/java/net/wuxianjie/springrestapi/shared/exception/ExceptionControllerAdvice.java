package net.wuxianjie.springrestapi.shared.exception;

import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.springrestapi.shared.util.ApiUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.LinkedHashMap;

@Slf4j
@ControllerAdvice
public class ExceptionControllerAdvice {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<LinkedHashMap<String, Object>> handleApiException(final ApiException e) {
    logMsg(e);
    final HttpStatus status = e.getStatus();
    return ResponseEntity.status(status)
      .body(ApiUtils.error(status, e.getReason()));
  }

  private void logMsg(final ApiException e) {
    // 若客户端请求异常, 则以 WARN 级别记录异常消息
    final HttpStatus status = e.getStatus();
    final boolean logStack = e.isLogStack();
    if (status.is4xxClientError() && logStack) {
      log.warn("{}", e.getMessage(), e);
      return;
    }
    if (status.is4xxClientError()) {
      log.warn("{}", e.getMessage());
      return;
    }

    // 若非客户端请求异常, 则以 ERROR 级别记录异常栈
    if (logStack) {
      log.error("{}", e.getMessage(), e);
      return;
    }
    log.error("{}", e.getMessage());
  }
}
