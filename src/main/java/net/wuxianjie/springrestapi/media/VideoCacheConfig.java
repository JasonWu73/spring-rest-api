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
    final long timeoutMilliseconds = 24 * 60 * 60 * 1000;
    final TimedCache<String, String> filePathToAbsolute = CacheUtil.newTimedCache(timeoutMilliseconds);
    filePathToAbsolute.schedulePrune(timeoutMilliseconds);
    return filePathToAbsolute;
  }
}
