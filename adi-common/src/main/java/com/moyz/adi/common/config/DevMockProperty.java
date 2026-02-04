package com.moyz.adi.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 开发环境模拟配置属性。
 */
@Configuration
@ConfigurationProperties("adi.dev-mock")
@Data
public class DevMockProperty {
}
