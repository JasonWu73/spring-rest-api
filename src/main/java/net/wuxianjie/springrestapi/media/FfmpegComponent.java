package net.wuxianjie.springrestapi.media;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FfmpegComponent {

  public void extractMp3(final String sourceFilePath, final String targetFilePath) {

    // 提取音频时偶尔会无法退出 FFmpeg 进程, 故采用异步执行, 且超时后销毁进程
    ThreadUtil.execAsync(() -> {
      // `ffmpeg -y -i trial.mp4 trial.mp3`
      final String command = StrUtil.format(
        "{} -y -i {} {}",
        "ffmpeg",
        "file:" + sourceFilePath,
        "file:" + targetFilePath
      );
      final Process ffmpeg = RuntimeUtil.exec(command);
      waitForTimeout(ffmpeg, command);
    });
  }

  private void waitForTimeout(final Process process, final String command) {
    // 5 分钟
    long timeoutMillis = 300_000;
    do {
      // 每 30 秒检查一次
      final int sleepMillis = 30_000;
      ThreadUtil.sleep(sleepMillis);
      timeoutMillis -= sleepMillis;

      if (!process.isAlive()) {
        return;
      }
    } while (timeoutMillis > 0);

    RuntimeUtil.destroy(process);
    log.error("音频提取超时 [{}]", command);
  }
}
