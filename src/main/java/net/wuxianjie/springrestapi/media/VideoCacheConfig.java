package net.wuxianjie.springrestapi.media;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VideoCacheConfig {

  /**
   * @return {@code {"文件相对路径": "文件绝对路径"}}
   */
  @Bean
  public TimedCache<String, String> filePathToAbsolute() {
    // 创建定时缓存, 设置过期时间为 24 小时
    final long timeoutMilliseconds = 24 * 60 * 60 * 1000;
    final TimedCache<String, String> filePathToAbsolute = CacheUtil.newTimedCache(timeoutMilliseconds);

    // 启动定时任务, 每 24 小时清理一次过期条目, 注释此行首次启动仍会清理过期条目
    filePathToAbsolute.schedulePrune(timeoutMilliseconds);
    return filePathToAbsolute;
  }
}
