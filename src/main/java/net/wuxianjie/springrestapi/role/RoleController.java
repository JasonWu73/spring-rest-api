package net.wuxianjie.springrestapi.role;

import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.shared.security.core.Authority;
import net.wuxianjie.springrestapi.shared.validation.group.CreateOne;
import net.wuxianjie.springrestapi.shared.validation.group.UpdateOne;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RoleController {

  private final RoleService roleService;

  /**
   * 获取角色列表。
   *
   * @return <pre>{@code
   * [
   *   {
   *     "roleId": 2, // 角色 id
   *     "remark": "角色用途说明", // 备注
   *     "name": "测试账号", // 角色名
   *     "menus": "user_view,role_view", // 菜单
   *     "parentId": 1, // 上级角色 id
   *     "parentName": "管理员" // 上级角色名
   *   }
   * ]
   * }</pre>
   */
  @GetMapping("role")
  @PreAuthorize(Authority.Role.HAS_VIEW)
  public ResponseEntity<List<LinkedHashMap<String, Object>>> getRoles() {
    return roleService.getRoles();
  }

  /**
   * 新增角色。
   *
   * @param request <pre>{@code
   * {
   *   "name": "测试账号", // 角色名，必填，长度 <= 100
   *   "menus": "user_view,role_view", // 菜单，必填，长度 <= 200
   *   "remark": "角色用途说明", // 备注，长度 <= 200
   *   "parentId": 1 // 上级角色 id
   * }
   * }</pre>
   * @return <pre>{@code
   * }</pre>
   */
  @PostMapping("role")
  @PreAuthorize(Authority.Role.HAS_ADD)
  public ResponseEntity<Void> addRole(@RequestBody @Validated(CreateOne.class) final RoleRequest request) {
    return roleService.addRole(request);
  }

  /**
   * 更新角色。
   *
   * @param roleId {@code /api/v1/role/{roleId}}
   * @param request <pre>{@code
   * {
   *   "name": "测试账号", // 角色名，必填，长度 <= 100
   *   "menus": "user_view,role_view", // 菜单，必填，长度 <= 200
   *   "remark": "角色用途说明", // 备注，长度 <= 200
   *   "parentId": 1 // 上级角色 id
   * }
   * }</pre>
   * @return <pre>{@code
   * }</pre>
   */
  @PutMapping("role/{roleId:\\d+}")
  @PreAuthorize(Authority.Role.HAS_EDIT)
  public ResponseEntity<Void> updateRole(
    @PathVariable final int roleId,
    @RequestBody @Validated(UpdateOne.class) final RoleRequest request
  ) {
    request.setRoleId(roleId);
    return roleService.updateRole(request);
  }

  /**
   * 删除角色。
   *
   * @param roleId {@code /api/v1/role/{roleId}}
   * @return <pre>{@code
   * }</pre>
   */
  @DeleteMapping("role/{roleId:\\d+}")
  @PreAuthorize(Authority.Role.HAS_DEL)
  public ResponseEntity<Void> deleteRole(@PathVariable final int roleId) {
    return roleService.deleteRole(roleId);
  }
}
