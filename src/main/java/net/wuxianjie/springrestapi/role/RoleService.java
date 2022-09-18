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
import java.util.*;

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
    final Integer parentId = request.getParentId();
    final Map<String, Object> hierarchyInfo = getRoleHierarchyInfo(roleId, parentId, null);

    // 更新数据库中上级节点信息及全路径
    role.setParentId(parentId);
    role.setParentName((String) hierarchyInfo.get("parentName"));
    role.setFullPath((String) hierarchyInfo.get("fullPath"));
    roleMapper.updateById(role);

    return ResponseEntity.ok().build();
  }

  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<Void> updateRole(final RoleRequest request) {
    // 数据存在性校验
    final Integer roleId = request.getRoleId();
    final Role role = roleMapper.selectById(roleId);
    if (role == null) {
      throw new ApiException(HttpStatus.NOT_FOUND, "角色不存在");
    }
    final String oldFullPath = role.getFullPath();

    // 角色名唯一性校验
    final String newName = request.getName();
    final String oldName = role.getName();
    final boolean needsUpdateName = !Objects.equals(newName, oldName);
    if (needsUpdateName) {
      final boolean nameExisted = roleMapper.selectExitsByNameIdNot(newName, roleId);
      if (nameExisted) {
        throw new ApiException(HttpStatus.CONFLICT, "已存在相同角色名");
      }
    }

    // 获取上级角色信息，并构造全路径
    final Integer newParentId = request.getParentId();
    final Integer oldParentId = role.getParentId();
    Map<String, Object> hierarchyInfo = new HashMap<>();
    final boolean needsUpdateParent = !Objects.equals(newParentId, oldParentId);
    if (needsUpdateParent) {
      hierarchyInfo = getRoleHierarchyInfo(roleId, newParentId, oldFullPath);
    }

    // 更新数据库
    role.setUpdatedAt(LocalDateTime.now());
    role.setRemark(request.getRemark());
    role.setName(newName);
    role.setMenus(request.getMenus());
    role.setParentId(newParentId);
    if (needsUpdateParent) {
      role.setParentName((String) hierarchyInfo.get("parentName"));
      role.setFullPath((String) hierarchyInfo.get("fullPath"));
    }
    roleMapper.updateById(role);

    // 若更新角色名，则还要更新其子角色的上级角色名
    if (needsUpdateName) {
      final Role child = new Role();
      child.setUpdatedAt(LocalDateTime.now());
      child.setParentId(roleId);
      child.setParentName(newName);
      roleMapper.updateUpdateAtParentNameByParentId(child);
    }

    // 若更新上级角色，则还要更新下级角色的完整路径
    if (needsUpdateParent) {
      final Role lower = new Role();
      lower.setUpdatedAt(LocalDateTime.now());
      final String newFullPathPrefix = role.getFullPath() + ".";
      lower.setFullPath(newFullPathPrefix);
      roleMapper.updateUpdateAtFullPathByFullPathLike(lower, oldFullPath + ".");
    }

    return ResponseEntity.ok().build();
  }

  private Map<String, Object> getRoleHierarchyInfo(
    final Integer roleId,
    final Integer parentId,
    final String oldFullPath
  ) {
    String fullPath = roleId + "";
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
      final boolean startWithCurrentUserRoleFullPath = Objects.equals(parentRoleFullPath, currentUserRoleFullPath) ||
        StrUtil.startWith(parentRoleFullPath, currentUserRoleFullPath + ".");
      if (!startWithCurrentUserRoleFullPath) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "仅可创建自身或下级角色");
      }

      if (oldFullPath != null) {
        // 不可将自身作为上级角色
        final boolean isSelf = Objects.equals(parentRoleFullPath, oldFullPath);
        if (isSelf) {
          throw new ApiException(HttpStatus.BAD_REQUEST, "不可将自身作为上级角色");
        }

        // 不可将下级作为上级角色
        final boolean startWithOldFullPath = StrUtil.startWith(parentRoleFullPath, oldFullPath + ".");
        if (startWithOldFullPath) {
          throw new ApiException(HttpStatus.BAD_REQUEST, "不可将下级作为上级角色");
        }
      }
    }

    final Map<String, Object> result = new HashMap<>();
    result.put("parentName", parentName);
    result.put("fullPath", fullPath);
    return result;
  }
}
