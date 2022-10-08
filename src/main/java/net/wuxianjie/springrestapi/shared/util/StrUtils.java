package net.wuxianjie.springrestapi.shared.util;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StrUtils {

  /**
   * 去除字符串的首尾空白字符，并转换为支持数据库 LIKE 模糊搜索的字符串（{@code %value%}）。
   *
   * <p>本方法会将字符串中的空格替换为 {@code %}，例如：{@code "key1    key2" -> "%key1%key2%"}。
   *
   * @param value 需要转换的原字符串
   * @return 去除字符串首尾空白字符后的 {@code %value%} 字符串；若 {@code value} 为 null 或仅包含空白字符，则返回 null
   */
  public static String toNullableLikeValue(final String value) {
    final String trimmed = StrUtil.trimToNull(value);
    if (trimmed == null) {
      return null;
    }
    return "%" + trimmed.replaceAll(" +", "%") + "%";
  }

  public static String getMachineCode() {
    return HexUtil.encodeHexStr(StrUtil.bytes(NetUtil.getLocalMacAddress()), false);
  }
}
