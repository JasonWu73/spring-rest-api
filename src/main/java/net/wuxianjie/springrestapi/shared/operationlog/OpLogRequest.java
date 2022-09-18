package net.wuxianjie.springrestapi.shared.operationlog;

import cn.hutool.core.date.DatePattern;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class OpLogRequest {

  // 请求时间，包含
  @NotNull(message = "开始时间不能为 null")
  @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
  private LocalDateTime startTime;

  @NotNull(message = "结束时间不能为 null")
  @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
  private LocalDateTime endTime;

  @Size(max = 100, message = "请求方 IP 最多 100 个字符")
  private String requestIp;

  @Size(max = 100, message = "接口端点最多 100 个字符")
  private String endpoint;

  @Size(max = 100, message = "方法信息最多 100 个字符")
  private String message;
}
