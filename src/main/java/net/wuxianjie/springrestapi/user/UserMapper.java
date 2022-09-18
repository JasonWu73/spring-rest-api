package net.wuxianjie.springrestapi.user;

import net.wuxianjie.springrestapi.shared.pagination.PaginationRequest;
import net.wuxianjie.springrestapi.shared.security.core.AuthData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;
import java.util.List;

@Mapper
public interface UserMapper {

  User selectById(int userId);

  AuthData selectByUsername(String username);

  boolean selectExistsByUsername(String username);

  int selectCountByUsernameLikeNicknameLikeEnabled(UserRequest request);

  List<LinkedHashMap<String, Object>> selectByUsernameLikeNicknameLikeEnabledOrderByUpdatedAtDesc(
    @Param("p") PaginationRequest pagination,
    @Param("q") UserRequest request
  );

  void insert(User user);

  void updateById(User user);

  void deleteById(int userId);
}
