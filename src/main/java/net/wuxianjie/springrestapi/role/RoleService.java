package net.wuxianjie.springrestapi.role;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.text.StrSplitter;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.shared.exception.ApiException;
import net.wuxianjie.springrestapi.shared.security.core.TokenDetails;
import net.wuxianjie.springrestapi.shared.util.ApiUtils;
import net.wuxianjie.springrestapi.user.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RoleService {

  private final RoleMapper roleMapper;
  private final UserMapper userMapper;

  public ResponseEntity<List<LinkedHashMap<String, Object>>> getRoles() {
    // 获取当前用户角色的所有下级角色
    // 若角色的节点完整路径为空, 则代表当前用户拥有全部角色的权限
    final TokenDetails token = ApiUtils.getAuthentication().orElseThrow();
    final String currentUserRoleFullPath = roleMapper.selectFullPathById(token.getRoleId());
    final List<LinkedHashMap<String, Object>> list;
    if (StrUtil.isEmpty(currentUserRoleFullPath)) {
      list = roleMapper.selectAll();
    }
    // 若角色的节点完整路径不为空, 则获取下级角色
    else {
      list = roleMapper.selectByFullPathOrLike(currentUserRoleFullPath, currentUserRoleFullPath + ".%");
    }

    // 将菜单字符串转换为列表
    for (final LinkedHashMap<String, Object> item : list) {
      final String menus = (String) item.get("menus");
      final List<String> menusList = StrSplitter.split(menus, StrPool.COMMA, 0, true, true);
      item.put("menus", menusList);
    }
    return ResponseEntity.ok(list);
  }

  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<Void> addRole(final RoleRequest request) {
    // 角色名唯一性校验
    final String name = request.getName();
    final boolean nameExisted = roleMapper.selectExitsByName(name);
    if (nameExisted) {
      throw new ApiException(HttpStatus.CONFLICT, "已存在相同角色名");
    }

    // 将角色数据保存至数据库，并获取保存后的角色 id
    final Role role = new Role();
    role.setRemark(request.getRemark());
    role.setName(name);
    role.setMenus(StrUtil.join(StrPool.COMMA, request.getMenus()));
    roleMapper.insert(role);

    // 更新数据库中角色的上级角色名及全路径
    final Integer parentId = request.getParentId();
    final Map<String, Object> hierarchyInfo = getRoleHierarchyInfo(role.getId(), parentId, null);
    role.setParentId(parentId);
    role.setParentName((String) hierarchyInfo.get("parentName"));
    role.setFullPath((String) hierarchyInfo.get("fullPath"));
    roleMapper.updateById(role);
    return ResponseEntity.ok().build();
  }

  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<Void> updateRole(final RoleRequest request) {
    // 从数据库中获取角色数据
    final Integer roleId = request.getRoleId();
    final Role role = roleMapper.selectById(roleId);
    if (role == null) {
      throw new ApiException(HttpStatus.NOT_FOUND, "角色不存在");
    }

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
    final Map<String, Object> hierarchyInfo;
    final boolean needsUpdateParent = !Objects.equals(newParentId, oldParentId);
    final String oldFullPath = role.getFullPath();
    if (needsUpdateParent) {
      hierarchyInfo = getRoleHierarchyInfo(roleId, newParentId, oldFullPath);
    } else {
      hierarchyInfo = new HashMap<>();
    }

    // 更新数据库中的角色数据
    role.setRemark(request.getRemark());
    role.setName(newName);
    role.setMenus(StrUtil.join(StrPool.COMMA, request.getMenus()));
    role.setParentId(newParentId);
    if (needsUpdateParent) {
      role.setParentName((String) hierarchyInfo.get("parentName"));
      role.setFullPath((String) hierarchyInfo.get("fullPath"));
    }
    roleMapper.updateById(role);

    // 若更新角色名, 则还要更新其子角色的上级角色名
    if (needsUpdateName) {
      final Role child = new Role();
      child.setParentId(roleId);
      child.setParentName(newName);
      roleMapper.updateUpdateAtParentNameByParentId(child);
    }

    // 若更新上级角色, 则还要更新下级角色的全路径
    if (needsUpdateParent) {
      roleMapper.updateFullPathByFullPathLike(role.getFullPath() + StrPool.DOT, oldFullPath + StrPool.DOT);
    }
    return ResponseEntity.ok().build();
  }

  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<Void> deleteRole(final int roleId) {
    // 当角色存在下级角色时不可删除
    final String fullPath = roleMapper.selectFullPathById(roleId);
    if (StrUtil.isEmpty(fullPath)) {
      return ResponseEntity.ok().build();
    }
    final boolean existsLowerNode = roleMapper.selectExitsByFullPathLike(fullPath + StrPool.DOT + "%");
    if (existsLowerNode) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "不可删除存在下级的角色");
    }

    // 当角色已被用户使用时不可删除
    final boolean existsUser = userMapper.selectExistsByRoleId(roleId);
    if (existsUser) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "不可删除已被用户使用中的角色");
    }

    // 删除数据库中的角色数据
    roleMapper.deleteById(roleId);
    return ResponseEntity.ok().build();
  }

  private Map<String, Object> getRoleHierarchyInfo(
    final int roleId,
    final int parentId,
    final String oldFullPath
  ) {
    final Role parentRole = roleMapper.selectById(parentId);
    if (parentRole == null) {
      throw new ApiException(HttpStatus.NOT_FOUND, "上级角色不存在");
    }
    final String parentName = parentRole.getName();
    final String parentRoleFullPath = parentRole.getFullPath();
    final String fullPath = parentRoleFullPath + StrPool.DOT + roleId;

    // 用户仅可创建自身或下级角色
    final TokenDetails token = ApiUtils.getAuthentication().orElseThrow();
    final String currentUserRoleFullPath = Optional.ofNullable(roleMapper.selectFullPathById(token.getRoleId()))
      .orElse("");
    final boolean startWithCurrentUserRoleFullPath = Objects.equals(parentRoleFullPath, currentUserRoleFullPath) ||
      StrUtil.startWith(parentRoleFullPath, currentUserRoleFullPath + StrPool.DOT);
    if (!startWithCurrentUserRoleFullPath) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "仅可创建自身或下级角色");
    }

    // 检查是否需要更新其他相关节点信息
    if (oldFullPath != null) {
      // 不可将自身作为上级角色
      final boolean isSelf = Objects.equals(parentRoleFullPath, oldFullPath);
      if (isSelf) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "不可将自身作为上级角色");
      }

      // 不可将下级作为上级角色
      final boolean isLowerNode = StrUtil.startWith(parentRoleFullPath, oldFullPath + StrPool.DOT);
      if (isLowerNode) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "不可将下级作为上级角色");
      }
    }
    return new HashMap<>() {{
      put("parentName", parentName);
      put("fullPath", fullPath);
    }};
  }
}
