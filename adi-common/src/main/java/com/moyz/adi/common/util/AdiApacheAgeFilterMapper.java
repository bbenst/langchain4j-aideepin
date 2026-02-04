package com.moyz.adi.common.util;

import com.moyz.adi.common.vo.GraphContains;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.*;
import dev.langchain4j.store.embedding.filter.logical.And;
import dev.langchain4j.store.embedding.filter.logical.Not;
import dev.langchain4j.store.embedding.filter.logical.Or;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
/**
 * Apache AGE 过滤条件映射器抽象基类。
 */
abstract class AdiApacheAgeFilterMapper {
    /**
     * 节点别名。
     */
    protected String alias;
    /**
     * 设置节点别名。
     *
     * @param alias 节点别名
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Java 类型到 SQL 类型的映射表。
     */
    static final Map<Class<?>, String> SQL_TYPE_MAP = Stream.of(
                    new SimpleEntry<>(Integer.class, "int"),
                    new SimpleEntry<>(Long.class, "bigint"),
                    new SimpleEntry<>(Float.class, "float"),
                    new SimpleEntry<>(Double.class, "float8"),
                    new SimpleEntry<>(String.class, "text"),
                    new SimpleEntry<>(UUID.class, "uuid"),
                    new SimpleEntry<>(Boolean.class, "boolean"),
                    // 默认类型
                    new SimpleEntry<>(Object.class, "text"))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    /**
     * 将过滤条件映射为 WHERE 子句片段。
     *
     * @param filter 过滤条件
     * @return WHERE 子句片段
     */
    public String map(Filter filter) {
        if (filter instanceof IsEqualTo filterInst) {
            return mapEqual(filterInst);
        } else if (filter instanceof IsNotEqualTo filterInst) {
            return mapNotEqual(filterInst);
        } else if (filter instanceof IsGreaterThan filterInst) {
            return mapGreaterThan(filterInst);
        } else if (filter instanceof IsGreaterThanOrEqualTo filterInst) {
            return mapGreaterThanOrEqual(filterInst);
        } else if (filter instanceof IsLessThan filterInst) {
            return mapLessThan(filterInst);
        } else if (filter instanceof IsLessThanOrEqualTo filterInst) {
            return mapLessThanOrEqual(filterInst);
        } else if (filter instanceof IsIn filterInst) {
            return mapIn(filterInst);
        } else if (filter instanceof IsNotIn filterInst) {
            return mapNotIn(filterInst);
        } else if (filter instanceof And filterInst) {
            return mapAnd(filterInst);
        } else if (filter instanceof Not filterInst) {
            return mapNot(filterInst);
        } else if (filter instanceof Or filterInst) {
            return mapOr(filterInst);
        } else if (filter instanceof GraphContains filterInst) {
            return mapContains(filterInst);
        } else {
            throw new UnsupportedOperationException("Unsupported filter type: " + filter.getClass().getName());
        }
    }
    /**
     * 映射等值条件。
     *
     * @param isEqualTo 等值条件
     * @return WHERE 子句片段
     */
    private String mapEqual(IsEqualTo isEqualTo) {
        String key = formatKey(isEqualTo.key(), isEqualTo.comparisonValue().getClass());
        return format("%s is not null and %s = %s", key, key,
                formatValue(isEqualTo.comparisonValue()));
    }
    /**
     * 映射不等条件。
     *
     * @param isNotEqualTo 不等条件
     * @return WHERE 子句片段
     */
    private String mapNotEqual(IsNotEqualTo isNotEqualTo) {
        String key = formatKey(isNotEqualTo.key(), isNotEqualTo.comparisonValue().getClass());
        return format("%s is null or %s != %s", key, key,
                formatValue(isNotEqualTo.comparisonValue()));
    }
    /**
     * 映射大于条件。
     *
     * @param isGreaterThan 大于条件
     * @return WHERE 子句片段
     */
    private String mapGreaterThan(IsGreaterThan isGreaterThan) {
        return format("%s > %s", formatKey(isGreaterThan.key(), isGreaterThan.comparisonValue().getClass()),
                formatValue(isGreaterThan.comparisonValue()));
    }
    /**
     * 映射大于等于条件。
     *
     * @param isGreaterThanOrEqualTo 大于等于条件
     * @return WHERE 子句片段
     */
    private String mapGreaterThanOrEqual(IsGreaterThanOrEqualTo isGreaterThanOrEqualTo) {
        return format("%s >= %s", formatKey(isGreaterThanOrEqualTo.key(), isGreaterThanOrEqualTo.comparisonValue().getClass()),
                formatValue(isGreaterThanOrEqualTo.comparisonValue()));
    }
    /**
     * 映射小于条件。
     *
     * @param isLessThan 小于条件
     * @return WHERE 子句片段
     */
    private String mapLessThan(IsLessThan isLessThan) {
        return format("%s < %s", formatKey(isLessThan.key(), isLessThan.comparisonValue().getClass()),
                formatValue(isLessThan.comparisonValue()));
    }
    /**
     * 映射小于等于条件。
     *
     * @param isLessThanOrEqualTo 小于等于条件
     * @return WHERE 子句片段
     */
    private String mapLessThanOrEqual(IsLessThanOrEqualTo isLessThanOrEqualTo) {
        return format("%s <= %s", formatKey(isLessThanOrEqualTo.key(), isLessThanOrEqualTo.comparisonValue().getClass()),
                formatValue(isLessThanOrEqualTo.comparisonValue()));
    }
    /**
     * 映射 IN 条件。
     *
     * @param isIn IN 条件
     * @return WHERE 子句片段
     */
    private String mapIn(IsIn isIn) {
        return format("%s in %s", formatJsonKeyAsString(isIn.key()), formatValuesAsString(isIn.comparisonValues()));
    }
    /**
     * 映射 NOT IN 条件。
     *
     * @param isNotIn NOT IN 条件
     * @return WHERE 子句片段
     */
    private String mapNotIn(IsNotIn isNotIn) {
        String key = formatKeyAsString(isNotIn.key());
        return format("%s is null or %s not in %s", key, key, formatValuesAsString(isNotIn.comparisonValues()));
    }
    /**
     * 映射 AND 条件。
     *
     * @param and AND 条件
     * @return WHERE 子句片段
     */
    private String mapAnd(And and) {
        return format("%s and %s", map(and.left()), map(and.right()));
    }
    /**
     * 映射 NOT 条件。
     *
     * @param not NOT 条件
     * @return WHERE 子句片段
     */
    private String mapNot(Not not) {
        return format("not(%s)", map(not.expression()));
    }
    /**
     * 映射 OR 条件。
     *
     * @param or OR 条件
     * @return WHERE 子句片段
     */
    private String mapOr(Or or) {
        return format("(%s or %s)", map(or.left()), map(or.right()));
    }
    /**
     * 映射包含条件。
     *
     * @param contains 包含条件
     * @return WHERE 子句片段
     */
    private String mapContains(GraphContains contains) {
        return format("(%s contains %s)", formatKeyAsString(contains.key()), formatValue(contains.value()));
    }

