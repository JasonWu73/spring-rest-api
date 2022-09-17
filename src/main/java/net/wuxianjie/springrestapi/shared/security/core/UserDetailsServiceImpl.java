package net.wuxianjie.springrestapi.shared.security.core;

import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.user.UserMapper;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserMapper userMapper;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    final AuthData authData = userMapper.selectByUsername(username);
    if (authData == null) {
      // 该异常内容并不会打印或显示给客户端
      throw new UsernameNotFoundException("账号不存在");
    }

    return new TokenDetails(
      authData.getUsername(),
      authData.getHashedPassword(),
      AuthorityUtils.commaSeparatedStringToAuthorityList(authData.getMenus()),
      authData.getEnabled(),
      authData.getUserId(),
      authData.getNickname()
    );
    /*
    if (!usernameToToken.containsKey(username)) {
      throw new UsernameNotFoundException("账号不存在");
    }
    return usernameToToken.get(username);
    */
  }

  /*
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
  */
}
