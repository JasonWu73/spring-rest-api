package net.wuxianjie.springrestapi.role;

import org.apache.ibatis.annotations.Mapper;

import java.util.LinkedHashMap;
import java.util.List;

@Mapper
public interface RoleMapper {

  Role selectById(int roleId);

  List<LinkedHashMap<String, Object>> selectAll();

  List<LinkedHashMap<String, Object>> selectByFullPathOrLike(String fullPath, String fullPathLike);
}
