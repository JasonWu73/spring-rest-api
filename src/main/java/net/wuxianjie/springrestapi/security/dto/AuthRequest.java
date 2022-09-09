package net.wuxianjie.springrestapi.security.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class AuthRequest {

  @NotBlank(message = "用户名不能为空")
  @Size(max = 100, message = "用户名需在 100 个字符之内")
  private String username;

  @NotBlank(message = "密码不能为空")
  @Size(max = 100, message = "密码需在 100 个字符之内")
  private String password;
}
