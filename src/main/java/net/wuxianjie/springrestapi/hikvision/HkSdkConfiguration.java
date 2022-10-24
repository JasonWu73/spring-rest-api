package net.wuxianjie.springrestapi.hikvision;

import com.sun.jna.Native;
import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.springrestapi.shared.util.FileUtils;
import net.wuxianjie.springrestapi.shared.util.OsUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import javax.annotation.PreDestroy;
import java.util.Optional;

@Slf4j
@Configuration
public class HkSdkConfiguration {

  private Optional<HCNetSDK> sdkOpt;

  @Bean
  @EventListener(ApplicationReadyEvent.class)
  public Optional<HCNetSDK> initSdkWhenStartup() {
    if (!OsUtils.isWindows()) return Optional.empty();

    // 加载海康 SDK 动态库
    sdkOpt = createSdkInstance();

    // SDK 初始化, 一个程序只需要调用一次
    init();
    return sdkOpt;
  }

  @PreDestroy
  public void destroySdk() {
    // SDK 反初始化, 释放资源, 只需要退出时调用一次
    sdkOpt.ifPresent(HCNetSDK::NET_DVR_Cleanup);
  }

  private static Optional<HCNetSDK> createSdkInstance() {
    final String dll = FileUtils.getAppDirAbsolutePath() + "hk_sdk\\HCNetSDK.dll";
    try {
      final HCNetSDK hcNetSDK = (HCNetSDK) Native.loadLibrary(dll, HCNetSDK.class);
      return Optional.of(hcNetSDK);
    } catch (Exception e) {
      log.error("加载 SDK 动态库失败 [{}]", dll, e);
      return Optional.empty();
    }
  }

  private void init() {
    if (sdkOpt.isEmpty()) return;

    // SDK 初始化, 一个程序只需要调用一次
    final boolean isOk = sdkOpt.get().NET_DVR_Init();
    if (!isOk) {
      log.error("SDK 初始化失败");
      return;
    }

    // 设置异常消息回调
    if (!sdkOpt.get().NET_DVR_SetExceptionCallBack_V30(
      0,
      0,
      (dwType, userId, handle, pointer) -> log.error("SDK 异常事件类型: {}", dwType),
      null
    )) {
      log.error("设置 SDK 异常消息回调失败");
    }

    // 启动 SDK 写日志, 只输出 ERROR 错误日志
    sdkOpt.get().NET_DVR_SetLogToFile(
      1,
      FileUtils.getAppDirAbsolutePath() + "logs",
      true
    );
  }
}
