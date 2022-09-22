package net.wuxianjie.springrestapi.shared.security.core;

import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.shared.exception.ApiException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * @see <a href="https://www.baeldung.com/spring-security-method-security">Introduction to Spring Method Security | Introduction to Spring Method Security | Baeldung</a>
 * @see <a href="https://www.baeldung.com/get-user-in-spring-security">Retrieve User Information in Spring Security | Baeldung</a>
 * @see <a href="https://stackoverflow.com/questions/29888458/spring-security-role-hierarchy-not-working-using-java-config">Spring Security Role Hierarchy not working using Java Config - Stack Overflo</a>
 * @see <a href="https://www.toptal.com/spring/spring-security-tutorial">Spring Security JWT Tutorial | Toptal</a>
 * @see <a href="https://stackoverflow.com/questions/72381114/spring-security-upgrading-the-deprecated-websecurityconfigureradapter-in-spring">Spring Security: Upgrading the deprecated WebSecurityConfigurerAdapter in Spring Boot 2.7.0 - Stack Overflow</a>
 * @see <a href="https://www.baeldung.com/spring-security-exceptionhandler">Handle Spring Security Exceptions With @ExceptionHandler | Baeldung</a>
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtTokenFilter jwtTokenFilter;
  private final HandlerExceptionResolver handlerExceptionResolver;

  @Bean
  public RoleHierarchy roleHierarchy() {
    RoleHierarchyImpl role = new RoleHierarchyImpl();

    final String hierarchyValue = "" +
      "root > " + Authority.OperationLog.ROOT +
      "root > " + Authority.User.ROOT +
      "root > " + Authority.Role.ROOT +

      Authority.OperationLog.ROOT + " > " + Authority.OperationLog.VIEW + "\n" +

      Authority.User.ROOT + " > " + Authority.User.VIEW + "\n" +
      Authority.User.ROOT + " > " + Authority.User.ADD + "\n" +
      Authority.User.ROOT + " > " + Authority.User.EDIT + "\n" +
      Authority.User.ROOT + " > " + Authority.User.DEL + "\n" +
      Authority.User.ROOT + " > " + Authority.User.RESET + "\n" +

      Authority.Role.ROOT + " > " + Authority.Role.VIEW + "\n" +
      Authority.Role.ROOT + " > " + Authority.Role.ADD + "\n" +
      Authority.Role.ROOT + " > " + Authority.Role.EDIT + "\n" +
      Authority.Role.ROOT + " > " + Authority.Role.DEL + "\n";

    role.setHierarchy(hierarchyValue);
    return role;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
    http
      // 设置请求端点的权限
      .authorizeRequests()
      // 开放 API 除了此外配置外，于 JwtTokenFilter 再配置，以免过度 Token 身份验证
      .antMatchers(HttpMethod.POST, "/api/v1/token").permitAll()
      .antMatchers(HttpMethod.POST, "/api/v1/token/*").permitAll()
      .antMatchers("/api/*/public/**").permitAll()
      .antMatchers("/api/**").authenticated()
      .anyRequest().permitAll()
      // 启用 CORS 并禁用 CSRF
      .and().cors().and().csrf().disable()
      // 将会话管理设置为无状态
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      // 设置异常处理程序
      .and().exceptionHandling()
      // 设置身份验证（Authentication）异常处理程序，对应 401 HTTP 状态码
      .authenticationEntryPoint((request, response, authException) -> {
        final ApiException apiException = new ApiException(HttpStatus.UNAUTHORIZED, "身份验证失败", authException);
        handlerExceptionResolver.resolveException(request, response, null, apiException);
      })
      // 设置授权（Authorization）异常处理程序，对应 403 HTTP 状态码
      .accessDeniedHandler((request, response, accessDeniedException) -> {
        final ApiException apiException = new ApiException(HttpStatus.FORBIDDEN, "权限不足", accessDeniedException);
        handlerExceptionResolver.resolveException(request, response, null, apiException);
      })
      // 添加 JWT Token 过滤器
      .and().addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  /**
   * 公开身份验证管理器，在登录业务中调用 {@link AuthenticationManager#authenticate} 即可实现身份验证：
   *
   * <pre>{@code
   *   final Authentication authenticate = authenticationManager.authenticate(
   *     new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
   *   );
   *
   *   final UserDetails user = (UserDetails) authenticate.getPrincipal();
   * }</pre>
   *
   * @param config 身份验证配置
   * @return 身份验证管理器
   * @throws Exception 获取身份验证管理器失败的异常
   */
  @Bean
  public AuthenticationManager authenticationManager(final AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  /**
   * 使用 bcrypt 密码哈希算法作为 Spring Security 身份验证管理器的密码编码模式。
   *
   * @return bcrypt 密码编码器
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * CORS 过滤器，在启用 CORS 时，被 Spring Security 所使用 。
   *
   * @return CORS 过滤器
   */
  @Bean
  public CorsFilter corsFilter() {
    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    final CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOriginPattern("*");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }
}
