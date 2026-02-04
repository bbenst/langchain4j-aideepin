package com.moyz.adi.common.util;

import com.moyz.adi.common.enums.ErrorEnum;
import com.moyz.adi.common.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
/**
 * 哈希计算工具类。
 */
@Slf4j
public class HashUtil {
    /**
     * 工具类禁止实例化。
     */
    private HashUtil() {
    }
    /**
     * 计算文件内容的 SHA-256。
     *
     * @param file 文件
     * @return 十六进制哈希串
     */
    public static String sha256(MultipartFile file) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(file.getBytes());
            // 将哈希值转换为十六进制字符串
            return getHashStr(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Calculate file sha256 NoSuchAlgorithmException", e);
            throw new BaseException(ErrorEnum.B_SERVER_EXCEPTION);
        } catch (Exception e) {
            log.error("Calculate file sha256 error", e);
            throw new BaseException(ErrorEnum.B_SERVER_EXCEPTION);
        }
    }
    /**
     * 计算字符串的 SHA-256。
     *
     * @param str 输入字符串
     * @return 十六进制哈希串
     */
    public static String sha256(String str) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(str.getBytes(StandardCharsets.UTF_8));
            // 将哈希值转换为十六进制字符串
            return getHashStr(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Calculate string sha256 NoSuchAlgorithmException", e);
            throw new BaseException(ErrorEnum.B_SERVER_EXCEPTION);
        } catch (Exception e) {
            log.error("Calculate string sha256 error", e);
            throw new BaseException(ErrorEnum.B_SERVER_EXCEPTION);
        }
    }
    /**
     * 将字节数组转换为十六进制字符串。
     *
     * @param hash 哈希字节
     * @return 十六进制字符串
     */
    @NotNull
    private static String getHashStr(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
