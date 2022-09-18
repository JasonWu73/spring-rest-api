package net.wuxianjie.springrestapi.role;

import lombok.Data;
import net.wuxianjie.springrestapi.shared.validation.group.CreateOne;
import net.wuxianjie.springrestapi.shared.validation.group.UpdateOne;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class RoleRequest {

  @Size(max = 200, message = "备注最多 200 个字符")
  private String remark;

  @NotBlank(message = "角色名不能为空", groups = {CreateOne.class, UpdateOne.class})
  @Size(max = 100, message = "角色名最多 100 个字符")
  private String name;

  @NotBlank(message = "菜单不能为空", groups = {CreateOne.class, UpdateOne.class})
  @Size(max = 200, message = "菜单最多 200 个字符")
  private String menus;

  private Integer parentId;

  private Integer roleId;
}
