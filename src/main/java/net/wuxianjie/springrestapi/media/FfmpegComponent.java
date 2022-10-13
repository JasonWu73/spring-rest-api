package net.wuxianjie.springrestapi.media;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class FfmpegComponent {

  public void extractMp3(final String sourceFilePath, final String targetFilePath) {
    ThreadUtil.execAsync(() -> {
      // `ffmpeg -y -i trial.mp4 trial.mp3`
      final String command = StrUtil.format(
        "{} -y -i {} {}",
        "ffmpeg",
        "file:" + sourceFilePath,
        "file:" + targetFilePath
      );

      try {
        final Process process = Runtime.getRuntime().exec(command);
        final boolean isExited = process.waitFor(10, TimeUnit.MINUTES);
        if (!isExited) {
          process.destroy();
          log.error("音频可能提取失败 [{}]", command);
        }
      } catch (Exception e) {
        log.error("音频提取失败 [{}]", command, e);
      }
    });
  }
}
