package net.wuxianjie.springrestapi.shared.exception;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.springrestapi.shared.security.core.TokenDetails;
import net.wuxianjie.springrestapi.shared.util.ApiUtils;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ExceptionControllerAdvice {

  /**
   * 处理因客户端主动中止连接而导致的异常.
   */
  @ExceptionHandler(ClientAbortException.class)
  public void handleException(ClientAbortException e) {
    final ApiException apiException = new ApiException(
      HttpStatus.BAD_REQUEST,
      "客户端主动中止了连接",
      e
    );
    logMsg(apiException);
  }

  /**
   * 处理因无法处理请求提交的媒体类型而导致的异常.
   */
  @ExceptionHandler(HttpMediaTypeException.class)
  public ResponseEntity<LinkedHashMap<String, Object>> handleException(final HttpMediaTypeException e) {
    final ApiException apiException = new ApiException(
      HttpStatus.NOT_ACCEPTABLE,
      "不支持的媒体类型",
      e
    );
    return handleApiException(apiException);
  }

  /**
   * 处理因不支持请求方法而导致的异常.
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<LinkedHashMap<String, Object>> handleException(
    final HttpRequestMethodNotSupportedException e,
    final HttpServletRequest request
  ) {
    final ApiException apiException = new ApiException(
      HttpStatus.METHOD_NOT_ALLOWED,
      StrUtil.format("不支持 {} 请求", request.getMethod()),
      e
    );
    return handleApiException(apiException);
  }

  /**
   * 处理因无法解析请求体内容而导致的异常.
   */
  @ExceptionHandler(HttpMessageConversionException.class)
  public ResponseEntity<LinkedHashMap<String, Object>> handleException(final HttpMessageConversionException e) {
    final ApiException apiException = new ApiException(
      HttpStatus.BAD_REQUEST,
      "请求体内容不合法",
      e
    );
    return handleApiException(apiException);
  }

  /**
   * 处理因请求缺少必填参数而导致的异常.
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<LinkedHashMap<String, Object>> handleException(final MissingServletRequestParameterException e) {
    final ApiException apiException = new ApiException(
      HttpStatus.BAD_REQUEST,
      StrUtil.format("缺少必填参数 [{}]", e.getParameterName()),
      e
    );
    return handleApiException(apiException);
  }

  /**
   * 处理因请求参数 (方法参数) 校验不通过而导致的异常.
   *
   * <p>校验方法参数必需在 Controller 类上打上 {@code @Validated} 注解.
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<LinkedHashMap<String, Object>> handleException(final ConstraintViolationException e) {
    final List<String> msgList = new ArrayList<>();
    Optional.ofNullable(e.getConstraintViolations())
      .ifPresent(violations -> violations.forEach(v -> msgList.add(v.getMessage())));
    final ApiException apiException = new ApiException(
      HttpStatus.BAD_REQUEST,
      String.join("; ", msgList),
      e
    );
    return handleApiException(apiException);
  }

  /**
   * 处理因请求参数 (对象参数) 校验不通过而导致的异常.
   */
  @ExceptionHandler(BindException.class)
  public ResponseEntity<LinkedHashMap<String, Object>> handleException(final BindException e) {
    final List<String> msgList = new ArrayList<>();
    e.getBindingResult().getFieldErrors()
      .forEach(fieldError -> {
        // 当 Controller 接收的参数类型不符合要求时, 只需提示参数有误即可, 而不是返回服务异常
        final boolean isControllerArgTypeError = fieldError.contains(TypeMismatchException.class);
        final String msg = isControllerArgTypeError
          ? StrUtil.format("参数类型不匹配 [{}] ", fieldError.getField())
          : fieldError.getDefaultMessage();
        msgList.add(msg);
      });
    final ApiException apiException = new ApiException(
      HttpStatus.BAD_REQUEST,
      String.join("; ", msgList),
      e
    );
    return handleApiException(apiException);
  }

  /**
   * 处理所有未被特定 {@code handleException(...)} 方法捕获的异常.
   */
  @ExceptionHandler(Throwable.class)
  public ResponseEntity<LinkedHashMap<String, Object>> handleException(final Throwable e) {
    // 不要处理 AccessDeniedException, 否则会导致 Spring Security 无法处理 403
    final boolean isSpringSecurity403Exception = e instanceof AccessDeniedException;
    if (isSpringSecurity403Exception) throw (AccessDeniedException) e;

    final ApiException apiException = new ApiException(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "服务异常",
      e,
      true
    );
    return handleApiException(apiException);
  }

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
    final String clientInfo = getClientInfo();
    if (status.is4xxClientError() && logStack) {
      log.warn("{} -> {}", clientInfo, e.getMessage(), e);
      return;
    }
    if (status.is4xxClientError()) {
      log.warn("{} -> {}", clientInfo, e.getMessage());
      return;
    }

    // 若非客户端请求异常, 则以 ERROR 级别记录异常栈
    if (logStack) {
      log.error("{} -> {}", clientInfo, e.getMessage(), e);
      return;
    }
    log.error("{} -> {}", clientInfo, e.getMessage());
  }

  private String getClientInfo() {
    final HttpServletRequest request = ApiUtils.getHttpServletRequest().orElseThrow();
    String username = ApiUtils.getAuthentication().map(TokenDetails::getUsername).orElse(null);
    return StrUtil.format(
      "api=[{} {}];client={};user={}",
      request.getMethod(),
      request.getRequestURI(),
      ServletUtil.getClientIP(request),
      username
    );
  }
}
