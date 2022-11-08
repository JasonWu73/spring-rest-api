package net.wuxianjie.springrestapi;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.springrestapi.shared.util.FileUtils;
import net.wuxianjie.springrestapi.shared.util.RsaUtils;
import net.wuxianjie.springrestapi.shared.util.StrUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
@Slf4j
class LicenseTest {

  @Test
  void generateKeyPair() {
    final RsaUtils.KeyPair keyPair = RsaUtils.generateKeyPair();
    log.info("PRIVATE KEY:\n{}", keyPair.getPrivateKey());
    log.info("PUBLIC KEY:\n{}", keyPair.getPublicKey());
  }

  @Test
  void checkForLocalMacAddressSigning() {
    final String machineCode = StrUtils.getMachineCode().orElseThrow();
    log.info("Machine Code: {}", machineCode);
    log.info("MAC Address: {}", StrUtils.toMacAddress(machineCode).orElseThrow());

    final String privateKeyBase64 = FileUtil.readString(FileUtils.getAppDirAbsolutePath() + "PRIVATE_KEY.txt", CharsetUtil.CHARSET_UTF_8);
    final byte[] bytes = StrUtil.bytes(machineCode, CharsetUtil.CHARSET_UTF_8);
    final String license = RsaUtils.encryptHexStr(privateKeyBase64, bytes);
    log.info("LICENSE (length: {}):\n{}", license.length(), license);

    final String publicKeyBase64 = FileUtil.readString(FileUtils.getAppDirAbsolutePath() + "PUBLIC_KEY.txt", CharsetUtil.CHARSET_UTF_8);
    final byte[] decrypted = RsaUtils.decrypt(publicKeyBase64, license);
    Assertions.assertEquals(machineCode, StrUtil.str(decrypted, CharsetUtil.CHARSET_UTF_8));
  }
}
