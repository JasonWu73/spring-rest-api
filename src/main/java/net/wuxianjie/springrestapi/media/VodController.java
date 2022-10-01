package net.wuxianjie.springrestapi.media;

import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.shared.exception.ApiException;
import net.wuxianjie.springrestapi.shared.operationlog.core.Log;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class VodController {

  private final VodService vodService;

  /**
   * 音视频点播.
   *
   * @param request HttpServletRequest
   * @param headers HttpHeaders
   * @return <pre>{@code
   * 音频/视频
   * }</pre>
   */
  @GetMapping("/vod/**")
  public ResponseEntity<ResourceRegion> vod(
    final HttpServletRequest request,
    @RequestHeader final HttpHeaders headers
  ) {
    return vodService.vod(request, headers);
  }

  /**
   * 下载点播音视频.
   *
   * @param request HttpServletRequest
   * @return <pre>{@code
   * 下载音频/视频
   * }</pre>
   */
  @GetMapping("/dl/**")
  public ResponseEntity<Resource> download(final HttpServletRequest request) {
    return vodService.download(request);
  }

  /**
   * 获取点播音视频列表.
   *
   * @return <pre>{@code
   * [
   *   {
   *     "filename": "sample.mp3", // 文件名
   *     "vodPath": "http://192.168.2.42:8090/vod/点播目录/sample.mp3", // 点播地址
   *     "download": "http://192.168.2.42:8090/dl/点播目录/sample.mp3" // 下载地址
   *   }
   * ]
   * }</pre>
   */
  @GetMapping("/api/v1/vod")
  public ResponseEntity<List<LinkedHashMap<String, Object>>> getVodList() {
    return vodService.getVodList();
  }

  /**
   * 新增点播音视频.
   *
   * @param file 文件, 必填, 大小 <= 1G, 仅支持 MP3 及 MP4 格式, 文件名不能包含 {@code \ / : * ? " < > |} 字符
   * @return <pre>{@code
   * }</pre>
   */
  @Log("新增点播音视频")
  @PostMapping(value = "/api/v1/vod", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Void> addVod(@RequestPart MultipartFile file) {
    if (file.isEmpty()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "上传文件不能为空");
    }
    return vodService.addVod(file);
  }

  /**
   * 删除点播音视频.
   *
   * @param filename {@code /api/v1/vod/{filename}}
   * <p>文件名, 不能包含 \ / : * ? " < > | 字符, 且必须以 .mp3 或 .mp4 结尾
   * @return <pre>{@code
   * }</pre>
   */
  @Log("删除纪律播报")
  @DeleteMapping("/api/v1/vod/{filename}")
  public ResponseEntity<Void> deleteVod(@PathVariable final String filename) {
    return vodService.deleteVod(filename);
  }
}
