package net.wuxianjie.springrestapi.shared.operationlog.service;

import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.shared.operationlog.dto.OpLogRequest;
import net.wuxianjie.springrestapi.shared.operationlog.mapper.OpLogMapper;
import net.wuxianjie.springrestapi.shared.pagination.PaginationRequest;
import net.wuxianjie.springrestapi.shared.pagination.PaginationResult;
import net.wuxianjie.springrestapi.shared.util.StrUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OpLogService {

  private final OpLogMapper opLogMapper;

  public ResponseEntity<PaginationResult<LinkedHashMap<String, Object>>> getLogs(
    final PaginationRequest pagination,
    final OpLogRequest request
  ) {
    // 设置模糊搜索参数
    request.setRequestIp(StrUtils.toNullableLikeValue(request.getRequestIp()));
    request.setEndpoint(StrUtils.toNullableLikeValue(request.getEndpoint()));
    request.setMessage(StrUtils.toNullableLikeValue(request.getMessage()));

    // 获取分页列表和总条目数
    final int total = opLogMapper.selectCountByReqIpLikeEndPointLikeMessageLike(request);
    final List<LinkedHashMap<String, Object>> list = opLogMapper
      .selectByReqIpLikeEndPointLikeMessageLikeOrderByReqTimeDesc(pagination, request);

    // 构造并返回分页结果
    final PaginationResult<LinkedHashMap<String, Object>> result = new PaginationResult<>();
    result.setPageNumber(pagination.getPageNumber());
    result.setPageSize(pagination.getPageSize());
    result.setTotal(total);
    result.setList(list);
    return ResponseEntity.ok(result);
  }
}
