package net.wuxianjie.springrestapi.hikvision;

import cn.hutool.core.util.StrUtil;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import static net.wuxianjie.springrestapi.hikvision.HkSdkConfiguration.HC_NET_SDK;

/**
 * @see <a href="https://blog.csdn.net/lftaoyuan/article/details/108072732">海康OSD添加获取及清除</a>
 */
@Slf4j
@Component
public class HkSdkComponent {

  /**
   * 获取主机状态.
   *
   * @param deviceIp 设备 IP
   * @param devicePort 设备端口号
   * @param username 设备登录用户名
   * @param password 设备登录密码
   * @return 主机状态
   */
  public NvrStatus getNvrStatus(
    final String deviceIp,
    final int devicePort,
    final String username,
    final String password
  ) {
    if (HC_NET_SDK == null) throw new RuntimeException("海康 SDK 初始化失败");

    // 在线状态
    final HCNetSDK.NET_DVR_DEVICEINFO_V40 deviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();// 设备信息
    final int userHandle = login(deviceIp, devicePort, username, password, deviceInfo);
    if (userHandle == -1) return new NvrStatus(false, null, null, null, null);

    try {
      // 音视频状态, 只要设备在线就一定正常
      final NvrStatus status = new NvrStatus(true, null, null, true, true);
      // 录像状态
      final HCNetSDK.NET_DVR_WORKSTATE_V30 workStatus = new HCNetSDK.NET_DVR_WORKSTATE_V30();
      workStatus.write();
      if (!HC_NET_SDK.NET_DVR_GetDVRWorkState_V30(userHandle, workStatus)) {
        log.warn("海康 SDK 获取设备状态失败");
        return status;
      }
      workStatus.read();

      // 通道的状态, 从前往后顺序排列
      // 只要有一个正常就认为录像正常
      // boolean recordStatus = false;
      // for (int i = 0; i < workStatus.struChanStatic.length; i++) {
      //   log.info("{}. 录像状态: {}", i, workStatus.struChanStatic[i].byRecordStatic);
      //   // 通道是否在录像, 0: 不录像, 1: 录像
      //   if (workStatus.struChanStatic[i].byRecordStatic == 1) {
      //     recordStatus = true;
      //     break;
      //   }
      // }
      // status.setRecordStatus(recordStatus);

      // 硬盘状态, 只要有一个不正常就认为硬盘异常
      boolean diskStatus = true;
      // 硬盘状态, 一次最多只能获取 33 个硬盘信息
      for (int i = 0; i < workStatus.struHardDiskStatic.length; i++) {
        log.info("{}. 硬盘状态: {}", i, workStatus.struHardDiskStatic[i].dwHardDiskStatic);
        // 硬盘状态, 0: 正常, 1: 休眠, 2: 不正常, 3: 休眠硬盘出错 (如拆除硬盘)
        final int diskStatusCode = workStatus.struHardDiskStatic[i].dwHardDiskStatic;
        if (diskStatusCode != 0 && diskStatusCode != 1) {
          diskStatus = false;
          break;
        }
      }
      status.setDiskStatus(diskStatus);
      // 录像状态直接与硬盘状态同步更为符合实际需求, 因为用户关心的是否录像文件存在于硬盘中
      status.setRecordStatus(diskStatus);

      return status;
    } finally {
      logout(userHandle);
    }
  }

