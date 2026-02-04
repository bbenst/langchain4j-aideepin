package com.moyz.adi.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 应用自定义配置属性。
 */
@Configuration
@ConfigurationProperties("adi")
@Data
public class AdiProperties {

    /**
     * 服务主机地址。
     */
    private String host;

    /**
     * 前端访问地址。
     */
    private String frontendUrl;

    /**
     * 后端访问地址。
     */
    private String backendUrl;

    /**
     * 代理配置。
     */
    private Proxy proxy;

    /**
     * 向量嵌入模型名称。
     */
    private String embeddingModel;

    /**
     * 向量数据库类型。
     */
    private String vectorDatabase;

    /**
     * 图数据库类型。
     */
    private String graphDatabase;

    /**
     * 数据源配置。
     */
    private Datasource datasource;

    /**
     * 加密配置。
     */
    private Encrypt encrypt;

    /**
     * 代理配置项。
     */
    @Data
    public static class Proxy {
        /**
         * 是否启用代理。
         */
        private boolean enable;
        /**
         * 代理主机。
         */
        private String host;
        /**
         * 代理 HTTP 端口。
         */
        private int httpPort;
    }

    /**
     * 数据源配置项。
     */
    @Data
    public static class Datasource {
        /**
         * Neo4j 配置。
         */
        private Neo4j neo4j;
    }

    /**
     * Neo4j 连接配置。
     */
    @Data
    public static class Neo4j {
        /**
         * 主机地址。
         */
        private String host;
        /**
         * 端口号。
         */
        private int port;
        /**
         * 用户名。
         */
        private String username;
        /**
         * 密码。
         */
        private String password;
        /**
         * 数据库名称。
         */
        private String database;
    }

    /**
     * 加密配置项。
     */
    @Data
    public static class Encrypt {
        /**
         * AES 密钥。
         */
        private String aesKey;
    }
}
