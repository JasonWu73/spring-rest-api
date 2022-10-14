package net.wuxianjie.springrestapi.user;

import net.wuxianjie.springrestapi.shared.pagination.PaginationRequest;
import net.wuxianjie.springrestapi.shared.security.core.AuthData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;
import java.util.List;

@Mapper
public interface UserMapper {

  String selectHashedPasswordById(long userId);

  User selectById(long userId);

  AuthData selectByUsername(String username);

  boolean selectExistsByUsername(String username);

  boolean selectExistsByRoleId(long roleId);

  long selectCountByUsernameLikeNicknameLikeEnabled(String currentUserRoleFullPath,
                                                    @Param("q") UserRequest request);

  List<LinkedHashMap<String, Object>> selectByUsernameLikeNicknameLikeEnabledOrderByUpdatedAtDesc(
    String currentUserRoleFullPath,
    @Param("p") PaginationRequest pagination,
    @Param("q") UserRequest request
  );

  void insert(User user);

  void updateById(User user);

  void deleteByIdRoleFullPathLike(long userId, final String currentUserRoleFullPath);
}
