package net.wuxianjie.springrestapi.security.dto;

import lombok.Data;

@Data
public class CachedToken {

  private String accessToken;
  private String refreshToken;
}
