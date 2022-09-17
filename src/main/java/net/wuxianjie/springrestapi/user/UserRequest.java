package net.wuxianjie.springrestapi.user;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class UserRequest {

  @Size(max = 100, message = "用户名长度不能大于 100 个字符")
  private String username;

  @Size(max = 100, message = "用户昵称长度不能大于 100 个字符")
  private String nickname;

  private Boolean enabled;
}
