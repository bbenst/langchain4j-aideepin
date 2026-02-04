package com.moyz.adi.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StopWatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * RestTemplate 客户端请求日志拦截器。
 */
@Slf4j
public class LogClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    /**
     * 记录请求与响应信息。
     *
     * @param request 请求
     * @param body 请求体
     * @param execution 执行器
     * @return 响应
     * @throws IOException IO 异常
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ClientHttpResponse response = execution.execute(request, body);

        stopWatch.stop();
        StringBuilder resBody = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
            String line = bufferedReader.readLine();
            while (line != null) {
                resBody.append(line);
                line = bufferedReader.readLine();
            }
        }
        if (request.getHeaders().getContentType() != null && request.getHeaders().getContentType()
                .includes(MediaType.MULTIPART_FORM_DATA)) {
            body = new byte[]{};
        }

        log.info("rest log status:{},time:{},url:{},body:{},response:{}",
                response.getRawStatusCode(), stopWatch.getLastTaskTimeMillis(),
                request.getURI(), new String(body, StandardCharsets.UTF_8), resBody);
        return response;
    }

}
