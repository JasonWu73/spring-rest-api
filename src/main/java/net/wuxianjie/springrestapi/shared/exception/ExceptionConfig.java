package net.wuxianjie.springrestapi.shared.exception;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

/**
 * @see <a href="https://stackoverflow.com/questions/42541520/spring-boot-custom-errorattributes-http-status-not-set-to-response">java - Spring Boot custom ErrorAttributes http status not set to response - Stack Overflow</a>
 * @see <a href="https://github.com/spring-projects/spring-boot/issues/18952">Inability to override response status with BasicErrorController in 2.1.10 · Issue #18952 · spring-projects/spring-boot</a>
 */
@Configuration
public class ExceptionConfig {

  @Bean
  public ErrorAttributes errorAttributes() {
    return new DefaultErrorAttributes() {
      @Override
      public Map<String, Object> getErrorAttributes(final WebRequest webRequest, final ErrorAttributeOptions options) {
        return super.getErrorAttributes(webRequest, options);

        // 以转由 ExceptionControllerAdvice#handleApiException(ApiException) 处理
        /*
        final Throwable error = getError(webRequest);
        if (error instanceof ApiException) {
          final ApiException apiException = (ApiException) error;
          final int httpStatus = apiException.getStatus().value();

          // 新增/修改/删除 Spring Boot 错误响应结果字段
          errorAttributes.put("status", httpStatus);
          errorAttributes.put("error", apiException.getMessage());

          // 以下方法已经无法再修改 HTTP 状态码
          // 因为 BasicErrorController#rror(HttpServletRequest request) 在 getErrorAttributes 前已获取了 HTTP 状态码
          // webRequest.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, httpStatus, RequestAttributes.SCOPE_REQUEST);
        }
        */

        // Console.error("HTTP 状态码：{}", webRequest.getAttribute(RequestDispatcher.ERROR_STATUS_CODE, RequestAttributes.SCOPE_REQUEST));
      }
    };
  }
}