  /**
   * 设置 OSD 字符叠加参数.
   *
   * @param deviceIp 设备 IP
   * @param devicePort 设备端口号
   * @param username 设备登录用户名
   * @param password 设备登录密码
   * @param channel 通道号
   * @param contents 叠加的字符, 如:
   * <pre>{@code
   * new String[]{
   *   "案号：（2022）浙0382刑初790案件",
   *   "庭次：1",
   *   "案由：离婚纠纷",
   *   "法庭：附楼法庭1",
   *   "承办人：杨志敏",
   *   "书记员：陈佳佳",
   *   "原告：沈洪英",
   *   "被告：朱小明"
   * }
   * }</pre>
   */
  public void setOsd(
    final String deviceIp,
    final int devicePort,
    final String username,
    final String password,
    final int channel,
    final String[] contents
  ) {
    if (HC_NET_SDK == null) return;

    if (null == contents || contents.length == 0) return;

    final HCNetSDK.NET_DVR_DEVICEINFO_V40 deviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();// 设备信息
    final int userHandle = login(deviceIp, devicePort, username, password, deviceInfo);
    if (userHandle == -1) return;

    try {
      // 获取叠加字符参数
      final HCNetSDK.NET_DVR_SHOWSTRING_V30 osdCfg = getOsdCfg(userHandle, channel);
      if (osdCfg == null) return;

      int index = 0;
      for (final String content : contents) {
        if (StrUtil.isBlank(content)) continue;

        if (index >= osdCfg.struStringInfo.length) break;

        final byte[] osdBytes;
        try {
          osdBytes = content.getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
          log.error("不支持 GBK 编码 []");
          return;
        }
        osdCfg.struStringInfo[index].wShowString = 1;
        osdCfg.struStringInfo[index].sString = osdBytes;
        osdCfg.struStringInfo[index].wStringSize = (short) osdBytes.length;
        osdCfg.struStringInfo[index].wShowStringTopLeftX = 50;
        osdCfg.struStringInfo[index].wShowStringTopLeftY = (short) (50 + (index * 50));
        index++;
      }
      osdCfg.write();

      setOsdCfg(userHandle, channel, osdCfg);
    } finally {
      logout(userHandle);
    }
  }

  /**
   * 清除 OSD.
   *
   * @param deviceIp 设备 IP
   * @param devicePort 设备端口号
   * @param username 设备登录用户名
   * @param password 设备登录密码
   * @param channel 通道号
   */
  public void clearOsd(
    final String deviceIp,
    final int devicePort,
    final String username,
    final String password,
    final int channel
  ) {
    if (HC_NET_SDK == null) return;

    final HCNetSDK.NET_DVR_DEVICEINFO_V40 deviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();// 设备信息
    final int userHandle = login(deviceIp, devicePort, username, password, deviceInfo);
    if (userHandle == -1) return;

    try {
      // 获取叠加字符参数
      final HCNetSDK.NET_DVR_SHOWSTRING_V30 osdCfg = getOsdCfg(userHandle, channel);
      if (osdCfg == null) return;

      for (int i = 0; i < osdCfg.struStringInfo.length; i++) {
        osdCfg.struStringInfo[i].wShowString = 0;
      }
      osdCfg.write();

      setOsdCfg(userHandle, channel, osdCfg);
    } finally {
      logout(userHandle);
    }
  }

  /**
   * 获取设备序列号.
   *
   * @param deviceIp 设备 IP
   * @param devicePort 设备端口号
   * @param username 设备登录用户名
   * @param password 设备登录密码
   * @return 设备序列号
   */
  public Optional<String> getSn(
    final String deviceIp,
    final int devicePort,
    final String username,
    final String password
  ) {
    if (HC_NET_SDK == null) throw new RuntimeException("海康 SDK 初始化失败");

    // 登录设备, 每一台设备分别登录; 登录句柄是唯一的, 可以区分设备
    final HCNetSDK.NET_DVR_USER_LOGIN_INFO loginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();// 设备登录信息
    final HCNetSDK.NET_DVR_DEVICEINFO_V40 deviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();// 设备信息
    fillInLoginInfo(deviceIp, (short) devicePort, username, password, loginInfo);
    final int userHandle = HC_NET_SDK.NET_DVR_Login_V40(loginInfo, deviceInfo); // 用户句柄
    if (userHandle == -1) {
      log.error("设备登录失败 [IP={};错误码={}]", deviceIp, HC_NET_SDK.NET_DVR_GetLastError());
      return Optional.empty();
    } else {
      deviceInfo.read();
      logDeviceInfo(deviceIp, deviceInfo);

      // 注销
      logout(userHandle);
      return Optional.of(new String(deviceInfo.struDeviceV30.sSerialNumber).trim());
    }
  }

  private int login(
    final String deviceIp,
    final int devicePort,
    final String username,
    final String password,
    final HCNetSDK.NET_DVR_DEVICEINFO_V40 deviceInfo
  ) {
    // 登录设备, 每一台设备分别登录; 登录句柄是唯一的, 可以区分设备
    final HCNetSDK.NET_DVR_USER_LOGIN_INFO loginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();// 设备登录信息
    fillInLoginInfo(deviceIp, (short) devicePort, username, password, loginInfo);
    final int userHandle = HC_NET_SDK.NET_DVR_Login_V40(loginInfo, deviceInfo); // 用户句柄
    if (userHandle == -1) {
      log.error("设备登录失败 [IP={};错误码={}]", deviceIp, HC_NET_SDK.NET_DVR_GetLastError());
      return userHandle;
    } else {
      deviceInfo.read();
      logDeviceInfo(deviceIp, deviceInfo);
    }
    return userHandle;
  }

