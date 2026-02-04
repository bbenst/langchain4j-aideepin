package com.moyz.adi.common.filter;

import com.moyz.adi.common.config.RequestReaderHttpServletRequestWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 请求包装过滤器。
 * 当前主要用于配合 LogInterceptor 使用，现已由 ControllerParamsLogAspect 替代。
 */
@Slf4j
//@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpServletRequestReplacedFilter extends OncePerRequestFilter {

    /**
     * 包装请求以便重复读取流。
     *
     * @param request 请求
     * @param response 响应
     * @param filterChain 过滤链
     * @throws ServletException Servlet 异常
     * @throws IOException IO 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("HttpServletRequestReplacedFilter:" + request.getRequestURI());
        ServletRequest requestWrapper = new RequestReaderHttpServletRequestWrapper(request);
        filterChain.doFilter(requestWrapper, response);
    }

}
