package net.wuxianjie.springrestapi.shared.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.wuxianjie.springrestapi.shared.security.core.TokenDetails;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.apache.tomcat.util.buf.MessageBytes;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServletUtils {

  public static Optional<HttpServletRequest> getHttpServletRequest() {
    return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
      .map(attr -> (HttpServletRequest) attr.resolveReference(RequestAttributes.REFERENCE_REQUEST));
  }

  public static String getClientInfo() {
    final HttpServletRequest req = ServletUtils.getHttpServletRequest().orElseThrow();
    String username = ApiUtils.getAuthentication().map(TokenDetails::getUsername).orElse(null);
    return StrUtil.format(
      "api=[{} {}];client={};user={}",
      req.getMethod(),
      req.getRequestURI(),
      ServletUtil.getClientIP(req),
      username
    );
  }

  /**
   * 获取原始请求信息.
   *
   * @return <pre>{@code
   * {
   *   "uri": "/api/test-sdf",
   *   "method": "POST"
   * }
   * }</pre>
   */
  public static Map<String, String> getOriginalRequestInfo() {
    final HttpServletRequest req = getHttpServletRequest().orElseThrow();
    try {
      final Field reqWrapField = ServletRequestWrapper.class.getDeclaredField("request");
      reqWrapField.setAccessible(true);
      final HttpServletRequest firewalledReq = (HttpServletRequest) reqWrapField.get(req);

      final Field appReqField = ServletRequestWrapper.class.getDeclaredField("request");
      appReqField.setAccessible(true);
      final HttpServletRequest appReq = (HttpServletRequest) appReqField.get(firewalledReq);

      final Field reqFacadeField = ServletRequestWrapper.class.getDeclaredField("request");
      reqFacadeField.setAccessible(true);
      final RequestFacade reqFacade = (RequestFacade) reqFacadeField.get(appReq);

      final Field reqFacadeField2 = RequestFacade.class.getDeclaredField("request");
      reqFacadeField2.setAccessible(true);
      final Request connReq = (Request) reqFacadeField2.get(reqFacade);

      final Field coyoteReqField = Request.class.getDeclaredField("coyoteRequest");
      coyoteReqField.setAccessible(true);
      final org.apache.coyote.Request coyoteReq = (org.apache.coyote.Request) coyoteReqField.get(connReq);

      final Field uriMbField = org.apache.coyote.Request.class.getDeclaredField("uriMB");
      uriMbField.setAccessible(true);
      final MessageBytes uriMb = (MessageBytes) uriMbField.get(coyoteReq);

      final Field methodMbField = org.apache.coyote.Request.class.getDeclaredField("methodMB");
      methodMbField.setAccessible(true);
      final MessageBytes methodMb = (MessageBytes) methodMbField.get(coyoteReq);

      return new HashMap<>() {{
        put("uri", uriMb.getString());
        put("method", methodMb.getString());
      }};
    } catch (NoSuchFieldException | IllegalAccessException e) {
      return new HashMap<>() {{
        put("uri", req.getRequestURI());
        put("method", req.getMethod());
      }};
    }
  }
}
