package net.wuxianjie.springrestapi.role;

import org.apache.ibatis.annotations.Mapper;

import java.util.LinkedHashMap;
import java.util.List;

@Mapper
public interface RoleMapper {

  String selectFullPathById(long roleId);

  Role selectById(long roleId);

  boolean selectExitsByName(String name);

  boolean selectExitsByNameIdNot(String name, long roleId);

  boolean selectExitsByFullPathLike(String fullPathPrefix);

  List<LinkedHashMap<String, Object>> selectByFullPathOrLike(String fullPath);

  void insert(Role role);

  void updateById(Role role);

  void updateUpdateAtParentNameByParentId(Role child);

  void updateFullPathByFullPathLike(String newFullPathPrefix, String oldFullPathPrefix);

  void deleteById(long roleId);
}
