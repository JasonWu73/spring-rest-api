package net.wuxianjie.springrestapi.media;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FfmpegComponent {

  public void extractMp3(final String sourceAbsoluteFilePath, final String targetMp3AbsoluteFilePath) {
    // `ffmpeg -y -i trial.mp4 trial.mp3`
    final String command = StrUtil.format(
      "{} -y -i {} {}",
      "ffmpeg",
      "file:" + sourceAbsoluteFilePath,
      "file:" + targetMp3AbsoluteFilePath
    );
    try {
      final Process process = Runtime.getRuntime().exec(command);
      final int signal = process.waitFor();

      if (signal != 0) {
        log.error("音频提取失败 [{}], Exit Code: {}", command, signal);
      }
    } catch (Exception e) {
      log.error("音频提取失败 [{}]", command, e);
    }
  }
}
