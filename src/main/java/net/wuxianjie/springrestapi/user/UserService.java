package net.wuxianjie.springrestapi.user;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.role.Role;
import net.wuxianjie.springrestapi.role.RoleMapper;
import net.wuxianjie.springrestapi.shared.exception.ApiException;
import net.wuxianjie.springrestapi.shared.pagination.PaginationRequest;
import net.wuxianjie.springrestapi.shared.pagination.PaginationResult;
import net.wuxianjie.springrestapi.shared.security.core.TokenDetails;
import net.wuxianjie.springrestapi.shared.util.ApiUtils;
import net.wuxianjie.springrestapi.shared.util.StrUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;
  private final RoleMapper roleMapper;

  public ResponseEntity<PaginationResult<LinkedHashMap<String, Object>>> getUsers(
    final PaginationRequest pagination,
    final UserRequest request
  ) {
    // 设置模糊搜索参数
    request.setUsername(StrUtils.toNullableLikeValue(request.getUsername()));
    request.setNickname(StrUtils.toNullableLikeValue(request.getNickname()));

    // 获取分页列表和总条目数
    final int total = userMapper.selectCountByUsernameLikeNicknameLikeEnabled(request);
    final List<LinkedHashMap<String, Object>> list = userMapper
      .selectByUsernameLikeNicknameLikeEnabledOrderByUpdatedAtDesc(pagination, request);

    // 构造并返回分页结果
    return ResponseEntity.ok(new PaginationResult<>(
      pagination.getPageNumber(),
      pagination.getPageSize(),
      total,
      list
    ));
  }

  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<Void> addUser(final UserRequest request) {
    // 用户名唯一性校验
    final String username = request.getUsername();
    final boolean usernameExisted = userMapper.selectExistsByUsername(username);
    if (usernameExisted) {
      throw new ApiException(HttpStatus.CONFLICT, "已存在相同用户名");
    }

    checkForRole(request.getRoleId());

    // 密码编码
    final String rawPassword = request.getPassword();
    final String hashedPassword = passwordEncoder.encode(rawPassword);

    // 保存数据库
    final User user = new User();
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    user.setRemark(request.getRemark());
    user.setUsername(request.getUsername());
    user.setNickname(request.getNickname());
    user.setHashedPassword(hashedPassword);
    user.setEnabled(request.getEnabled());
    user.setRoleId(request.getRoleId());
    userMapper.insert(user);

    return ResponseEntity.ok().build();
  }

  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<Void> updateUser(final UserRequest request) {
    // 数据存在性校验
    final User user = userMapper.selectById(request.getUserId());
    if (user == null) {
      throw new ApiException(HttpStatus.NOT_FOUND, "用户不存在");
    }

    checkForRole(request.getRoleId());

    // 更新数据库
    user.setUpdatedAt(LocalDateTime.now());
    user.setRemark(request.getRemark());
    user.setNickname(request.getNickname());
    user.setEnabled(request.getEnabled());
    user.setRoleId(request.getRoleId());
    userMapper.updateById(user);

    return ResponseEntity.ok().build();
  }

  private void checkForRole(final int roleId) {
    // 角色 id 有效性校验
    final Role addedUserRole = roleMapper.selectById(roleId);
    if (addedUserRole == null) {
      throw new ApiException(HttpStatus.NOT_FOUND, "角色不存在");
    }

    // 用户仅可创建自身或下级角色的用户
    final TokenDetails token = ApiUtils.getAuthentication().orElseThrow();
    final Role currentUserRole = roleMapper.selectById(token.getRoleId());
    final String currentUserRoleFullPath = currentUserRole.getFullPath() == null ? "" : currentUserRole.getFullPath();
    final String addedUserFullPath = addedUserRole.getFullPath() == null ? "" : addedUserRole.getFullPath();
    final boolean startWithCurrentUserRoleFullPath = StrUtil.startWith(addedUserFullPath, currentUserRoleFullPath);
    if (!startWithCurrentUserRoleFullPath) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "不可创建上级角色的用户");
    }
  }
}
