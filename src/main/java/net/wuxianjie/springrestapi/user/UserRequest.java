package net.wuxianjie.springrestapi.user;

import lombok.Data;
import net.wuxianjie.springrestapi.shared.validation.group.CreateOne;
import net.wuxianjie.springrestapi.shared.validation.group.UpdateOne;
import net.wuxianjie.springrestapi.shared.validation.group.UpdateTwo;

import javax.validation.constraints.*;

@Data
public class UserRequest {

  @Pattern(
    regexp = "(^\\s*$|^[\\u4E00-\\u9FA5A-Za-z][\\u4E00-\\u9FA5A-Za-z\\d_]+$)",
    message = "用户名只能包含中文, 英文, 数字或_，且必须以中文或英文开头",
    groups = CreateOne.class
  )
  @NotBlank(message = "用户名不能为空", groups = CreateOne.class)
  @Size(max = 100, message = "用户名最多 100 个字符")
  private String username;

  @Size(max = 100, message = "用户昵称最多 100 个字符")
  private String nickname;

  @NotBlank(message = "密码不能为空", groups = {CreateOne.class, UpdateTwo.class})
  @Size(max = 100, message = "密码最多 100 个字符")
  private String password;

  @NotNull(message = "是否启用不能为 null", groups = {CreateOne.class, UpdateOne.class})
  private Boolean enabled;

  @Size(max = 200, message = "备注最多 200 个字符")
  private String remark;

  @NotNull(message = "角色 id 不能为 null", groups = {CreateOne.class, UpdateOne.class})
  @Min(value = 1, message = "角色 id 不能小于 1")
  private Long roleId;

  @Min(value = 1, message = "用户 id 不能小于 1")
  private Long userId;
}
