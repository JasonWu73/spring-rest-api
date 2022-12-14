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
      "root > " + Authority.OperationLog.ROOT + "\n" +
      "root > " + Authority.User.ROOT + "\n" +
      "root > " + Authority.Role.ROOT + "\n" +

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

    // ??????????????????
    /*
    System.out.println("###################################");
    System.out.println(hierarchyValue);
    System.out.println("###################################");
    */

    role.setHierarchy(hierarchyValue);
    return role;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
    http
      // ???????????????????????????
      .authorizeRequests()
      // ?????? API ?????????????????????, ??? JwtTokenFilter ?????????, ???????????? Token ????????????
      .antMatchers(HttpMethod.POST, "/api/v1/token").permitAll()
      .antMatchers(HttpMethod.POST, "/api/v1/token/*").permitAll()
      .antMatchers("/api/*/public/**").permitAll()
      .antMatchers(HttpMethod.GET, "/vod/**", "/dl/**").permitAll()
      .antMatchers("/api/**").authenticated()
      .anyRequest().permitAll()
      // ?????? CORS ????????? CSRF
      .and().cors().and().csrf().disable()
      // ?????????????????????????????????
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      // ????????????????????????
      .and().exceptionHandling()
      // ?????????????????? (Authentication) ??????????????????, ?????? 401 HTTP ?????????
      .authenticationEntryPoint((request, response, authException) -> {
        final ApiException apiException = new ApiException(HttpStatus.UNAUTHORIZED, "??????????????????", authException);
        handlerExceptionResolver.resolveException(request, response, null, apiException);
      })
      // ???????????????Authorization?????????????????????????????? 403 HTTP ?????????
      .accessDeniedHandler((request, response, accessDeniedException) -> {
        final ApiException apiException = new ApiException(HttpStatus.FORBIDDEN, "????????????", accessDeniedException);
        handlerExceptionResolver.resolveException(request, response, null, apiException);
      })
      // ?????? JWT Token ?????????
      .and().addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  /**
   * ???????????????????????????, ???????????????????????? {@link AuthenticationManager#authenticate} ????????????????????????:
   *
   * <pre>{@code
   *   final Authentication authenticate = authenticationManager.authenticate(
   *     new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
   *   );
   *
   *   final UserDetails user = (UserDetails) authenticate.getPrincipal();
   * }</pre>
   *
   * @param config ??????????????????
   * @return ?????????????????????
   * @throws Exception ??????????????????????????????????????????
   */
  @Bean
  public AuthenticationManager authenticationManager(final AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  /**
   * ?????? bcrypt ???????????????????????? Spring Security ??????????????????????????????????????????.
   *
   * @return bcrypt ???????????????
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * CORS ?????????, ????????? CORS ???, ??? Spring Security ?????????.
   *
   * @return CORS ?????????
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
