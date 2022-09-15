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

  public ApiException(final HttpStatus status, final String reason) {
    super(reason);
    this.status = status;
    this.reason = reason;
  }

  public ApiException(final HttpStatus status, final String reason, final Throwable cause) {
    super(reason, cause);
    this.status = status;
    this.reason = reason;
  }

  @Override
  public String getMessage() {
    final String msg = StrUtil.format("{} \"{}\"", status, reason);
    return NestedExceptionUtils.buildMessage(msg, getCause());
  }
}
