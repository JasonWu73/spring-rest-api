package net.wuxianjie.springrestapi;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.springrestapi.shared.util.FileUtils;
import net.wuxianjie.springrestapi.shared.util.StrUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
class LicenseTest {

  @Test
  void generateKeyPair() {
    final RSA rsa = new RSA();
    final String privateKeyBase64 = rsa.getPrivateKeyBase64();
    log.info("PRIVATE KEY:\n{}", privateKeyBase64);
    final String publicKeyBase64 = rsa.getPublicKeyBase64();
    log.info("PUBLIC KEY:\n{}", publicKeyBase64);
  }

  @Test
  void checkForLocalMacAddressSigning() {
    final String machineCode = StrUtils.getMachineCode();
    log.info("Machine Code: {}", machineCode);
    log.info("MAC: {}", StrUtils.toMacAddress(machineCode));

    final String privateKeyBase64 = FileUtil.readString(FileUtils.getJarDirAbsoluteFilePath() + "PRIVATE_KEY.txt", CharsetUtil.CHARSET_UTF_8);
    final RSA rsaOnlyPrivateKey = new RSA(privateKeyBase64, null);
    final byte[] encryptBytes = rsaOnlyPrivateKey.encrypt(StrUtil.bytes(machineCode, CharsetUtil.CHARSET_UTF_8), KeyType.PrivateKey);
    final String license = HexUtil.encodeHexStr(encryptBytes, false);
    log.info("LICENSE (length: {}):\n{}", license.length(), license);

    final String publicKeyBase64 = FileUtil.readString(FileUtils.getJarDirAbsoluteFilePath() + "PUBLIC_KEY.txt", CharsetUtil.CHARSET_UTF_8);
    final RSA rsaOnlyPublicKey = new RSA(null, publicKeyBase64);
    final byte[] decryptBytes = rsaOnlyPublicKey.decrypt(license, KeyType.PublicKey);
    Assertions.assertEquals(machineCode, StrUtil.str(decryptBytes, CharsetUtil.CHARSET_UTF_8));
  }
}
