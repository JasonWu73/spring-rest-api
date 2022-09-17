package net.wuxianjie.springrestapi.user;

import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.shared.pagination.PaginationRequest;
import net.wuxianjie.springrestapi.shared.pagination.PaginationResult;
import net.wuxianjie.springrestapi.shared.util.StrUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserMapper userMapper;

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
}
