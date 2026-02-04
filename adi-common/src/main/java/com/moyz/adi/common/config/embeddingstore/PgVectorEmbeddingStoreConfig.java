package com.moyz.adi.common.config.embeddingstore;

import com.moyz.adi.common.config.AdiProperties;
import com.moyz.adi.common.util.AdiPropertiesUtil;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PgVector 向量库配置。
 */
@Slf4j
@Configuration
@ConditionalOnProperty(value = "adi.vector-database", havingValue = "pgvector")
public class PgVectorEmbeddingStoreConfig {

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
     * 应用配置属性。
     */
    @Resource
    private AdiProperties adiProperties;

    /**
     * 知识库向量库。
     *
     * @return EmbeddingStore 实例
     */
    @Bean(name = "kbEmbeddingStore")
    @Primary
    public EmbeddingStore<TextSegment> initKbEmbeddingStore() {
        log.info("Initializing kbEmbeddingStore...");
        String tableName = "adi_knowledge_base_embedding";
        Pair<String, Integer> pair = AdiPropertiesUtil.getSuffixAndDimension(adiProperties);
        if (StringUtils.isNotBlank(pair.getLeft())) {
            tableName = tableName + "_" + pair.getLeft();
        }
        return createEmbeddingStore(tableName, pair.getRight());
    }

    /**
     * 角色记忆使用的向量库。
     *
     * @return EmbeddingStore 实例
     */
    @Bean(name = "convMemoryEmbeddingStore")
    public EmbeddingStore<TextSegment> initConvMemoryEmbeddingStore() {
        log.info("Initializing convMemoryEmbeddingStore...");
        String tableName = "adi_conversation_memory_embedding";
        Pair<String, Integer> pair = AdiPropertiesUtil.getSuffixAndDimension(adiProperties);
        if (StringUtils.isNotBlank(pair.getLeft())) {
            tableName = tableName + "_" + pair.getLeft();
        }
        return createEmbeddingStore(tableName, pair.getRight());
    }

    /**
     * AI 搜索向量库。
     *
     * @return EmbeddingStore 实例
     */
    @Bean(name = "searchEmbeddingStore")
    public EmbeddingStore<TextSegment> initSearchEmbeddingStore() {
        log.info("Initializing searchEmbeddingStore...");
        String tableName = "adi_ai_search_embedding";
        Pair<String, Integer> pair = AdiPropertiesUtil.getSuffixAndDimension(adiProperties);
        if (StringUtils.isNotBlank(pair.getLeft())) {
            tableName = tableName + "_" + pair.getLeft();
        }
        return createEmbeddingStore(tableName, pair.getRight());
    }

    /**
     * 构建 PgVector 向量库实例。
     *
     * @param tableName 表名
     * @param dimension 向量维度
     * @return 向量库实例
     */
    private EmbeddingStore<TextSegment> createEmbeddingStore(String tableName, int dimension) {
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
        log.info("Creating PgVectorEmbeddingStore with table name:{},dimension:{}", tableName, dimension);
        return PgVectorEmbeddingStore.builder()
                .host(host)
                .port(Integer.parseInt(port))
                .database(databaseName)
                .user(dataBaseUserName)
                .password(dataBasePassword)
                .dimension(dimension)
                .createTable(true)
                .dropTableFirst(false)
                .table(tableName)
                .build();
    }

}
