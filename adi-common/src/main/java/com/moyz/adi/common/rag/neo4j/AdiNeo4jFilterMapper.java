package com.moyz.adi.common.rag.neo4j;

import static org.neo4j.cypherdsl.core.Cypher.asExpression;
import static org.neo4j.cypherdsl.core.Cypher.literalOf;
import static org.neo4j.cypherdsl.core.Cypher.mapOf;
import static org.neo4j.cypherdsl.core.Cypher.not;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.langchain4j.Internal;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsGreaterThan;
import dev.langchain4j.store.embedding.filter.comparison.IsGreaterThanOrEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsIn;
import dev.langchain4j.store.embedding.filter.comparison.IsLessThan;
import dev.langchain4j.store.embedding.filter.comparison.IsLessThanOrEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsNotEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsNotIn;
import dev.langchain4j.store.embedding.filter.logical.And;
import dev.langchain4j.store.embedding.filter.logical.Not;
import dev.langchain4j.store.embedding.filter.logical.Or;

import java.time.OffsetDateTime;
import java.util.Map;

import org.neo4j.cypherdsl.core.Condition;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Expression;
import org.neo4j.cypherdsl.core.FunctionInvocation;
import org.neo4j.cypherdsl.core.Node;
import org.neo4j.driver.internal.value.ListValue;
import org.neo4j.driver.internal.value.PointValue;

/**
 * Neo4j 过滤条件转换器，将过滤表达式转为 Cypher 条件。
 */
@Internal
class AdiNeo4jFilterMapper {

    /**
     * JSON 序列化器。
     */
    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 不支持的过滤类型提示。
     */
    static final String UNSUPPORTED_FILTER_TYPE_ERROR = "Unsupported filter type: ";

    /**
     * 将 PointValue 转为 Cypher 点表达式。
     *
     * @param value1 点值
     * @return 点表达式
     */
    private static FunctionInvocation convertToPoint(PointValue value1) {
        try {
            String s = OBJECT_MAPPER.writeValueAsString(value1.asObject());
            Map<String, Object> map = new JsonMapper().readValue(s, Map.class);
            return Cypher.point(asExpression(map));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@link Cypher#literalOf(Object)} 不支持所有数据类型，
     * 这里对特殊类型进行转换。
     *
     * @param value 原始值
     * @return Cypher 表达式
     */
    static Expression toCypherLiteral(Object value) {
        if (value instanceof OffsetDateTime) {
            return Cypher.datetime(literalOf(value.toString()));
        }
        if (value instanceof PointValue pointValue) {
            return convertToPoint(pointValue);
        }
        if (value instanceof Map) {
            return mapOf(value);
        }
        if (value instanceof ListValue listValue) {
            return literalOf(listValue.asList());
        }

        // Other data types
        return literalOf(value);
    }

    /**
     * 当前操作的节点。
     */
    private final Node node;

    /**
     * 构建过滤器映射器。
     *
     * @param node 节点
     */
    AdiNeo4jFilterMapper(Node node) {
        this.node = node;
    }

    /**
     * 根据过滤器构建 Cypher 条件。
     *
     * @param filter 过滤器
     * @return 条件表达式
     */
    Condition getCondition(Filter filter) {
        if (filter instanceof IsEqualTo item) {
            Expression cypherLiteral = toCypherLiteral(item.key());
            Expression cypherLiteral1 = toCypherLiteral(item.comparisonValue());
            return node.property(cypherLiteral).eq(cypherLiteral1);
        } else if (filter instanceof IsNotEqualTo item) {
            Expression cypherLiteral = toCypherLiteral(item.key());
            Expression cypherLiteral1 = toCypherLiteral(item.comparisonValue());
            return node.property(cypherLiteral).isNotEqualTo(cypherLiteral1);
        } else if (filter instanceof IsGreaterThan item) {
            Expression cypherLiteral = toCypherLiteral(item.key());
            Expression cypherLiteral1 = toCypherLiteral(item.comparisonValue());
            return node.property(cypherLiteral).gt(cypherLiteral1);
        } else if (filter instanceof IsGreaterThanOrEqualTo item) {
            Expression cypherLiteral = toCypherLiteral(item.key());
            Expression cypherLiteral1 = toCypherLiteral(item.comparisonValue());
            return node.property(cypherLiteral).gte(cypherLiteral1);
        } else if (filter instanceof IsLessThan item) {
            Expression cypherLiteral = toCypherLiteral(item.key());
            Expression cypherLiteral1 = toCypherLiteral(item.comparisonValue());
            return node.property(cypherLiteral).lt(cypherLiteral1);
        } else if (filter instanceof IsLessThanOrEqualTo item) {
            Expression cypherLiteral = toCypherLiteral(item.key());
            Expression cypherLiteral1 = toCypherLiteral(item.comparisonValue());
            return node.property(cypherLiteral).lte(cypherLiteral1);
        } else if (filter instanceof IsIn item) {
            return mapIn(item);
        } else if (filter instanceof IsNotIn item) {
            return mapNotIn(item);
        } else if (filter instanceof And item) {
            return mapAnd(item);
        } else if (filter instanceof Not item) {
            return mapNot(item);
        } else if (filter instanceof Or item) {
            return mapOr(item);
        } else {
            throw new UnsupportedOperationException(
                    UNSUPPORTED_FILTER_TYPE_ERROR + filter.getClass().getName());
        }
    }

    /**
     * 构建 IN 条件。
     *
     * @param filter IN 过滤器
     * @return 条件表达式
     */
    Condition mapIn(IsIn filter) {
        Expression cypherLiteral = toCypherLiteral(filter.key());
        Expression cypherLiteral1 = toCypherLiteral(filter.comparisonValues());
        return Cypher.includesAny(node.property(cypherLiteral), cypherLiteral1);
    }

    /**
     * 构建 NOT IN 条件。
     *
     * @param filter NOT IN 过滤器
     * @return 条件表达式
     */
    Condition mapNotIn(IsNotIn filter) {
        Expression cypherLiteral = toCypherLiteral(filter.key());
        Expression cypherLiteral1 = toCypherLiteral(filter.comparisonValues());
        Condition condition1 = Cypher.includesAny(node.property(cypherLiteral), cypherLiteral1);
        return not(condition1);
    }

    /**
     * 构建 AND 条件。
     *
     * @param filter AND 过滤器
     * @return 条件表达式
     */
    private Condition mapAnd(And filter) {
        Condition left = getCondition(filter.left());
        Condition right = getCondition(filter.right());
        return left.and(right);
    }

    /**
     * 构建 OR 条件。
     *
     * @param filter OR 过滤器
     * @return 条件表达式
     */
    private Condition mapOr(Or filter) {
        Condition left = getCondition(filter.left());
        Condition right = getCondition(filter.right());
        return left.or(right);
    }

    /**
     * 构建 NOT 条件。
     *
     * @param filter NOT 过滤器
     * @return 条件表达式
     */
    private Condition mapNot(Not filter) {
        Condition expression = getCondition(filter.expression());
        return not(expression);
    }
}
