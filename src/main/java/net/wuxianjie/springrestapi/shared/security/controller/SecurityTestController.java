package net.wuxianjie.springrestapi.shared.security.controller;

import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.springrestapi.shared.operationlog.core.OpLog;
import net.wuxianjie.springrestapi.shared.security.annotation.IsAdmin;
import net.wuxianjie.springrestapi.shared.security.annotation.IsUser;
import net.wuxianjie.springrestapi.shared.security.dto.TokenDetails;
import net.wuxianjie.springrestapi.shared.security.util.ApiUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/test")
public class SecurityTestController {

  @IsAdmin
  @GetMapping("admin")
  public String admin() {
    return "管理员";
  }

  @IsUser
  @GetMapping("user")
  public String user() {
    return "普通用户";
  }

  @OpLog("测试身份验证")
  @GetMapping("logged-in")
  public String loggedIn() {
    final TokenDetails authentication = ApiUtils.getAuthentication().orElseThrow();
    log.info("当前用户：{}", authentication);
    return "己登录";
  }
}
