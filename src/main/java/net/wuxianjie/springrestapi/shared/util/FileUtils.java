package net.wuxianjie.springrestapi.shared.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.io.resource.Resource;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.springrestapi.shared.exception.ApiException;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.filters.DrawtextFilter;
import ws.schild.jave.filters.helpers.Color;
import ws.schild.jave.info.MultimediaInfo;

import java.io.File;
import java.net.URL;
import java.util.Optional;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileUtils {

  public static String getAppDirAbsolutePath() {
    return new ApplicationHome(FileUtils.class).getDir().getAbsolutePath() + "/";
  }

  public static String appendPath(final String filePath, final String... names) {
    final String filePathWithoutSuffix;
    if (StrUtil.endWith(filePath, "/")) {
      filePathWithoutSuffix = StrUtil.removeSuffix(filePath, "/");
    } else if (StrUtil.endWith(filePath, "\\")) {
      filePathWithoutSuffix = StrUtil.removeSuffix(filePath, "\\");
    } else {
      filePathWithoutSuffix = filePath;
    }
    return StrUtil.join(getFilePathSeparator(), filePathWithoutSuffix, names);
  }

  public static String getFilePathSeparator() {
    return OsUtils.isWindows() ? "\\" : "/";
  }

  public static String getTopmostDirPath(final String filePath) {
    final String parent = FileUtil.getParent(filePath, 1);
    if (parent == null) {
      return filePath;
    }
    return getTopmostDirPath(parent);
  }

  public static String getValidFilename(final MultipartFile file) {
    final String originalFilename = file.getOriginalFilename();
    final String filenameTrimToNull = StrUtil.trimToNull(originalFilename);
    if (filenameTrimToNull == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "文件名不存在");
    }
    final String filename = FileNameUtil.getName(filenameTrimToNull);
    if (FileNameUtil.containsInvalid(filename)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "文件名存在非法字符, 包含: \\ / : * ? \" < > |");
    }
    return filename;
  }

  public static long getMediaTimeLengthInSeconds(final File file, final long defaultLength) {
    final MultimediaObject multimediaObject = new MultimediaObject(file);
    final MultimediaInfo info;
    try {
      info = multimediaObject.getInfo();
    } catch (Exception e) {
      log.warn("无法读取文件时长 [{}]", file.getAbsolutePath(), e);
      return defaultLength;
    }
    return info.getDuration() / 1000;
  }

  public static boolean toMp3(final File source, final File targetMp3) {
    // https://github.com/a-schild/jave2/wiki/Examples
    try {
      // 需要转换的音频属性
      final AudioAttributes audio = new AudioAttributes();
      // 音频编码器: MP3
      audio.setCodec("libmp3lame");
      // 比特率: 128 kbit/s
      audio.setBitRate(128000);
      // 采样率: 44100 Hz
      audio.setSamplingRate(44100);
      // 声道: 立体声
      audio.setChannels(2);

      // 编码属性
      final EncodingAttributes attrs = new EncodingAttributes();
      // 设置输出格式
      attrs.setOutputFormat("mp3");
      // 设置音频参数
      attrs.setAudioAttributes(audio);

      // 开始编码
      final Encoder encoder = new Encoder();
      encoder.encode(new MultimediaObject(source), targetMp3, attrs);
    } catch (Exception e) {
      log.error("文件转换失败 [{} -> {}]", source.getAbsolutePath(), targetMp3.getAbsolutePath(), e);
      return false;
    }
    return true;
  }

  public static boolean addWatermarkTextToVideo(final File source, final File target, final String text) {
    try {
      // 需要转换的音频属性
      final AudioAttributes audio = new AudioAttributes();
      // 音频编码器
      audio.setCodec(AudioAttributes.DIRECT_STREAM_COPY);

      // 需要转换的视频属性
      final VideoAttributes video = new VideoAttributes();
      // 文本水印
      // https://stackoverflow.com/questions/38726370/ffmpeg-text-watermark-bottom-left
      // ffmpeg -i "C:\test.mp4"
      // -vf "drawtext=text='这里是文字描述':x=10:y=H-th-10:
      //                fontfile=Songti.ttf:fontsize=18:fontcolor=white:
      //                shadowcolor=black:shadowx=5:shadowy=5"
      // "C:\test-watermark.mp4"
      File font = getFontFile();
      final DrawtextFilter drawtextFilter = new DrawtextFilter(
        text,
        "10",
        "10",
        font,
        55.0,
        new Color("FFFFFF")
      );
      drawtextFilter.setShadow(new Color("000000"), 1, 1);
      drawtextFilter.setLineSpacing(10);
      video.addFilter(drawtextFilter);
      // 视频编码器, 添加水印不能使用 copy, 推荐使用 H264
      video.setCodec("libx264");

      // 编码属性
      final EncodingAttributes attrs = new EncodingAttributes();
      // 设置输出格式
      attrs.setOutputFormat("mp4");
      // 设置音频参数
      attrs.setAudioAttributes(audio);
      // 设置视频参数
      attrs.setVideoAttributes(video);

      // 开始编码
      final Encoder encoder = new Encoder();
      encoder.encode(new MultimediaObject(source), target, attrs);
    } catch (Exception e) {
      log.error("视频添加文字水印失败 [{} -> {}]", source.getAbsolutePath(), target.getAbsolutePath(), e);
      return false;
    }
    return true;
  }

  public static boolean isMp3(final MultipartFile file) {
    final String contentType = file.getContentType();
    final boolean isMp3 = StrUtil.equalsAnyIgnoreCase(contentType, "audio/mpeg");
    if (!isMp3) {
      log.warn("非 MP3 MIME-Type [{}]", contentType);
    }
    return isMp3;
  }

  public static boolean isMp4(final MultipartFile file) {
    final String contentType = file.getContentType();
    final boolean isMp4 = StrUtil.equalsAnyIgnoreCase(contentType, "video/mp4");
    if (!isMp4) {
      log.warn("非 MP4 MIME-Type [{}]", contentType);
    }
    return isMp4;
  }

  private static File getFontFile() {
    final Resource resourceObj = ResourceUtil.getResourceObj("classpath:/drawtext/Songti.ttc");
    final URL url = Optional.ofNullable(resourceObj.getUrl())
      .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "字体文件不存在", false));
    return new File(url.getFile());
  }
}
