package net.wuxianjie.springrestapi.shared.operationlog.dto;

import cn.hutool.core.date.DatePattern;
import lombok.Data;
import net.wuxianjie.springrestapi.shared.validation.group.ReadOne;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class OpLogRequest {

  @Size(max = 100, message = "请求方 IP 长度不能大于 100 个字符", groups = ReadOne.class)
  private String requestIp;

  @Size(max = 100, message = "接口端点长度不能大于 100 个字符", groups = ReadOne.class)
  private String endpoint;

  @Size(max = 100, message = "方法信息长度不能大于 100 个字符", groups = ReadOne.class)
  private String message;

  // 请求时间，包含
  @NotNull(message = "开始时间不能为 null", groups = ReadOne.class)
  @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
  private LocalDateTime startTime;

  @NotNull(message = "结束时间不能为 null", groups = ReadOne.class)
  @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
  private LocalDateTime endTime;
}
