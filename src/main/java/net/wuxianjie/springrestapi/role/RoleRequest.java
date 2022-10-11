package net.wuxianjie.springrestapi.role;

import lombok.Data;
import net.wuxianjie.springrestapi.shared.validation.group.CreateOne;
import net.wuxianjie.springrestapi.shared.validation.group.UpdateOne;

import javax.validation.constraints.*;
import java.util.List;

@Data
public class RoleRequest {

  @Size(max = 200, message = "备注最多 200 个字符")
  private String remark;

  @NotBlank(message = "角色名不能为空", groups = {CreateOne.class, UpdateOne.class})
  @Size(max = 100, message = "角色名最多 100 个字符")
  private String name;

  @NotEmpty(message = "菜单列表不能为空", groups = {CreateOne.class, UpdateOne.class})
  private List<String> menus;

  @NotNull(message = "上级角色 id 不能为 null", groups = {CreateOne.class, UpdateOne.class})
  @Min(value = 1, message = "上级角色 id 不能小于 1")
  private Long parentId;

  @Min(value = 1, message = "角色 id 不能小于 1")
  private Long roleId;
}