    /**
     * 格式化字段键（带类型）。
     *
     * @param key       字段名
     * @param valueType 值类型
     * @return 格式化后的字段键
     */
    abstract String formatKey(String key, Class<?> valueType);

    /**
     * 格式化字段键为字符串形式。
     *
     * @param key 字段名
     * @return 格式化后的字段键
     */
    abstract String formatKeyAsString(String key);

    /**
     * 格式化 JSON 字段键为字符串形式。
     *
     * @param key 字段名
     * @return 格式化后的字段键
     */
    abstract String formatJsonKeyAsString(String key);

    /**
     * 格式化值为 SQL 字面量。
     *
     * @param value 值
     * @return SQL 字面量
     */
    String formatValue(Object value) {
        if (value instanceof String || value instanceof UUID) {
            return "'" + value + "'";
        } else {
            return value.toString();
        }
    }

    /**
     * 格式化集合为 SQL 列表字面量。
     *
     * @param values 值集合
     * @return SQL 列表字面量
     */
    String formatValuesAsString(Collection<?> values) {
        return "[" + values.stream().map(v -> String.format("'%s'", v))
                .collect(Collectors.joining(",")) + "]";
    }

    /**
     * 格式化集合为 JSON 字符串列表字面量。
     *
     * @param values 值集合
     * @return JSON 列表字面量
     */
    String formatJsonValuesAsString(Collection<?> values) {
        return "[" + values.stream().map(v -> String.format("'%s'", v))
                .collect(Collectors.joining(",")) + "]";
    }
}
