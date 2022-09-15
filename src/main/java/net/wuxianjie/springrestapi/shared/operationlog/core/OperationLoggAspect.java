package net.wuxianjie.springrestapi.shared.operationlog.core;

import cn.hutool.core.lang.Console;
import cn.hutool.core.text.StrSplitter;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.springrestapi.shared.security.dto.TokenDetails;
import net.wuxianjie.springrestapi.shared.security.util.ApiUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @see <a href="https://www.appsdeveloperblog.com/a-guide-to-spring-boot-aop-to-record-user-operations/">A guide to Spring Boot AOP to Record User Operations - Apps Developer Blog</a>
 * @see <a href="https://stackoverflow.com/questions/44671154/jackson-filtering-out-fields-without-annotations">java - Jackson filtering out fields without annotations - Stack Overflow</a>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLoggAspect {

  private final HttpServletRequest request;
  private final Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer;

  @Around("@annotation(OpLog)")
  public Object around(final ProceedingJoinPoint joinPoint) throws Throwable {
    final LocalDateTime requestTime = LocalDateTime.now();
    final Object result = joinPoint.proceed();
    saveLog(joinPoint, requestTime);
    return result;
  }

  private void saveLog(final ProceedingJoinPoint joinPoint, final LocalDateTime requestTime) throws JsonProcessingException {
    final net.wuxianjie.springrestapi.shared.operationlog.entity.OperationLog operationLog = new net.wuxianjie.springrestapi.shared.operationlog.entity.OperationLog();
    operationLog.setRequestTime(requestTime);

    // 请求信息
    final String requestIp = ServletUtil.getClientIP(request);
    operationLog.setRequestIp(requestIp);
    final String requestUri = request.getRequestURI();
    final String requestMethod = request.getMethod();
    final String endPoint = StrUtil.format("{} [{}]", requestUri, requestMethod);
    operationLog.setEndPoint(endPoint);
    final String username = ApiUtils.getAuthentication().map(TokenDetails::getUsername).orElse("anonymous");
    operationLog.setUsername(username);

    // 方法信息
    final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    final Method method = signature.getMethod();
    final OpLog annotation = method.getAnnotation(OpLog.class);
    if (annotation == null) {
      throw new RuntimeException("未使用操作日志注解");
    }
    final String message = annotation.value();
    operationLog.setMessage(message);
    final String className = joinPoint.getTarget().getClass().getName();
    final String methodName = signature.getName();
    final String qualifiedMethod = StrUtil.format("{}.{}()", className, methodName);
    operationLog.setMethod(qualifiedMethod);
    final Map<String, Object> paramMap = Optional.ofNullable(joinPoint.getArgs())
      .map(args -> {
        final Object[] values = Arrays.stream(args)
          .map(arg -> {
            if (arg instanceof MultipartFile) {
              return ((MultipartFile) arg).getOriginalFilename();
            }
            return arg;
          })
          .toArray();
        final String[] keys = signature.getParameterNames();
        return ArrayUtil.zip(keys, values, true);
      })
      .orElse(new HashMap<>());
    final Jackson2ObjectMapperBuilder jsonBuilder = Jackson2ObjectMapperBuilder.json();
    jackson2ObjectMapperBuilderCustomizer.customize(jsonBuilder);
    final ObjectMapper objectMapper = jsonBuilder.build().setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
      @Override
      public boolean hasIgnoreMarker(final AnnotatedMember m) {
        final String ignores = annotation.ignores();
        final List<String> exclusions = StrSplitter.split(ignores, ",", 0, true, true);
        return exclusions.contains(m.getName()) || super.hasIgnoreMarker(m);
      }
    });
    final String paramJson = objectMapper.writeValueAsString(paramMap);
    operationLog.setParams(paramJson);

    // 打印控制台
    log.info(
      "{} [{}] {} -> {}, method: {}, params: {}",
      operationLog.getUsername(),
      operationLog.getRequestIp(),
      operationLog.getMessage(),
      operationLog.getEndPoint(),
      operationLog.getMethod(),
      operationLog.getParams()
    );

    // 保存数据库
    Console.log(operationLog);
  }
}
