package net.wuxianjie.springrestapi.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class PasswdRequest {

  @NotBlank(message = "旧密码不能为空")
  @Size(max = 100, message = "旧密码最多 100 个字符")
  private String oldPassword;

  @NotBlank(message = "新密码不能为空")
  @Size(max = 100, message = "新密码最多 100 个字符")
  private String newPassword;
}
