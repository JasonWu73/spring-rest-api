package net.wuxianjie.springrestapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator;

@Slf4j
@SpringBootApplication
public class SpringRestApiApplication {

  @EventListener(ApplicationReadyEvent.class)
  public void onStartup() {
    // 测试运行一下 Jave2, 以便在系统未配置 ffmpeg 时将 ffmpeg 文件提取至系统临时目录 (C:\Windows\Temp\jave)
    defaultFfmpegLocatorTest();
  }

  public static void main(final String[] args) {
    SpringApplication.run(SpringRestApiApplication.class, args);
  }

  private void defaultFfmpegLocatorTest() {
    final DefaultFFMPEGLocator locator = new DefaultFFMPEGLocator();
    final String exePath = locator.getExecutablePath();
    log.info("ffmpeg 位于 \"{}\"", exePath);
  }
}
