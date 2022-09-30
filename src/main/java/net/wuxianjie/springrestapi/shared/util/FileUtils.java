package net.wuxianjie.springrestapi.shared.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.boot.system.ApplicationHome;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileUtils {

  public static String getJarDirAbsoluteFilePath() {
    return new ApplicationHome(FileUtils.class).getDir().getAbsolutePath() + "/";
  }
}
