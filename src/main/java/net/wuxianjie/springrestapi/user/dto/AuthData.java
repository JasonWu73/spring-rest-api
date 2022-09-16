package net.wuxianjie.springrestapi.user.dto;

import lombok.Data;

@Data
public class AuthData {

  // Spring Security 框架
  private String username;
  private String hashedPassword;
  private String menus;
  private Boolean enabled;

  // 业务扩展
  private Integer userId;
  private String nickname;
}
