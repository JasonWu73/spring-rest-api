package net.wuxianjie.springrestapi.role;

import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.shared.security.core.Authority;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
  @PreAuthorize(Authority.RoleManagement.HAS_VIEW)
  public ResponseEntity<List<LinkedHashMap<String, Object>>> getRoles() {
    return roleService.getRoles();
  }
}
