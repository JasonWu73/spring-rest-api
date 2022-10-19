package net.wuxianjie.springrestapi.shared.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
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
import ws.schild.jave.info.MultimediaInfo;

import java.io.File;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileUtils {

  public static String getJarDirAbsoluteFilePath() {
    return new ApplicationHome(FileUtils.class).getDir().getAbsolutePath() + "/";
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

  public static void toMp3(final File source, final File targetMp3) {
    // https://blog.csdn.net/l42606525/article/details/100743844
    try {
      final MultimediaObject multimediaObject = new MultimediaObject(source);

      // 需要转换的音频属性
      final AudioAttributes audio = new AudioAttributes();
      // 音频编码器: MP3
      audio.setCodec("libmp3lame");
      // 比特率: 64kbit/s is 64000
      audio.setBitRate(64000);
      // 声道: 立体声
      audio.setChannels(2);
      // 采样率: 使用源文件音频流采样率
      audio.setSamplingRate(multimediaObject.getInfo().getAudio().getSamplingRate());

      // 编码属性
      final EncodingAttributes attrs = new EncodingAttributes();
      // 设置输出格式
      attrs.setOutputFormat("mp3");
      // 设置音频参数
      attrs.setAudioAttributes(audio);

      // 开始编码
      final Encoder encoder = new Encoder();
      encoder.encode(multimediaObject, targetMp3, attrs);
    } catch (Exception e) {
      log.error("文件转换失败 [{} -> {}]", source.getAbsolutePath(), targetMp3.getAbsolutePath(), e);
    }
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
}
