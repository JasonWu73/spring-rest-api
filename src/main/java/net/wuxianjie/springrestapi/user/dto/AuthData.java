package net.wuxianjie.springrestapi.user.dto;

import lombok.Data;

@Data
public class AuthData {

  private String username;
  private String hashedPassword;
  private String menus;
  private Boolean enabled;

  private Integer userId;
  private String nickname;
}
