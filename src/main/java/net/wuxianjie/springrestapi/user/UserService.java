package net.wuxianjie.springrestapi.user;

import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.role.RoleMapper;
import net.wuxianjie.springrestapi.shared.exception.ApiException;
import net.wuxianjie.springrestapi.shared.pagination.PaginationRequest;
import net.wuxianjie.springrestapi.shared.pagination.PaginationResult;
import net.wuxianjie.springrestapi.shared.security.core.CachedToken;
import net.wuxianjie.springrestapi.shared.security.core.TokenDetails;
import net.wuxianjie.springrestapi.shared.util.ApiUtils;
import net.wuxianjie.springrestapi.shared.util.StrUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final TimedCache<String, CachedToken> usernameToToken;
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
    // 用户仅可显示当前用户或其下级角色的用户
    final String currentUserRoleFullPath = getCurrentUserRoleFullPath();
    final long userId = ApiUtils.getAuthentication().orElseThrow().getUserId();
    long total = userMapper.
      selectCountByFullPathLikeOrUserIdUsernameLikeNicknameLikeEnabled(
        currentUserRoleFullPath,
        userId,
        request
      );
    final List<LinkedHashMap<String, Object>> list = userMapper
      .selectByFullPathLikeOrUserIdUsernameLikeNicknameLikeEnabledOrderByUpdatedAtDesc(
        currentUserRoleFullPath,
        userId,
        pagination,
        request
      );

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

    // 检查角色 id 是否有效
    // 用户仅可创建下级角色的用户
    checkForRole(request.getRoleId());

    // 密码编码
    final String rawPassword = request.getPassword();
    final String hashedPassword = passwordEncoder.encode(rawPassword);

    // 将用户数据保存至数据库
    final User user = new User();
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
    // 从数据库中获取用户数据
    final User user = userMapper.selectById(request.getUserId());
    if (user == null) {
      throw new ApiException(HttpStatus.NOT_FOUND, "用户不存在");
    }

    // 检查角色 id 是否有效
    // 用户仅可创建下级角色的用户
    checkForRole(request.getRoleId());

    // 更新数据库中的用户数据
    user.setRemark(request.getRemark());
    user.setNickname(request.getNickname());
    user.setEnabled(request.getEnabled());
    user.setRoleId(request.getRoleId());
    userMapper.updateById(user);
    return ResponseEntity.ok().build();
  }

  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<Void> resetPassword(final UserRequest request) {
    // 密码编码
    final String rawPassword = request.getPassword();
    final String hashedPassword = passwordEncoder.encode(rawPassword);

    // 更新数据库中的用户密码
    final User user = new User();
    user.setId(request.getUserId());
    user.setHashedPassword(hashedPassword);
    userMapper.updateById(user);
    return ResponseEntity.ok().build();
  }

  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<Void> changePassword(final SelfRequest request) {
    // 新旧密码不能相同
    final String oldPassword = request.getOldPassword();
    final String newPassword = request.getNewPassword();
    if (oldPassword.equals(newPassword)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "新旧密码不能相同");
    }

    // 比较传入的旧密码是否与数据库中保存的用户密码一致
    final long userId = getCurrentUserId();
    final String oldHashedPassword = userMapper.selectHashedPasswordById(userId);
    if (oldHashedPassword == null || !passwordEncoder.matches(oldPassword, oldHashedPassword)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "旧密码错误");
    }

    // 密码编码
    final String newHashedPassword = passwordEncoder.encode(newPassword);

    // 更新数据库中的密码
    final User user = new User();
    user.setId(userId);
    user.setHashedPassword(newHashedPassword);
    userMapper.updateById(user);

    // 密码修改成功后注销登录
    usernameToToken.remove(user.getUsername());
    return ResponseEntity.ok().build();
  }

  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<Void> changeSelf(final SelfRequest request) {
    // 获取当前用户 id
    final long userId = getCurrentUserId();

    // 更新数据库中的用户信息
    final User user = new User();
    user.setId(userId);
    user.setNickname(request.getNickname());
    userMapper.updateById(user);
    return ResponseEntity.ok().build();
  }

  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<Void> deleteUser(final long userId) {
    // 删除数据库中的用户数据
    // 用户仅可显示其下级角色的用户
    final String currentUserRoleFullPath = getCurrentUserRoleFullPath();
    userMapper.deleteByIdRoleFullPathLike(userId, currentUserRoleFullPath);
    return ResponseEntity.ok().build();
  }

  private void checkForRole(final long roleId) {
    // 角色 id 有效性校验
    final String newRoleFullPath = roleMapper.selectFullPathById(roleId);
    if (newRoleFullPath == null) {
      throw new ApiException(HttpStatus.NOT_FOUND, "角色不存在");
    }

    // 用户仅可创建下级角色的用户
    final String currentUserRoleFullPath = getCurrentUserRoleFullPath();
    final boolean isLowerNode = StrUtil.equals(newRoleFullPath, currentUserRoleFullPath)
      || StrUtil.startWith(newRoleFullPath, currentUserRoleFullPath + StrPool.DOT);
    if (!isLowerNode) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "仅可操作下级角色的用户");
    }
  }

  private String getCurrentUserRoleFullPath() {
    final TokenDetails token = ApiUtils.getAuthentication().orElseThrow();
    return Optional.ofNullable(roleMapper.selectFullPathById(token.getRoleId())).orElseThrow();
  }

  private static long getCurrentUserId() {
    final TokenDetails token = ApiUtils.getAuthentication().orElseThrow();
    return token.getUserId();
  }
}
