package net.wuxianjie.springrestapi.role;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.shared.exception.ApiException;
import net.wuxianjie.springrestapi.shared.security.core.TokenDetails;
import net.wuxianjie.springrestapi.shared.util.ApiUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

  private final RoleMapper roleMapper;

  public ResponseEntity<List<LinkedHashMap<String, Object>>> getRoles() {
    // 获取当前用户角色的所有下级角色
    final TokenDetails token = ApiUtils.getAuthentication().orElseThrow();
    final Role currentUserRole = roleMapper.selectById(token.getRoleId());
    final String fullPath = currentUserRole.getFullPath();
    if (fullPath == null) {
      return ResponseEntity.ok(roleMapper.selectAll());
    }
    return ResponseEntity.ok(roleMapper.selectByFullPathOrLike(fullPath, fullPath + ".%"));
  }

  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<Void> addRole(final RoleRequest request) {
    // 角色名唯一性校验
    final String name = request.getName();
    final boolean nameExisted = roleMapper.selectExitsByName(name);
    if (nameExisted) {
      throw new ApiException(HttpStatus.CONFLICT, "已存在相同角色名");
    }

    // 保存数据库，获取保存后的 id
    final Role role = new Role();
    role.setCreatedAt(LocalDateTime.now());
    role.setUpdatedAt(LocalDateTime.now());
    role.setRemark(request.getRemark());
    role.setName(name);
    role.setMenus(request.getMenus());
    roleMapper.insert(role);

    // 获取上级角色信息，并构造全路径
    final Integer roleId = role.getId();
    String fullPath = roleId + "";
    final Integer parentId = request.getParentId();
    String parentName = null;
    if (parentId != null) {
      final Role parentRole = roleMapper.selectById(parentId);
      if (parentRole == null) {
        throw new ApiException(HttpStatus.NOT_FOUND, "上级角色不存在");
      }
      parentName = parentRole.getName();
      final String parentRoleFullPath = parentRole.getFullPath();
      fullPath = parentRoleFullPath + "." + roleId;

      // 用户仅可创建自身或下级角色
      final TokenDetails token = ApiUtils.getAuthentication().orElseThrow();
      final Role currentUserRole = roleMapper.selectById(token.getRoleId());
      final String currentUserRoleFullPath = currentUserRole.getFullPath() == null ? "" : currentUserRole.getFullPath();
      final boolean startWithCurrentUserRoleFullPath = StrUtil.startWith(parentRoleFullPath, currentUserRoleFullPath);
      if (!startWithCurrentUserRoleFullPath) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "不可创建上级角色");
      }
    }

    // 更新数据库中上级节点信息及全路径
    role.setParentId(parentId);
    role.setParentName(parentName);
    role.setFullPath(fullPath);
    roleMapper.updateById(role);

    return ResponseEntity.ok().build();
  }
}
