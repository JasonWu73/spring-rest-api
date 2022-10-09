package net.wuxianjie.springrestapi.media;

import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import net.wuxianjie.springrestapi.shared.exception.ApiException;
import net.wuxianjie.springrestapi.shared.util.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VodService {

  @Value("${server.port}")
  private int serverPort;

  private static final int MINIMUM_CONTENT_LENGTH_BYTES = 1024 * 1024;
  private static final String VOD_DIR = "点播目录/";

  private final TimedCache<String, String> filePathToAbsolute;
  private final MediaMapper mediaMapper;

  public ResponseEntity<ResourceRegion> vod(final HttpServletRequest request, final HttpHeaders headers) {
    // 获取文件的相对路径
    final String filePath = getFilePath(request);

    // 缓存文件路径以免频繁读取数据库, 从而影响数据库性能
    String absoluteFilePath = filePathToAbsolute.get(filePath);
    if (absoluteFilePath == null) {
      // 若于 Jar 包的相对路径存在文件, 则添加文件路径缓存并返回点播数据
      absoluteFilePath = toAbsoluteFilePath(filePath);
      if (FileUtil.exist(absoluteFilePath)) {
        filePathToAbsolute.put(filePath, absoluteFilePath);
        return buildPlayOnDemand(absoluteFilePath, headers);
      }

      // 若于 Jar 相对路径找不到文件, 则查询数据库
      absoluteFilePath = getAbsoluteFilePath(filePath);
    }

    // 若在磁盘中找不到文件, 则删除缓存条目
    checkForFileExists(filePath, absoluteFilePath);

    // 添加文件路径缓存并返回点播数据
    filePathToAbsolute.put(filePath, absoluteFilePath);
    return buildPlayOnDemand(absoluteFilePath, headers);
  }

  public ResponseEntity<Resource> download(final HttpServletRequest request) {
    // 获取文件的相对路径
    final String filePath = getFilePath(request);

    // 获取文件的绝对路径
    String absoluteFilePath = filePathToAbsolute.get(filePath);
    if (absoluteFilePath == null) {
      // 若于 Jar 包的相对路径存在文件, 则添加文件路径缓存并返回数据
      absoluteFilePath = toAbsoluteFilePath(filePath);
      if (FileUtil.exist(absoluteFilePath)) {
        filePathToAbsolute.put(filePath, absoluteFilePath);
        return buildDownload(absoluteFilePath);
      }

      // 若于 Jar 相对路径找不到文件, 则查询数据库
      absoluteFilePath = getAbsoluteFilePath(filePath);
    }

    // 若在磁盘中找不到文件, 则删除缓存条目
    checkForFileExists(filePath, absoluteFilePath);

    // 添加文件路径缓存并返回数据
    filePathToAbsolute.put(filePath, absoluteFilePath);
    return buildDownload(absoluteFilePath);
  }

  public ResponseEntity<List<LinkedHashMap<String, Object>>> getVodList() {
    // 于 Jar 的相对目录中读取所有点播文件
    final String vodDir = getVodDirAbsoluteFilePath();
    if (!FileUtil.exist(vodDir)) {
      throw new ApiException(HttpStatus.NOT_FOUND, "点播目录不存在");
    }
    final List<String> filenames = FileUtil.listFileNames(vodDir);

    // 构造点播和下载地址
    final List<LinkedHashMap<String, Object>> result = new ArrayList<>();
    for (final String filename : filenames) {
      final LinkedHashMap<String, Object> item = new LinkedHashMap<>();
      item.put("filename", filename);
      final String aodPath = StrUtil.format(
        "http://{}:{}/{}",
        "127.0.0.1",
        serverPort,
        "vod/" + VOD_DIR + filename
      );
      item.put("vodPath", aodPath);
      final String download = StrUtil.replaceFirst(aodPath, "vod", "dl");
      item.put("download", download);
      result.add(item);
    }
    return ResponseEntity.ok(result);
  }

  public ResponseEntity<Void> addVod(final MultipartFile file) {
    // 文件名校验, 不能包含在 Windows 下不支持的非法字符, 包括: \ / : * ? " < > |
    final String filename = FileUtils.getValidFilename(file);

    // 仅支持 MP3 或 MP4
    if (isNotMp3Mp4(filename)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "仅支持 MP3 及 MP4 文件");
    }

    // 于 Jar 的相对目录中保存文件
    final String vodDir = getVodDirAbsoluteFilePath();
    final String absoluteFilePath = vodDir + "/" + filename;
    if (FileUtil.exist(absoluteFilePath)) {
      throw new ApiException(HttpStatus.CONFLICT, "已存在同名文件");
    }
    final InputStream inputStream;
    try {
      inputStream = file.getInputStream();
    } catch (IOException e) {
      throw new RuntimeException("上传文件输入流获取失败", e);
    }
    FileUtil.writeFromStream(inputStream, absoluteFilePath);
    return ResponseEntity.ok().build();
  }

  public String getVodDirAbsoluteFilePath() {
    return FileUtils.getJarDirAbsoluteFilePath() + VOD_DIR;
  }

  public ResponseEntity<Void> deleteVod(final String filename) {
    // 文件名校验, 不能包含在 Windows 下不支持的非法字符, 包括: \ / : * ? " < > |
    if (FileNameUtil.containsInvalid(filename)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "文件名存在非法字符, 包含: \\ / : * ? \" < > |");
    }

    // 仅支持 MP3 或 MP4
    if (isNotMp3Mp4(filename)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "仅支持 MP3 及 MP4 文件");
    }

    // 于 Jar 的相对目录中删除文件
    final String absoluteFilePath = getVodDirAbsoluteFilePath() + filename;
    if (!FileUtil.exist(absoluteFilePath)) {
      return ResponseEntity.ok().build();
    }
    FileUtil.del(absoluteFilePath);
    return ResponseEntity.ok().build();
  }

  private static String getFilePath(final HttpServletRequest request) {
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
    if (encodedFilePath == null || isNotMp3Mp4(encodedFilePath)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "文件格式错误");
    }

    // 解码 URL 编码, 以正确识别中文字符
    // %E6%B5%8B%E8%AF%95/sample.mp4
    // ->
    // 测试/sample.mp4
    return URLDecoder.decode(encodedFilePath, StandardCharsets.UTF_8);
  }

  private static boolean isNotMp3Mp4(final String filePath) {
    return !StrUtil.endWithAny(filePath, ".mp4", ".mp3");
  }

  private static String toAbsoluteFilePath(final String filePath) {
    // 相对于 Jar 包的文件绝对路径
    final String jarDirPath = FileUtils.getJarDirAbsoluteFilePath();
    return jarDirPath + filePath;
  }

  private String getAbsoluteFilePath(final String filePath) {
    String absoluteFilePath;
    absoluteFilePath = mediaMapper.selectFilePathByFilePathLike("%" + filePath);
    if (StrUtil.isEmpty(absoluteFilePath)) {
      throw new ApiException(HttpStatus.NOT_FOUND, "记录不存在");
    }
    return absoluteFilePath;
  }

  private void checkForFileExists(final String filePath, final String absoluteFilePath) {
    // 若在磁盘中找不到文件, 则删除缓存条目
    if (!FileUtil.exist(absoluteFilePath)) {
      filePathToAbsolute.remove(filePath);
      throw new ApiException(HttpStatus.NOT_FOUND, "文件不存在");
    }
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

  private ResponseEntity<Resource> buildDownload(final String absoluteFilePath) {
    final File file = new File(absoluteFilePath);
    final FileSystemResource resource = new FileSystemResource(file);
    final String fileName = FileUtil.getName(absoluteFilePath);
    // https://segmentfault.com/a/1190000023601065
    final String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
      .replaceAll("\\+", "%20");
    return ResponseEntity
      .ok()
      .header(
        HttpHeaders.CONTENT_DISPOSITION,
        StrUtil.format(
          "attachment;filename={};filename*=UTF-8''{}",
          encodedFileName,
          encodedFileName
        )
      )
      .contentLength(FileUtil.size(file))
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .body(resource);
  }
}
