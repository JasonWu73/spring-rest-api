package net.wuxianjie.springrestapi.media;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MediaMapper {

  String selectFilePathByFilePathLike(String filePath);
}
