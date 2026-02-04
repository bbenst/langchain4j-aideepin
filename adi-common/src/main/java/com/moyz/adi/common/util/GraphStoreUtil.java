package com.moyz.adi.common.util;

import com.moyz.adi.common.vo.GraphSearchCondition;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
/**
 * 图谱存储查询条件拼装工具类。
 */
public class GraphStoreUtil {
    /**
     * 工具类禁止实例化。
     */
    private GraphStoreUtil() {
    }
    /**
     * 构建 WHERE 条件语句。
     *
     * @param search 搜索条件
     * @param alias  节点别名
     * @return WHERE 子句
     */
    public static String buildWhereClause(GraphSearchCondition search, String alias) {
        if (null == search) {
            return StringUtils.EMPTY;
        }
        StringBuilder whereClause = new StringBuilder();
        if (CollectionUtils.isNotEmpty(search.getNames())) {
            List<String> nameArgs = new ArrayList<>();
            for (int i = 0; i < search.getNames().size(); i++) {
                nameArgs.add("$" + alias + "_name_" + i);
            }
            whereClause.append(String.format("(%s.name in [%s])", alias, String.join(",", nameArgs)));
        }
        //Metadata直接拼接字符串
        if (null != search.getMetadataFilter()) {
            if (!whereClause.isEmpty()) {
                whereClause.append(" and ");
            }
            AdiApacheAgeJSONFilterMapper adiJSONFilterMapper = new AdiApacheAgeJSONFilterMapper("metadata");
            adiJSONFilterMapper.setAlias(alias);
            String metadataWhereClause = adiJSONFilterMapper.map(search.getMetadataFilter());
            whereClause.append(metadataWhereClause);
        }

        return whereClause.toString();
    }
    /**
     * 构建 WHERE 参数。
     *
     * @param search 搜索条件
     * @param alias  节点别名
     * @return 参数 Map
     */
    public static Map<String, Object> buildWhereArgs(GraphSearchCondition search, String alias) {
        if (null == search) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new HashMap<>();
        if (CollectionUtils.isNotEmpty(search.getNames())) {
            for (int i = 0; i < search.getNames().size(); i++) {
                result.put(alias + "_name_" + i, search.getNames().get(i));
            }
        }
        return result;
    }
    /**
     * 构建 SET 子句。
     *
     * @param metadata 元数据
     * @return SET 子句
     */
    public static String buildSetClause(Map<String, Object> metadata) {
        //Apache AGE不支持直接更新property中的Map或List，只能直接替换，否则会出现异常：ERROR:  SET clause doesn't not support updating maps or lists in a property
        StringBuilder setClause = new StringBuilder();
        if (MapUtils.isNotEmpty(metadata)) {
            setClause.append(",v.metadata=$new_metadata");
        }
        return setClause.toString();
    }
    /**
     * 构建 SET 参数。
     *
     * @param metadata 元数据
     * @return 参数 Map
     */
    public static Map<String, Object> buildSetArgs(Map<String, Object> metadata) {
        Map<String, Object> result = new HashMap<>();
        if (MapUtils.isNotEmpty(metadata)) {
            result.put("new_metadata", metadata);
        }

        return result;
    }
}
