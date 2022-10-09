package net.wuxianjie.springrestapi.shared.exception;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

  @Getter
  private final HttpStatus status;

  @Getter
  private final String reason;

  @Getter
  private final boolean logStack;

  public ApiException(final HttpStatus status, final String reason, final boolean logStack) {
    super(reason);
    this.status = status;
    this.reason = reason;
    this.logStack = logStack;
  }

  public ApiException(final HttpStatus status, final String reason) {
    this(status, reason, false);
  }

  public ApiException(final HttpStatus status, final String reason, final Throwable cause, final boolean logStack) {
    super(reason, cause);
    this.status = status;
    this.reason = reason;
    this.logStack = logStack;
  }

  public ApiException(final HttpStatus status, final String reason, final Throwable cause) {
    this(status, reason, cause, false);
  }

  @Override
  public String getMessage() {
    final String msg = StrUtil.format("{} \"{}\"", status, reason);
    return NestedExceptionUtils.buildMessage(msg, getCause());
  }
}
