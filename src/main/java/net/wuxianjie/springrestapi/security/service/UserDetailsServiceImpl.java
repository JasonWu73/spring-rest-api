package net.wuxianjie.springrestapi.security.service;

import net.wuxianjie.springrestapi.security.dto.TokenDetails;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  private static final Map<String, TokenDetails> usernameToToken = new HashMap<>() {{
    put("wxj", new TokenDetails(
      "wxj",
      new BCryptPasswordEncoder().encode("111"),
      AuthorityUtils.commaSeparatedStringToAuthorityList("admin"),
      true,
      100,
      "吴仙杰"
    ));
    put("zs", new TokenDetails(
      "zs",
      new BCryptPasswordEncoder().encode("222"),
      AuthorityUtils.commaSeparatedStringToAuthorityList("user"),
      true,
      101,
      "张三"
    ));
    put("ls", new TokenDetails(
      "ls",
      new BCryptPasswordEncoder().encode("333"),
      AuthorityUtils.commaSeparatedStringToAuthorityList(""),
      true,
      102,
      "李四"
    ));
    put("ww", new TokenDetails(
      "ww",
      new BCryptPasswordEncoder().encode("444"),
      AuthorityUtils.commaSeparatedStringToAuthorityList("admin"),
      false,
      103,
      "王五"
    ));
  }};

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    if (!usernameToToken.containsKey(username)) {
      throw new UsernameNotFoundException("账号不存在");
    }
    return usernameToToken.get(username);
  }
}
