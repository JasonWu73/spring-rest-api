package net.wuxianjie.springrestapi.shared.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OsUtils {

  public static boolean isLinux() {
    return System.getProperty("os.name").toLowerCase().contains("linux");
  }

  public static boolean isWindows() {
    return System.getProperty("os.name").toLowerCase().contains("windows");
  }
}
