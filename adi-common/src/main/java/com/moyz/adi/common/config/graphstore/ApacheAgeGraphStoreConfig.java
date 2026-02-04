package com.moyz.adi.common.config.graphstore;

import com.moyz.adi.common.rag.ApacheAgeGraphStore;
import com.moyz.adi.common.rag.GraphStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Apache AGE 图数据库配置。
 */
@Slf4j
@Configuration
@ConditionalOnProperty(value = "adi.graph-database", havingValue = "apache-age")
public class ApacheAgeGraphStoreConfig {

    /**
     * 数据库连接地址。
     */
    @Value("${spring.datasource.url}")
    private String dataBaseUrl;

    /**
     * 数据库用户名。
     */
    @Value("${spring.datasource.username}")
    private String dataBaseUserName;

    /**
     * 数据库密码。
     */
    @Value("${spring.datasource.password}")
    private String dataBasePassword;

    /**
     * 构建知识库图存储。
     *
     * @return 图存储实例
     */
    @Bean(name = "kbGraphStore")
    @Primary
    public GraphStore initGraphStore() {
        // 通过正则解析 JDBC 连接字符串，提取主机、端口和数据库名
        String regex = "jdbc:postgresql://([^:/]+):(\\d+)/(\\w+).+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(dataBaseUrl);

        String host = "";
        String port = "";
        String databaseName = "";
        if (matcher.matches()) {
            host = matcher.group(1);
            port = matcher.group(2);
            databaseName = matcher.group(3);

            log.info("Host: " + host);
            log.info("Port: " + port);
            log.info("Database: " + databaseName);
        } else {
            throw new RuntimeException("parse url error");
        }
        return ApacheAgeGraphStore.builder()
                .host(host)
                .port(Integer.parseInt(port))
                .database(databaseName)
                .user(dataBaseUserName)
                .password(dataBasePassword)
                .createGraph(true)
                .dropGraphFirst(false)
                .graphName("adi_knowledge_base_graph")
                .build();
    }
}
