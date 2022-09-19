package net.wuxianjie.springrestapi.shared.spa;

import net.wuxianjie.springrestapi.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
public class NotFoundController {

  @RequestMapping(value = "/404", produces = MediaType.TEXT_HTML_VALUE)
  public ModelAndView errorHtml() {
    // Spring Boot 默认会将 `src/main/resources/static/` 中的内容作为静态资源提供
    //noinspection SpringMVCViewInspection
    return new ModelAndView("index.html");
  }

  @RequestMapping("/404")
  public ResponseEntity<Map<String, Object>> error() {
    throw new ApiException(HttpStatus.NOT_FOUND, "API 不存在");
  }
}
