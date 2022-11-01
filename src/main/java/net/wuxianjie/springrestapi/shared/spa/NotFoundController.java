package net.wuxianjie.springrestapi.shared.spa;

import cn.hutool.core.io.resource.Resource;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.springrestapi.shared.exception.ApiException;
import net.wuxianjie.springrestapi.shared.util.ApiUtils;
import net.wuxianjie.springrestapi.shared.util.ServletUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Controller
public class NotFoundController {

  @RequestMapping("/404")
  public ResponseEntity<?> errorHtml(
    final HttpServletRequest request,
    final HttpServletResponse response
  ) {
    final HttpStatus status = HttpStatus.NOT_FOUND;
    final ApiException apiExc = new ApiException(status, "资源不存在");
    final Map<String, String> origReq = ServletUtils.getOriginalRequestInfo(); // 原始请求地址
    logWarn(origReq, apiExc);

    // 当请求 JSON 时返回 JSON, 否则都返回 HTML
    final String origReqUri = origReq.get("uri");
    if (isJsonRequest(request, origReqUri)) {
      final LinkedHashMap<String, Object> error = ApiUtils.error(status, apiExc.getReason());
      error.put("path", origReqUri);
      return ResponseEntity
        .status(status)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(error);
    }

    // 单页面应用程序 404 应由前端路由自行提供, 后端返回 200 即可
    response.setStatus(HttpStatus.OK.value());
    // Spring Boot 默认会将 `src/main/resources/static/` 中的内容作为静态资源提供
    return ResponseEntity
      .status(HttpStatus.OK)
      .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
      .body(getHtml());
  }

  private void logWarn(final Map<String, String> origReq, final ApiException apiExc) {
    final String clientInfo = getClientInfo(origReq);
    log.warn("{} -> {}", clientInfo, apiExc.getMessage());
  }

  private boolean isJsonRequest(final HttpServletRequest request, final String origReqUri) {
    final String accept = request.getHeader(HttpHeaders.ACCEPT);
    if (StrUtil.containsIgnoreCase(accept, "json")) return true;

    return StrUtil.startWith(origReqUri, "/api/");
  }

  private String getClientInfo(final Map<String, String> origReq) {
    final HttpServletRequest req = ServletUtils.getHttpServletRequest().orElseThrow();
    return StrUtil.format(
      "api=[{} {}];client={};user={}",
      origReq.get("method"),
      origReq.get("uri"),
      ServletUtil.getClientIP(req),
      null // 404 发生在进入 Spring Security 身份验证过滤器之前, 故肯定为 null
    );
  }

  private String getHtml() {
    final Resource resourceObj = ResourceUtil.getResourceObj("classpath:/static/index.html");
    return resourceObj.readUtf8Str();
  }
}
