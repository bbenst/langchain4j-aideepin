package com.moyz.adi.common.config;

import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.moyz.adi.common.util.AdiPropertiesUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 向量表动态表名处理器。
 */
public class EmbeddingTableNameHandler implements TableNameHandler {

    /**
     * 需要进行动态表名处理的表集合。
     */
    private List<String> tableNames;

    /**
     * 构造动态表名处理器。
     *
     * @param tableNames 需要处理的表名列表
     */
    public EmbeddingTableNameHandler(String... tableNames) {
        this.tableNames = Arrays.asList(tableNames);
    }

    /**
     * 根据配置后缀返回实际表名。
     *
     * @param sql 原始 SQL
     * @param tableName 原始表名
     * @return 动态表名
     */
    @Override
    public String dynamicTableName(String sql, String tableName) {
        if (this.tableNames.contains(tableName) && StringUtils.isNotBlank(AdiPropertiesUtil.EMBEDDING_TABLE_SUFFIX)) {
            return tableName + "_" + AdiPropertiesUtil.EMBEDDING_TABLE_SUFFIX;
        } else {
            // 未命中或无后缀配置时，保持表名不变
            return tableName;
        }
    }
}
