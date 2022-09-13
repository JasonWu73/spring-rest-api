package net.wuxianjie.springrestapi.security.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class AuthRequest {

  @NotBlank(message = "用户名不能为空")
  @Size(max = 100, message = "用户名长度不能大于 100 个字符")
  private String username;

  @NotBlank(message = "密码不能为空")
  @Size(max = 100, message = "密码长度不能大于 100 个字符")
  private String password;
}
