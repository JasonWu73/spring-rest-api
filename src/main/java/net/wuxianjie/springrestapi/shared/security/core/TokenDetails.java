package net.wuxianjie.springrestapi.shared.security.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@ToString
@RequiredArgsConstructor
public class TokenDetails implements UserDetails {

  // UserDetails 所需字段
  private final String username;
  private final String password;
  private final Collection<? extends GrantedAuthority> authorities;
  private final boolean accountNonExpired;
  private final boolean accountNonLocked;
  private final boolean credentialsNonExpired;
  private final boolean enabled;

  // 业务字段
  private long userId;
  private String nickname;
  private long roleId;

  public TokenDetails(
    final String username,
    final String password,
    final Collection<? extends GrantedAuthority> authorities,
    final boolean enabled,
    final long userId,
    final String nickname,
    final long roleId
  ) {
    this(username, password, authorities, true, true, true, enabled);
    this.userId = userId;
    this.nickname = nickname;
    this.roleId = roleId;
  }
}
