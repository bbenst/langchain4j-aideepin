package com.moyz.adi.common.util;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
/**
 * AES 加解密工具类（参考 com.baomidou.mybatisplus.core.toolkit.AES）。
 */
public class AesUtil {
    /**
     * 默认 AES 密钥。
     */
    public static String AES_KEY = "";
    /**
     * 使用默认密钥加密字符串。
     *
     * @param data 明文内容
     * @return Base64 密文
     */
    public static String encrypt(String data) {
        return encrypt(data, AES_KEY);
    }
    /**
     * 使用默认密钥解密字符串。
     *
     * @param data Base64 密文
     * @return 明文内容
     */
    public static String decrypt(String data) {
        return decrypt(data, AES_KEY);
    }

    /**
     * 加密字节数组。
     *
     * @param data 明文字节
     * @param key  密钥字节
     * @return 密文字节
     */
    public static byte[] encrypt(byte[] data, byte[] key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, Constants.AES);
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, Constants.AES);
            Cipher cipher = Cipher.getInstance(Constants.AES_CBC_CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(key));
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new MybatisPlusException(e);
        }
    }

    /**
     * 解密字节数组。
     *
     * @param data 密文字节
     * @param key  密钥字节
     * @return 明文字节
     */
    public static byte[] decrypt(byte[] data, byte[] key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, Constants.AES);
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, Constants.AES);
            Cipher cipher = Cipher.getInstance(Constants.AES_CBC_CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(key));
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new MybatisPlusException(e);
        }
    }

    /**
     * 加密字符串。
     *
     * @param data 明文内容
     * @param key  密钥
     * @return Base64 密文
     */
    public static String encrypt(String data, String key) {
        byte[] valueByte = encrypt(data.getBytes(StandardCharsets.UTF_8), key.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(valueByte);
    }

    /**
     * 解密字符串。
     *
     * @param data Base64 密文
     * @param key  密钥
     * @return 明文内容
     */
    public static String decrypt(String data, String key) {
        byte[] originalData = Base64.getDecoder().decode(data.getBytes());
        byte[] valueByte = decrypt(originalData, key.getBytes(StandardCharsets.UTF_8));
        return new String(valueByte);
    }

    /**
     * 生成随机字符串密钥。
     *
     * @return 16 位密钥字符串
     */
    public static String generateRandomKey() {
        return IdWorker.get32UUID().substring(0, 16);
    }
}
