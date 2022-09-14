package net.wuxianjie.springrestapi.user.mapper;

import net.wuxianjie.springrestapi.user.dto.AuthData;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

  AuthData selectByUsername(String username);
}
