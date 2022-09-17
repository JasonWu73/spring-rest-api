package net.wuxianjie.springrestapi.shared.security.core;

import lombok.Data;

@Data
public class CachedToken {

  private String accessToken;
  private String refreshToken;
}
