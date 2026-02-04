package com.moyz.adi.common.rag.neo4j;

import dev.langchain4j.community.store.embedding.neo4j.Neo4jEmbeddingStore;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsIn;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.neo4j.cypherdsl.core.Condition;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Node;
import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.cypherdsl.core.renderer.Renderer;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;

import java.lang.reflect.Field;
import java.util.*;

import static com.moyz.adi.common.rag.neo4j.Neo4jEmbeddingUtils.toEmbeddingMatch;
import static org.neo4j.cypherdsl.core.Cypher.*;


/**
 * 对 Neo4jEmbeddingStore 的包装，补充自定义查询能力。
 */
@Slf4j
public class AdiNeo4jEmbeddingStore implements EmbeddingStore<TextSegment> {

    /**
     * Neo4j 驱动。
     */
    private final Driver driver;
    /**
     * 会话配置。
     */
    private final SessionConfig config;
    /**
     * 清洗后的标签名称。
     */
    private final String sanitizedLabel;
    /**
     * 向量字段名。
     */
    private final String embeddingProperty;
    /**
     * ID 字段名。
     */
    private final String idProperty;

    /**
     * 原始 Neo4jEmbeddingStore。
     */
    private final Neo4jEmbeddingStore neo4jEmbeddingStore;

    /**
     * 构建包装存储。
     *
     * @param neo4jEmbeddingStore 原始存储实现
     */
    public AdiNeo4jEmbeddingStore(Neo4jEmbeddingStore neo4jEmbeddingStore) {
        this.neo4jEmbeddingStore = neo4jEmbeddingStore;
        try {
            Field field1 = FieldUtils.getDeclaredField(Neo4jEmbeddingStore.class, "driver", true);
            Field field2 = FieldUtils.getDeclaredField(Neo4jEmbeddingStore.class, "config", true);
            Field field3 = FieldUtils.getDeclaredField(Neo4jEmbeddingStore.class, "sanitizedLabel", true);
            Field field4 = FieldUtils.getDeclaredField(Neo4jEmbeddingStore.class, "embeddingProperty", true);
            Field field5 = FieldUtils.getDeclaredField(Neo4jEmbeddingStore.class, "idProperty", true);
            this.driver = (Driver) FieldUtils.readField(field1, neo4jEmbeddingStore);
            this.config = (SessionConfig) FieldUtils.readField(field2, neo4jEmbeddingStore);
            this.sanitizedLabel = (String) FieldUtils.readField(field3, neo4jEmbeddingStore);
            this.embeddingProperty = (String) FieldUtils.readField(field4, neo4jEmbeddingStore);
            this.idProperty = (String) FieldUtils.readField(field5, neo4jEmbeddingStore);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据 ID 列表检索向量。
     *
     * @param ids ID 列表
     * @return 检索结果
     */
    public EmbeddingSearchResult<TextSegment> searchByIds(List<String> ids) {
        if (ids.isEmpty()) {
            log.warn("searchByIds ids is empty");
            return new EmbeddingSearchResult<>(new ArrayList<>());
        }
        Map<String, Object> params = new HashMap<>();
        params.put("maxResults", 1000);
        try (var session = session()) {
//            String query = """
//                    match (node:%s)
//                    WHERE elementId(node) in (%s)
//                    with node
//                    return properties(node) as metadata, elementId(node) as elementId, node.text as text, node.embedding as embedding, 1 as score
//                    """.formatted(this.sanitizedLabel, String.join(",", ids));
            Node node = node(this.sanitizedLabel).named("node");
            AdiNeo4jFilterMapper neo4jFilterMapper = new AdiNeo4jFilterMapper(node);
            Condition condition = node.property(this.embeddingProperty)
                    .isNotNull()
                    .and(neo4jFilterMapper.getCondition(new IsIn("id", ids)));
            Statement statement = match(node)
                    .where(condition)
                    .with(node)
                    .returning(Cypher.raw("properties(node) as metadata, node['id'] as id, node.text as text, node.embedding as embedding, 1 as score"))
                    .orderBy(name(idProperty))
                    .descending()
                    .limit(parameter("maxResults"))
                    .build();
            String cypherQuery = Renderer.getDefaultRenderer().render(statement);
            log.info("searchByIds cypherQuery: {}", cypherQuery);
            return getEmbeddingSearchResult(session, cypherQuery, params);
        }
    }

    /**
     * 按元数据条件统计数量。
     *
     * @param filter 过滤条件
     * @return 数量
     */
    public int countByMetadata(Filter filter) {
        try (var session = session()) {
            Node node = node(this.sanitizedLabel).named("node");
            AdiNeo4jFilterMapper neo4jFilterMapper = new AdiNeo4jFilterMapper(node);
            Condition condition = node.property(this.embeddingProperty)
                    .isNotNull()
                    .and(neo4jFilterMapper.getCondition(filter));
            Statement statement = match(node)
                    .where(condition)
                    .with(node)
                    .returning(Cypher.raw("count(node) as count"))
                    .build();
            String cypherQuery = Renderer.getDefaultRenderer().render(statement);
            log.info("countByMetadata cypherQuery: {}", cypherQuery);
            return session.run(cypherQuery).single().get("count").asInt();
        }
    }

    /**
     * 按元数据条件检索向量。
     *
     * @param filter 过滤条件
     * @param maxResult 最大返回数量
     * @return 检索结果
     */
    public EmbeddingSearchResult<TextSegment> searchByMetadata(Filter filter, int maxResult) {
        try (var session = session()) {
            Node node = node(this.sanitizedLabel).named("node");
            AdiNeo4jFilterMapper neo4jFilterMapper = new AdiNeo4jFilterMapper(node);
            Condition condition = node.property(this.embeddingProperty)
                    .isNotNull()
                    .and(neo4jFilterMapper.getCondition(filter));
            Statement statement = match(node)
                    .where(condition)
                    .with(node)
                    .returning(Cypher.raw("properties(node) as metadata, node['id'] as id, node.text as text, node.embedding as embedding, 1 as score"))
                    .orderBy(name(idProperty))
                    .descending()
                    .limit(parameter("maxResults"))
                    .build();
            String cypherQuery = Renderer.getDefaultRenderer().render(statement);
            log.info("searchByMetadata cypherQuery: {}", cypherQuery);
            Map<String, Object> params = new HashMap<>();
            params.put("maxResults", maxResult);
            return getEmbeddingSearchResult(session, cypherQuery, params);
        }
    }

    /**
     * 执行查询并转换为检索结果。
     *
     * @param session 会话
     * @param query 查询语句
     * @param params 参数
     * @return 检索结果
     */
    private EmbeddingSearchResult<TextSegment> getEmbeddingSearchResult(
            Session session, String query, Map<String, Object> params) {
        List<EmbeddingMatch<TextSegment>> matches =
                session.run(query, params).list(item -> toEmbeddingMatch(this, item));

        return new EmbeddingSearchResult<>(matches);
    }

    /**
     * 创建会话。
     *
     * @return 会话
     */
    private Session session() {
        return this.driver.session(this.config);
    }


    /**
     * 获取原始 Neo4jEmbeddingStore。
     *
     * @return 原始存储
     */
    public Neo4jEmbeddingStore getNeo4jEmbeddingStore() {
        return neo4jEmbeddingStore;
    }

    /**
     * 写入向量并返回 ID。
     *
     * @param embedding 向量
     * @return ID
     */
    @Override
    public String add(Embedding embedding) {
        return neo4jEmbeddingStore.add(embedding);
    }

    /**
     * 写入指定 ID 的向量。
     *
     * @param s ID
     * @param embedding 向量
     */
    @Override
    public void add(String s, Embedding embedding) {
        neo4jEmbeddingStore.add(s, embedding);
    }

    /**
     * 写入向量与分段。
     *
     * @param embedding 向量
     * @param textSegment 分段
     * @return ID
     */
    @Override
    public String add(Embedding embedding, TextSegment textSegment) {
        return neo4jEmbeddingStore.add(embedding, textSegment);
    }

    /**
     * 批量写入向量。
     *
     * @param list 向量列表
     * @return ID 列表
     */
    @Override
    public List<String> addAll(List<Embedding> list) {
        return neo4jEmbeddingStore.addAll(list);
    }

    /**
     * 批量写入向量与分段。
     *
     * @param embeddings 向量列表
     * @param embedded 分段列表
     * @return ID 列表
     */
    @Override
    public List<String> addAll(List<Embedding> embeddings, List<TextSegment> embedded) {
        return neo4jEmbeddingStore.addAll(embeddings, embedded);
    }

    /**
     * 批量写入指定 ID 的向量与分段。
     *
     * @param ids ID 列表
     * @param embeddings 向量列表
     * @param embedded 分段列表
     */
    @Override
    public void addAll(List<String> ids, List<Embedding> embeddings, List<TextSegment> embedded) {
        neo4jEmbeddingStore.addAll(ids, embeddings, embedded);
    }

    /**
     * 执行向量检索。
     *
     * @param embeddingSearchRequest 检索请求
     * @return 检索结果
     */
    @Override
    public EmbeddingSearchResult<TextSegment> search(EmbeddingSearchRequest embeddingSearchRequest) {
        return neo4jEmbeddingStore.search(embeddingSearchRequest);
    }

    /**
     * 根据过滤条件删除向量。
     *
     * @param filter 过滤条件
     */
    @Override
    public void removeAll(Filter filter) {
        neo4jEmbeddingStore.removeAll(filter);
    }

    /**
     * 删除指定 ID 的向量。
     *
     * @param id ID
     */
    @Override
    public void remove(String id) {
        neo4jEmbeddingStore.remove(id);
    }

    /**
     * 删除全部向量。
     */
    @Override
    public void removeAll() {
        neo4jEmbeddingStore.removeAll();
    }

    /**
     * 批量删除指定 ID 的向量。
     *
     * @param ids ID 集合
     */
    @Override
    public void removeAll(Collection<String> ids) {
        neo4jEmbeddingStore.removeAll(ids);
    }
}
