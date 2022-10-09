package net.wuxianjie.springrestapi.shared.security.core;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenCacheConfig {

  @Bean
  public TimedCache<String, CachedToken> usernameToToken() {
    // 创建定时缓存, 设置过期时间为 JWT 有效时间
    final int timeoutMilliseconds = JwtTokenService.EXPIRES_IN_SECONDS * 1000;
    final TimedCache<String, CachedToken> usernameToToken = CacheUtil.newTimedCache(timeoutMilliseconds);

    // 启动定时任务, 每到过期时间时清理一次过期条目, 注释此行首次启动仍会清理过期条目
    usernameToToken.schedulePrune(timeoutMilliseconds);
    return usernameToToken;
  }
}
