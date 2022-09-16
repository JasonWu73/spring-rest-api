package net.wuxianjie.springrestapi.shared.operationlog;

import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.shared.pagination.PaginationRequest;
import net.wuxianjie.springrestapi.shared.pagination.PaginationResult;
import net.wuxianjie.springrestapi.shared.security.Authority;
import net.wuxianjie.springrestapi.shared.validation.group.ReadOne;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OpLogController {

  private final OpLogService opLogService;

  /**
   * 获取操作日志分页列表。
   *
   * @param pagination <pre>{@code
   * pageNumber: 1 // 页码，必填，值 >= 1
   * pageSize: 10 // 每页显示条目个数，必填，值 >= 1
   * }</pre>
   * @param request <pre>{@code
   * startTime: 2022-09-16 15:35:55 // 开始时间，必填，格式为 yyyy-MM-dd HH:mm:ss
   * endTime: 2022-09-16 16:00:00 // 结束时间，必填，格式为 yyyy-MM-dd HH:mm:ss
   * }</pre>
   * @return <pre>{@code
   * {
   *   "pageNumber": 1, // 页码
   *   "pageSize": 10, // 每页显示条目个数
   *   "total": 2, // 总条目数
   *   "list": [ // 具体数据列表
   *     {
   *       "requestTime": "2022-09-16 15:35:57", // 请求时间
   *       "requestIp": "127.0.0.1", // 请求方 IP
   *       "endpoint": "/api/v1/token [POST]", // 接口端点
   *       "username": "zhangsan", // 用户名
   *       "message": "虚拟方法", // 方法信息
   *       "method": "xxx.xxx.test()" // 方法名
   *       "params": "{\"request\":{\"param\":\"test\"}}" // 方法参数（JSON 字符串）
   *     }
   *   ]
   * }
   * }</pre>
   */
  @GetMapping("op-log")
  @PreAuthorize(Authority.OperationLog.HAS_VIEW)
  public ResponseEntity<PaginationResult<LinkedHashMap<String, Object>>> getLogs(
    @Valid final PaginationRequest pagination,
    @Validated(ReadOne.class) final OpLogRequest request
  ) {
    return opLogService.getLogs(pagination, request);
  }
}
