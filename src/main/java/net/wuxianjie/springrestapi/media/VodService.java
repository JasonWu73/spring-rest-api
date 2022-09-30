package net.wuxianjie.springrestapi.media;

import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.shared.exception.ApiException;
import net.wuxianjie.springrestapi.shared.util.FileUtils;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VodService {

  private static final int MINIMUM_CONTENT_LENGTH_BYTES = 1024 * 1024;

  private final TimedCache<String, String> filePathToAbsolute;
  private final MediaMapper mediaMapper;

  public ResponseEntity<ResourceRegion> vod(final HttpServletRequest request, final HttpHeaders headers) {
    // 获取点播音视频文件的相对路径, 即 ** 部分的地址:
    // http://127.0.0.1:8090/vod/%E6%B5%8B%E8%AF%95/sample.mp4
    // ->
    // %E6%B5%8B%E8%AF%95/sample.mp4
    final String originalFilePath = new AntPathMatcher().extractPathWithinPattern(
      request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString(),
      request.getRequestURI()
    );
    final String encodedFilePath = StrUtil.trimToNull(originalFilePath);

    // 文件格式校验
    if (encodedFilePath == null || !StrUtil.endWithAny(encodedFilePath, ".mp4", ".mp3")) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "文件格式错误");
    }

    // 解码 URL 编码, 以正确识别中文字符
    // %E6%B5%8B%E8%AF%95/sample.mp4
    // ->
    // 测试/sample.mp4
    final String filePath = URLDecoder.decode(encodedFilePath, StandardCharsets.UTF_8);

    // 缓存文件路径以免频繁读取数据库, 从而影响数据库性能
    String absoluteFilePath = filePathToAbsolute.get(filePath);
    if (absoluteFilePath == null) {
      // 若于 Jar 包的相对路径存在文件, 则添加文件路径缓存并返回点播数据
      final String jarDirPath = FileUtils.getJarDirAbsoluteFilePath();
      absoluteFilePath = jarDirPath + filePath;
      if (FileUtil.exist(absoluteFilePath)) {
        filePathToAbsolute.put(filePath, absoluteFilePath);
        return buildPlayOnDemand(absoluteFilePath, headers);
      }

      // 若于 Jar 相对路径找不到文件, 则查询数据库
      absoluteFilePath = mediaMapper.selectFilePathByFilePathLike("%" + filePath);
    }
    if (StrUtil.isEmpty(absoluteFilePath)) {
      throw new ApiException(HttpStatus.NOT_FOUND, "记录不存在");
    }

    // 若在磁盘中找不到文件, 则删除缓存条目
    if (!FileUtil.exist(absoluteFilePath)) {
      filePathToAbsolute.remove(filePath);
      throw new ApiException(HttpStatus.NOT_FOUND, "文件不存在");
    }

    // 添加文件路径缓存并返回点播数据
    filePathToAbsolute.put(filePath, absoluteFilePath);
    return buildPlayOnDemand(absoluteFilePath, headers);
  }

  private ResponseEntity<ResourceRegion> buildPlayOnDemand(
    final String absoluteFilePath,
    final HttpHeaders headers
  ) {
    // 获取磁盘中的文件资源
    final UrlResource resource;
    try {
      resource = new UrlResource("file:" + absoluteFilePath);
    } catch (MalformedURLException e) {
      throw new RuntimeException(StrUtil.format("文件地址不合法 [{}]", absoluteFilePath), e);
    }

    // 检查请求是否指定了所需返回的数据大小范围
    final Optional<HttpRange> rangeOpt = headers.getRange().stream().findFirst();
    final long fileSizeByes = FileUtil.size(new File(absoluteFilePath));
    final ResourceRegion resourceRegion;
    if (rangeOpt.isPresent()) {
      final HttpRange range = rangeOpt.get();
      final long start = range.getRangeStart(fileSizeByes);
      final long end = range.getRangeEnd(fileSizeByes);
      final long length = Math.min(MINIMUM_CONTENT_LENGTH_BYTES, end - start + 1);
      resourceRegion = new ResourceRegion(resource, start, length);
    } else {
      final long length = Math.min(MINIMUM_CONTENT_LENGTH_BYTES, fileSizeByes);
      resourceRegion = new ResourceRegion(resource, 0, length);
    }

    // 点播 HTTP Status 206
    return ResponseEntity
      .status(HttpStatus.PARTIAL_CONTENT)
      .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
      .body(resourceRegion);
  }
}
