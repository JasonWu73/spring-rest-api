package net.wuxianjie.springrestapi.shared.util;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RsaUtils {

  /**
   * 生成新的 Base64 私钥公钥对.
   *
   * @return Base64 公私钥对
   */
  public static KeyPair generateKeyPair() {
    final RSA rsa = new RSA();
    final String privateKeyBase64 = rsa.getPrivateKeyBase64();
    final String publicKeyBase64 = rsa.getPublicKeyBase64();
    return new KeyPair(privateKeyBase64, publicKeyBase64);
  }

  /**
   * 将原始字节流数据加密为 Hex 字符串.
   *
   * @param privateKey 私钥
   * @param raw 被加密的 bytes
   * @return 十六进制字符串
   */
  public static String encryptHexStr(final String privateKey, final byte[] raw) {
    final RSA rsa = new RSA(privateKey, null);
    final byte[] encryptBytes = rsa.encrypt(raw, KeyType.PrivateKey);
    return HexUtil.encodeHexStr(encryptBytes, false);
  }

  /**
   * 从 Hex 或 Base64 字符串解密, 编码为 UTF-8 格式.
   *
   * @param publicKey 公钥
   * @param data Hex (16 进制) 或 Base64 字符串
   * @return 解密后的 bytes
   */
  public static byte[] decrypt(final String publicKey, final String data) {
    final RSA rsa = new RSA(null, publicKey);
    return rsa.decrypt(data, KeyType.PublicKey);
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class KeyPair {
    private String privateKey;
    private String publicKey;
  }
}
