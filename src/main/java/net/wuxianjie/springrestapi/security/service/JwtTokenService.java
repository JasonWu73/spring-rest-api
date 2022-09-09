package net.wuxianjie.springrestapi.security.service;

import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSignerUtil;
import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.security.config.SecurityProperties;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

  public static final String ACCESS_TOKEN_TYPE = "access";
  public static final String REFRESH_TOKEN_TYPE = "refresh";
  public static final Integer EXPIRES_IN_SECONDS = 1800;

  private final SecurityProperties properties;

  public String createToken(final String username, final String type) {
    final long currentTimeSeconds = System.currentTimeMillis() / 1000;
    return JWTUtil.createToken(
      new HashMap<>() {{
        put("username", username);
        put("type", type);
        put(JWTPayload.EXPIRES_AT, currentTimeSeconds + EXPIRES_IN_SECONDS);
      }},
      properties.getJwtKey().getBytes()
    );
  }

  public void validateToken(final String token) throws ValidateException {
    JWTValidator
      .of(token)
      .validateAlgorithm(JWTSignerUtil.hs256(properties.getJwtKey().getBytes()))
      .validateDate();
  }

  public String getUsername(final String token) {
    final JWT jwt = JWTUtil.parseToken(token);
    return (String) jwt.getPayload("username");
  }

  public String getType(final String token) {
    final JWT jwt = JWTUtil.parseToken(token);
    return (String) jwt.getPayload("type");
  }
}
