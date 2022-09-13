package net.wuxianjie.springrestapi.shared.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NetUtils {

  /**
   * 获取客户端请求的真实 IP 地址。
   *
   * <p>可能会返回形如 {@code 231.23.45.65, 10.20.10.33, 10.20.20.34} 的字符串，分别代表：客户端 IP，负载均衡服务器，反向代理服务器。
   *
   * @return 客户端的真实 IP 地址
   */
  public static String getRealIpAddress(final HttpServletRequest request) {
    final String header = request.getHeader("X-FORWARDED-FOR");
    if (header == null) {
      return request.getRemoteAddr();
    }
    return header.trim();
  }

  /**
   * 获取 Servlet 环境中当前线程中的请求对象。
   *
   * @return HTTP Servlet 请求对象
   */
  public static Optional<HttpServletRequest> getHttpServletRequest() {
    return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
      .map(attr -> (HttpServletRequest) attr.resolveReference(RequestAttributes.REFERENCE_REQUEST));
  }
}
