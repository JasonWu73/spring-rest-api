package net.wuxianjie.springrestapi.shared.operationlog.mapper;

import net.wuxianjie.springrestapi.shared.operationlog.entity.OpLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OpLogMapper {

  void insert(OpLog opLog);
}
