package net.wuxianjie.springrestapi.shared.security.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

/**
 * @see <a href="https://www.baeldung.com/configuration-properties-in-spring-boot">Guide to @ConfigurationProperties in Spring Boot | Baeldung</a>
 */
@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

  @NotBlank(message = "JWT Token 的签名密钥不能为空")
  private String jwtKey;
}
