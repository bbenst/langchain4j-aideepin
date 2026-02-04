package com.moyz.adi.common.helper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
/**
 * HTTP 辅助类。
 */
@Slf4j
public class HttpHelper {
    /**
     * 私有构造函数，禁止实例化。
     */
    private HttpHelper() {
    }
    /**
     * 获取请求体字符串。
     *
     * @param request 请求
     * @return 请求体字符串
     * @throws IOException IO 异常
     */
    public static String getBodyString(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (
                InputStream inputStream = request.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
        ) {
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            log.error("error", e);
        }
        return sb.toString();
    }
}
