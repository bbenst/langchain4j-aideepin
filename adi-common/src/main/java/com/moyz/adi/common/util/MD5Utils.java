package com.moyz.adi.common.util;

import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
/**
 * MD5 摘要计算工具类。
 */
public class MD5Utils {
    /**
     * 生成字符串的 MD5 值。
     *
     * @param input 输入字符串
     * @return MD5 十六进制字符串
     */
    public static String generateMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 计算文件的 MD5 值。
     *
     * @param filePath 文件路径
     * @return MD5 十六进制字符串
     */
    public static String calculateMD5(String filePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (FileInputStream fis = new FileInputStream(filePath)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }
            byte[] md5Bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte md5Byte : md5Bytes) {
                sb.append(Integer.toString((md5Byte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }
    /**
     * 计算上传文件的 MD5 值。
     *
     * @param file 上传文件
     * @return MD5 十六进制字符串
     */
    public static String md5ByMultipartFile(MultipartFile file) {
        try {
            return DigestUtils.md5DigestAsHex(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
