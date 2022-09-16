package net.wuxianjie.springrestapi.shared.operationlog;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OpLog {

  private Integer id;

  // 请求信息
  private LocalDateTime requestTime; // 请求时间
  private String requestIp; // 请求方 IP
  private String endpoint; // 接口端点
  private String username; // 用户名

  // 方法信息
  private String message; // 方法信息
  private String method; // 方法名
  private String params; // 方法参数（JSON 字符串）
}
