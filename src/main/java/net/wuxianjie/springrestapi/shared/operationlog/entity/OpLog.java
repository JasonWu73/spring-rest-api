package net.wuxianjie.springrestapi.shared.operationlog.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OpLog {

  private Integer id;

  // 请求信息
  private LocalDateTime requestTime;
  private String requestIp;
  private String endPoint;
  private String username;

  // 方法信息
  private String message;
  private String method;
  private String params;
}
