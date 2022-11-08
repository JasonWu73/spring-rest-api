package net.wuxianjie.springrestapi.hikvision;

import cn.hutool.core.util.StrUtil;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
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
   * 获取设备序列号.
   *
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
      log.error("设备登录失败 [错误码={}]", HC_NET_SDK.NET_DVR_GetLastError());
      return Optional.empty();
    } else {
      final String sn = new String(deviceInfo.struDeviceV30.sSerialNumber).trim();
      log.warn("设备登录成功 [IP={};SN={}]", deviceIp, sn);
      return Optional.of(sn);
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

    final int userHandle = login(deviceIp, devicePort, username, password);
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

    final int userHandle = login(deviceIp, devicePort, username, password);
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

  private int login(final String deviceIp, final int devicePort, final String username, final String password) {
    // 登录设备, 每一台设备分别登录; 登录句柄是唯一的, 可以区分设备
    final HCNetSDK.NET_DVR_USER_LOGIN_INFO loginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();// 设备登录信息
    final HCNetSDK.NET_DVR_DEVICEINFO_V40 deviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();// 设备信息
    fillInLoginInfo(deviceIp, (short) devicePort, username, password, loginInfo);
    final int userHandle = HC_NET_SDK.NET_DVR_Login_V40(loginInfo, deviceInfo); // 用户句柄
    if (userHandle == -1) {
      log.error("设备登录失败 [错误码={}]", HC_NET_SDK.NET_DVR_GetLastError());
      return userHandle;
    } else {
      log.warn(
        "设备登录成功 [IP={};SN={}]",
        deviceIp,
        new String(deviceInfo.struDeviceV30.sSerialNumber).trim()
      );
      deviceInfo.read();
    }
    return userHandle;
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
    // 获取网络参数 NET_DVR_GetDVRConfig
    final Pointer osdPointer = osdCfg.getPointer(); // 接收数据的缓冲指针
    final IntByReference bytesReturned = new IntByReference(0); // 获取 OSD 配置参数
    if (!HC_NET_SDK.NET_DVR_GetDVRConfig(
      userHandle,
      HCNetSDK.NET_DVR_GET_SHOWSTRING_V30,
      channel,
      osdPointer,
      osdCfg.size(),
      bytesReturned
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
}
