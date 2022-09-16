package net.wuxianjie.springrestapi.shared.operationlog.mapper;

import net.wuxianjie.springrestapi.shared.operationlog.dto.OpLogRequest;
import net.wuxianjie.springrestapi.shared.operationlog.entity.OpLog;
import net.wuxianjie.springrestapi.shared.pagination.PaginationRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @see <a href="https://blog.csdn.net/jx520/article/details/87452574">Mybatis 查询结果返回 Map、List＜Map＞、Pair_笑虾的博客-CSDN博客_mybatisplus返回map集合</a>
 */
@Mapper
public interface OpLogMapper {

  int selectCountByReqIpLikeEndPointLikeMessageLike(OpLogRequest request);

  List<LinkedHashMap<String, Object>> selectByReqIpLikeEndPointLikeMessageLikeOrderByReqTimeDesc(
    @Param("p") PaginationRequest pagination,
    @Param("q") OpLogRequest request
  );

  void insert(OpLog opLog);
}
