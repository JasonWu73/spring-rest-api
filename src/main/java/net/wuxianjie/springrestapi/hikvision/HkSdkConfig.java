package net.wuxianjie.springrestapi.hikvision;

import com.sun.jna.Native;
import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.springrestapi.shared.util.FileUtils;
import net.wuxianjie.springrestapi.shared.util.OsUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import javax.annotation.PreDestroy;

@Slf4j
@Configuration
public class HkSdkConfig {

  static HCNetSDK HC_NET_SDK;

  @EventListener(ApplicationReadyEvent.class)
  public HCNetSDK initSdkWhenStartup() {
    if (!OsUtils.isWindows()) {
      log.warn("非 Windows 系统, 暂不支持加载海康 SDK");
      return null;
    }

    // 加载海康 SDK 动态库
    HC_NET_SDK = createSdkInstance();
    if (HC_NET_SDK == null) return null;

    // SDK 初始化, 一个程序只需要调用一次
    init();
    return HC_NET_SDK;
  }

  @PreDestroy
  public void destroySdk() {
    // SDK 反初始化, 释放资源, 只需要退出时调用一次
    if (HC_NET_SDK == null) return;

    HC_NET_SDK.NET_DVR_Cleanup();
  }

  private static HCNetSDK createSdkInstance() {
    final String dll = FileUtils.getAppDirAbsolutePath() + "hk_sdk\\HCNetSDK.dll";
    try {
      return (HCNetSDK) Native.loadLibrary(dll, HCNetSDK.class);
    } catch (Exception e) {
      log.error("加载海康 SDK 动态库失败 [{}]", dll, e);
      return null;
    }
  }

  private void init() {
    // SDK 初始化, 一个程序只需要调用一次
    final boolean isOk = HC_NET_SDK.NET_DVR_Init();
    if (!isOk) {
      log.error("海康 SDK 初始化失败");
      return;
    }

    // 设置异常消息回调
    if (!HC_NET_SDK.NET_DVR_SetExceptionCallBack_V30(
      0,
      0,
      (dwType, userId, handle, pointer) -> log.error("海康 SDK 异常事件类型: {}", dwType),
      null
    )) {
      log.error("设置海康 SDK 异常消息回调失败");
    }

    // 启动 SDK 写日志, 只输出 ERROR 错误日志
    HC_NET_SDK.NET_DVR_SetLogToFile(
      1,
      FileUtils.getAppDirAbsolutePath() + "logs",
      true
    );
  }
}
