package net.wuxianjie.springrestapi.user;

import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.shared.pagination.PaginationRequest;
import net.wuxianjie.springrestapi.shared.pagination.PaginationResult;
import net.wuxianjie.springrestapi.shared.security.core.Authority;
import net.wuxianjie.springrestapi.shared.validation.group.CreateOne;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  /**
   * 获取用户分页列表。
   *
   * @param pagination <pre>{@code
   * pageNumber: 1 // 页码，必填，值 >= 1
   * pageSize: 10 // 每页显示条目个数，必填，值 >= 1
   * }</pre>
   * @param request <pre>{@code
   * username: zhangsan // 用户名，长度 <= 100
   * nickname: 张三 // 用户昵称，长度 <= 100
   * enabled: 1 // 是否启用：1：已启用，0：已禁用
   * }</pre>
   * @return <pre>{@code
   * {
   *   "pageNumber": 1, // 页码
   *   "pageSize": 10, // 每页显示条目个数
   *   "total": 1, // 总条目数
   *   "list": [ // 具体数据列表
   *     {
   *       "userId": 123, // 用户 id
   *       "updatedAt": "2022-09-16 11:44:07", // 修改时间
   *       "remark": "测试备注", // 备注
   *       "username": "zhangsan", // 用户名
   *       "nickname": "张三", // 用户昵称
   *       "enabled": 1, // 是否启用：1：已启用，0：已禁用
   *       "roleId": 123, // 角色 id
   *       "role": "测试人员", // 角色名
   *       "menus": "xxx,xxx" // 功能权限
   *     }
   *   ]
   * }
   * }</pre>
   */
  @GetMapping("user")
  @PreAuthorize(Authority.UserManagement.HAS_VIEW)
  public ResponseEntity<PaginationResult<LinkedHashMap<String, Object>>> getUsers(
    @Valid final PaginationRequest pagination,
    @Valid final UserRequest request
  ) {
    return userService.getUsers(pagination, request);
  }

  /**
   * 新增用户。
   *
   * @param request <pre>{@code
   * {
   *   "username": "zhangsan", // 用户名，必填，长度 <= 100，用户名只能包含中文、英文、数字或_，且必须以中文或英文开头
   *   "password": "123", // 密码，必填，长度 <= 100
   *   "enabled": 1, // 是否启用：1：已启用，0：已禁用，必填
   *   "roleId": 1, // 角色 id，必填
   *   "nickname": "张三", // 用户昵称，长度 <= 100
   *   "remark": "测试用户" // 备注，长度 <= 200
   * }
   * }</pre>
   * @return <pre>{@code
   * {}
   * }</pre>
   */
  @PostMapping("user")
  @PreAuthorize(Authority.UserManagement.HAS_ADD)
  public ResponseEntity<Void> addUser(@RequestBody @Validated(CreateOne.class) final UserRequest request) {
    return userService.addUser(request);
  }
}
