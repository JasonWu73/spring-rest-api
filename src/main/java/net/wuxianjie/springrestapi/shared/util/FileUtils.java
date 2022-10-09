package net.wuxianjie.springrestapi.shared.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.wuxianjie.springrestapi.shared.exception.ApiException;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileUtils {

  public static String getJarDirAbsoluteFilePath() {
    return new ApplicationHome(FileUtils.class).getDir().getAbsolutePath() + "/";
  }

  public static String getTopmostDirPath(final String filePath) {
    final String parent = FileUtil.getParent(filePath, 1);
    if (parent == null) {
      return filePath;
    }
    return getTopmostDirPath(parent);
  }

  public static String getValidFilename(final MultipartFile file) {
    final String originalFilename = file.getOriginalFilename();
    final String filenameTrimToNull = StrUtil.trimToNull(originalFilename);
    if (filenameTrimToNull == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "文件名不存在");
    }
    final String filename = FileNameUtil.getName(filenameTrimToNull);
    if (FileNameUtil.containsInvalid(filename)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "文件名存在非法字符, 包含: \\ / : * ? \" < > |");
    }
    return filename;
  }
}
