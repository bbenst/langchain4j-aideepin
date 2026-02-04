package com.moyz.adi.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC 配置类。
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * JSON 序列化工具。
     */
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 配置消息转换器。
     *
     * @param converters 转换器列表
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("WebMvcConfig==configureMessageConverters");
        WebMvcConfigurer.super.configureMessageConverters(converters);
        converters.add(new StringHttpMessageConverter());
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
        converters.add(new ByteArrayHttpMessageConverter());
    }

    /**
     * 配置跨域规则。
     *
     * @param registry 跨域注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*"); // 允许的请求头
    }

    /**
     * 配置拦截器（当前不启用）。
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //使用ParamsLogAspect代替
//        log.info("WebMvcConfig==addInterceptors");
//        registry.addInterceptor(logInterceptor)
//                .addPathPatterns("/**")
//                .excludePathPatterns("/swagger-resources/**", "/webjars/**", "/v3/**", "/swagger-ui.html");
    }

}
