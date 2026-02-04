package com.moyz.adi.common.config;

import com.moyz.adi.common.helper.HttpHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 请求日志拦截器。
 */
@Slf4j
//@Service
public class LogInterceptor implements HandlerInterceptor {

    /**
     * JSON 序列化工具。
     */
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 请求进入前记录参数与请求体。
     *
     * @param request 请求
     * @param response 响应
     * @param handler 处理器
     * @return 是否放行
     * @throws Exception 处理异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod) handler;
            if (HttpMethod.GET.matches(request.getMethod())) {
                log.info("url:{},ip:{},method:{},param:{}", request.getRequestURL(),
                        request.getRemoteAddr(), method.getMethod().getName(),
                        objectMapper.writeValueAsString(request.getParameterMap()));
            } else {
                String bodyString = HttpHelper.getBodyString(request);
                log.info("url:{},ip:{},method：{},param:{},body:{}", request.getRequestURL(),
                        request.getRemoteAddr(), method.getMethod().getName(),
                        objectMapper.writeValueAsString(request.getParameterMap()), bodyString);
            }
        } else {
            log.info("url:{},ip:{}", request.getRequestURL(),
                    request.getRemoteAddr());
        }

        return true;
    }

    /**
     * 请求处理完成后回调（此处留空）。
     *
     * @param request 请求
     * @param response 响应
     * @param handler 处理器
     * @param modelAndView 视图模型
     * @throws Exception 处理异常
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {

    }

    /**
     * 请求完成后回调（此处留空）。
     *
     * @param request 请求
     * @param response 响应
     * @param handler 处理器
     * @param ex 异常
     * @throws Exception 处理异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {

    }
}
