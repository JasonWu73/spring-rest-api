package net.wuxianjie.springrestapi.user;

import lombok.Data;
import net.wuxianjie.springrestapi.shared.validation.group.UpdateOne;
import net.wuxianjie.springrestapi.shared.validation.group.UpdateTwo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class SelfRequest {

  @NotBlank(message = "旧密码不能为空", groups = UpdateOne.class)
  @Size(max = 100, message = "旧密码最多 100 个字符")
  private String oldPassword;

  @NotBlank(message = "新密码不能为空", groups = UpdateOne.class)
  @Size(max = 100, message = "新密码最多 100 个字符")
  private String newPassword;

  @NotBlank(message = "昵称不能为空", groups = UpdateTwo.class)
  @Size(max = 100, message = "昵称最多 100 个字符")
  private String nickname;
}
