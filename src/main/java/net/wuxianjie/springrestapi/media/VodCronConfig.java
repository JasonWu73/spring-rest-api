package net.wuxianjie.springrestapi.media;

import cn.hutool.core.io.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class VodCronConfig {

  /**
   * 每天凌晨 3 点启动定时任务.
   *
   * @see <a href="https://www.freeformatter.com/cron-expression-generator-quartz.html">Free Online Cron Expression Generator and Describer - FreeFormatter.com</a>
   */
  private static final String EVERY_DAY_3_AM_CRON_EXPRESSION = "0 0 3 * * ?";

  private final VodService vodService;

  @Scheduled(cron = EVERY_DAY_3_AM_CRON_EXPRESSION)
  public void cleanEmptyVodDir() {
    final String vodDirPath = vodService.getVodDirAbsoluteFilePath();
    final boolean isCleaned = FileUtil.cleanEmpty(new File(vodDirPath));
    log.warn("定时清理点播空目录 [{}, {}]", vodDirPath, isCleaned);
  }
}
