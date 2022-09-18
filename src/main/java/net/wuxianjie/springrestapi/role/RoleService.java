package net.wuxianjie.springrestapi.role;

import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.shared.security.core.TokenDetails;
import net.wuxianjie.springrestapi.shared.util.ApiUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
}
