package net.wuxianjie.springrestapi.shared.util;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StrUtils {

  /**
   * 当 {@code value} 不为空时, 将字符串中的空白字符替换为 {@code %}, 例如: {@code "key1    key2" -> "%key1%key2%"}; 否则返回 {@code null}. 可用于数据库 LIKE 的值.
   *
   * @param value 需要转换的原字符串
   * @return {@code "key1    key2" -> "%key1%key2%"} 或 {@code null}
   */
  public static String toNullableLikeValue(final String value) {
    final String trimmed = StrUtil.trimToNull(value);
    if (trimmed == null) {
      return null;
    }
    return "%" + trimmed.replaceAll(" +", "%") + "%";
  }

  public static Optional<String> getMachineCode() {
    final String macAddr = NetUtil.getLocalMacAddress();
    if (macAddr == null) return Optional.empty();

    return Optional.of(HexUtil.encodeHexStr(StrUtil.bytes(macAddr), false));
  }

  public static Optional<String> toMacAddress(final String machineCode) {
    final String trimmed = StrUtil.trimToNull(machineCode);
    if (trimmed == null) {
      return Optional.empty();
    }
    return Optional.of(HexUtil.decodeHexStr(machineCode));
  }
}
