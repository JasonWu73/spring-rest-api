package net.wuxianjie.springrestapi.user;

import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.shared.pagination.PaginationRequest;
import net.wuxianjie.springrestapi.shared.pagination.PaginationResult;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserMapper userMapper;

  public ResponseEntity<PaginationResult<LinkedHashMap<String, Object>>> getUsers(
    final PaginationRequest pagination,
    final UserRequest request
  ) {
    return null;
  }
}
