package net.wuxianjie.springrestapi.media;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class VodController {

  private final VodService vodService;

  /**
   * 音视频点播。
   *
   * @param request HttpServletRequest
   * @param headers HttpHeaders
   * @return <pre>{@code
   * 音频/视频
   * }</pre>
   */
  @GetMapping("/vod/**")
  public ResponseEntity<ResourceRegion> vod(
    HttpServletRequest request,
    @RequestHeader HttpHeaders headers
  ) {
    return vodService.vod(request, headers);
  }
}
