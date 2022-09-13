package net.wuxianjie.springrestapi.shared.security.config;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import net.wuxianjie.springrestapi.shared.security.dto.CachedToken;
import net.wuxianjie.springrestapi.shared.security.service.JwtTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenCacheConfig {

  @Bean
  public TimedCache<String, CachedToken> usernameToToken() {
    // 创建缓存，默认 N 毫秒过期（与 JWT 中过期时间一致）
    final int timeoutMilliseconds = JwtTokenService.EXPIRES_IN_SECONDS * 1000;
    final TimedCache<String, CachedToken> usernameToToken = CacheUtil.newTimedCache(timeoutMilliseconds);

    // 启动定时任务，每 N 毫秒（与 JWT 中过期时间一致）清理一次过期条目，注释此行首次启动仍会清理过期条目
    usernameToToken.schedulePrune(timeoutMilliseconds);

    return usernameToToken;
  }
}
