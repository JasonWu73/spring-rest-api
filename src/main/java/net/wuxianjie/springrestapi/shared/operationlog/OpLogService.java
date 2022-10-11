package net.wuxianjie.springrestapi.shared.operationlog;

import lombok.RequiredArgsConstructor;
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
    final long total = opLogMapper
      .selectCountByRequestIpLikeEndpointLikeMessageLike(request);
    final List<LinkedHashMap<String, Object>> list = opLogMapper
      .selectByRequestIpLikeEndpointLikeMessageLikeOrderByRequestTimeDesc(pagination, request);

    // 构造并返回分页结果
    return ResponseEntity.ok(new PaginationResult<>(
      pagination.getPageNumber(),
      pagination.getPageSize(),
      total,
      list
    ));
  }
}
