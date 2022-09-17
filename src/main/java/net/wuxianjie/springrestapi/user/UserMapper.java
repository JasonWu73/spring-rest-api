package net.wuxianjie.springrestapi.user;

import net.wuxianjie.springrestapi.shared.security.core.AuthData;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

  AuthData selectByUsername(String username);
}