  private static void logDeviceInfo(final String deviceIp, final HCNetSDK.NET_DVR_DEVICEINFO_V40 deviceInfo) {
    final String sn = new String(deviceInfo.struDeviceV30.sSerialNumber).trim();
    final byte channelNums = deviceInfo.struDeviceV30.byChanNum;
    final byte startChannel = deviceInfo.struDeviceV30.byStartChan;
    final int ipChannelNums = deviceInfo.struDeviceV30.byIPChanNum + deviceInfo.struDeviceV30.byHighDChanNum * 256;
    final byte startDigitalChannel = deviceInfo.struDeviceV30.byStartDChan;
    log.warn(
      "设备登录成功 [IP={};SN={};模拟通道个数={};模拟通道起始通道号={};设备支持的最大 IP 通道数={};数字通道起始通道号={}]",
      deviceIp,
      sn,
      channelNums,
      startChannel,
      ipChannelNums,
      startDigitalChannel
    );
  }

  private static void fillInLoginInfo(
    final String deviceIp,
    final short devicePort,
    final String username,
    final String password,
    final HCNetSDK.NET_DVR_USER_LOGIN_INFO loginInfo
  ) {
    loginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
    System.arraycopy(deviceIp.getBytes(), 0, loginInfo.sDeviceAddress, 0, deviceIp.length());

    loginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
    System.arraycopy(username.getBytes(), 0, loginInfo.sUserName, 0, username.length());

    loginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
    System.arraycopy(password.getBytes(), 0, loginInfo.sPassword, 0, password.length());

    loginInfo.wPort = devicePort; // SDK 端口
    loginInfo.bUseAsynLogin = false; // 是否异步登录
    loginInfo.write();
  }

  private void logout(final int userHandle) {
    // 退出程序时调用, 每一台设备分别注销
    HC_NET_SDK.NET_DVR_Logout(userHandle);
  }

  private HCNetSDK.NET_DVR_SHOWSTRING_V30 getOsdCfg(final int userHandle, final int channel) {
    // 获取叠加字符参数 NET_DVR_SHOWSTRING_V30
    final HCNetSDK.NET_DVR_SHOWSTRING_V30 osdCfg = new HCNetSDK.NET_DVR_SHOWSTRING_V30();
    osdCfg.write();

    // 获取通道参数 NET_DVR_GetDVRConfig
    final Pointer pointer = osdCfg.getPointer(); // 接收数据的缓冲指针
    final IntByReference ref = new IntByReference(0); // 获取 OSD 配置参数
    if (!HC_NET_SDK.NET_DVR_GetDVRConfig(
      userHandle,
      HCNetSDK.NET_DVR_GET_SHOWSTRING_V30,
      channel,
      pointer,
      osdCfg.size(),
      ref
    )) {
      log.error("海康 SDK 获取叠加字符参数失败 [错误码={}]", HC_NET_SDK.NET_DVR_GetLastError());
      return null;
    }

    osdCfg.read();
    // log.warn("debug osdCfg: {} -> {}", osdCfg, osdCfg.struStringInfo);
    return osdCfg;
  }

  private void setOsdCfg(final int userHandle, final int channel, final HCNetSDK.NET_DVR_SHOWSTRING_V30 osdCfg) {
    // 设置叠加字符参数 NET_DVR_SET_SHOWSTRING_V30
    // 设置通道参数 NET_DVR_SetDVRConfig
    if (!HC_NET_SDK.NET_DVR_SetDVRConfig(
      userHandle,
      HCNetSDK.NET_DVR_SET_SHOWSTRING_V30,
      channel,
      osdCfg.getPointer(),
      osdCfg.size()
    )) {
      log.error("海康 SDK 设置叠加字符参数失败 [错误码={}]", HC_NET_SDK.NET_DVR_GetLastError());
    }
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class NvrStatus {

    private Boolean online; // 网络状态
    private Boolean recordStatus; // 录像状态
    private Boolean diskStatus; // 硬盘状态
    private Boolean audioStatus; // 音频状态
    private Boolean videoStatus; // 视频状态
  }
}
