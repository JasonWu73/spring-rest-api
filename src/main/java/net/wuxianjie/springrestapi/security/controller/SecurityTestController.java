package net.wuxianjie.springrestapi.security.controller;

import net.wuxianjie.springrestapi.security.annotation.IsAdmin;
import net.wuxianjie.springrestapi.security.annotation.IsUser;
import net.wuxianjie.springrestapi.security.dto.TokenDetails;
import net.wuxianjie.springrestapi.security.util.ApiUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class SecurityTestController {

  @IsAdmin
  @GetMapping("admin")
  public String admin() {
    final TokenDetails authentication = ApiUtils.getAuthentication().orElseThrow();
    System.out.println(authentication);
    return "管理员";
  }

  @IsUser
  @GetMapping("user")
  public String user() {
    return "普通用户";
  }

  @GetMapping("logged-in")
  public String loggedIn() {
    return "己登录";
  }
}
