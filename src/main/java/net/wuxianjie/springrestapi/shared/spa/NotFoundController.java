package net.wuxianjie.springrestapi.shared.spa;

import net.wuxianjie.springrestapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class NotFoundController {

  @RequestMapping("/api/v1/version")
  public ResponseEntity<Map<String, Object>> getVersion() {
    return ResponseEntity.ok(new LinkedHashMap<>() {{
      put("version", "v1.0.0");
      put("name", "REST API 项目");
    }});
  }

  @RequestMapping(value = "/404", produces = MediaType.TEXT_HTML_VALUE)
  public ModelAndView errorHtml(final HttpServletResponse response) {
    // 单页页应用程序 404 应由前端路由自行提供，后端返回 200 即可
    response.setStatus(HttpStatus.OK.value());
    // Spring Boot 默认会将 `src/main/resources/static/` 中的内容作为静态资源提供
    //noinspection SpringMVCViewInspection
    return new ModelAndView("index.html");
  }

  @RequestMapping("/404")
  public ResponseEntity<Map<String, Object>> error() {
    throw new ApiException(HttpStatus.NOT_FOUND, "API 不存在");
  }
}
