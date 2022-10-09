package net.wuxianjie.springrestapi.role;

import org.apache.ibatis.annotations.Mapper;

import java.util.LinkedHashMap;
import java.util.List;

@Mapper
public interface RoleMapper {

  String selectFullPathById(int roleId);

  Role selectById(int roleId);

  boolean selectExitsByName(String name);

  boolean selectExitsByNameIdNot(String name, int roleId);

  boolean selectExitsByFullPathLike(String fullPathPrefix);

  List<LinkedHashMap<String, Object>> selectAll();

  List<LinkedHashMap<String, Object>> selectByFullPathOrLike(String fullPath, String fullPathLike);

  void insert(Role role);

  void updateById(Role role);

  void updateUpdateAtParentNameByParentId(Role child);

  void updateFullPathByFullPathLike(String newFullPathPrefix, String oldFullPathPrefix);

  void deleteById(int roleId);
}
