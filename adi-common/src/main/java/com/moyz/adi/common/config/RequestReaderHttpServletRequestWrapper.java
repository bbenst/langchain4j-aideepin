package com.moyz.adi.common.config;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 缓存请求输入流，支持重复读取。
 */
public class RequestReaderHttpServletRequestWrapper extends HttpServletRequestWrapper {

    /**
     * 请求体字节缓存。
     */
    private final byte[] body;

    /**
     * 构造包装请求并缓存请求体。
     *
     * @param request 原始请求
     * @throws IOException IO 异常
     */
    public RequestReaderHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        body = StreamUtils.copyToByteArray(request.getInputStream());
    }

    /**
     * 获取可重复读取的字符流。
     *
     * @return 读取器
     * @throws IOException IO 异常
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    /**
     * 获取可重复读取的输入流。
     *
     * @return 输入流
     * @throws IOException IO 异常
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {

        final ByteArrayInputStream newInputStream = new ByteArrayInputStream(body);

        return new ServletInputStream() {

            /**
             * 读取下一个字节。
             *
             * @return 读取结果
             * @throws IOException IO 异常
             */
            @Override
            public int read() throws IOException {
                return newInputStream.read();
            }

            /**
             * 判断是否读取完成。
             *
             * @return 是否完成
             */
            @Override
            public boolean isFinished() {
                return newInputStream.available() == 0;
            }

            /**
             * 判断是否可读取。
             *
             * @return 是否就绪
             */
            @Override
            public boolean isReady() {
                return newInputStream.available() > 0;
            }

            /**
             * 设置读取监听器（此处不处理）。
             *
             * @param readListener 读取监听器
             */
            @Override
            public void setReadListener(ReadListener readListener) {

            }
        };
    }
}
