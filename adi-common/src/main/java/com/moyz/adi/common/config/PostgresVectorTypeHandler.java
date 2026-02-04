package com.moyz.adi.common.config;

import com.pgvector.PGvector;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * PostgreSQL 向量类型处理器。
 */
public class PostgresVectorTypeHandler extends BaseTypeHandler<PGvector> {

    /**
     * 写入非空向量参数。
     *
     * @param ps 预编译语句
     * @param i 参数索引
     * @param parameter 向量参数
     * @param jdbcType JDBC 类型
     * @throws SQLException SQL 异常
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, PGvector parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setObject(i, parameter);
//        ps.setArray(i, ps.getConnection().createArrayOf("float", parameter));
    }

    /**
     * 通过列名读取向量。
     *
     * @param rs 结果集
     * @param columnName 列名
     * @return 向量
     * @throws SQLException SQL 异常
     */
    @Override
    public PGvector getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toFloatArray(rs.getArray(columnName));
    }

    /**
     * 通过列索引读取向量。
     *
     * @param rs 结果集
     * @param columnIndex 列索引
     * @return 向量
     * @throws SQLException SQL 异常
     */
    @Override
    public PGvector getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toFloatArray(rs.getArray(columnIndex));
    }

    /**
     * 通过存储过程列索引读取向量。
     *
     * @param cs 存储过程
     * @param columnIndex 列索引
     * @return 向量
     * @throws SQLException SQL 异常
     */
    @Override
    public PGvector getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toFloatArray(cs.getArray(columnIndex));
    }

    /**
     * SQL 数组转换为 PGvector。
     *
     * @param sqlArray SQL 数组
     * @return PGvector
     * @throws SQLException SQL 异常
     */
    private PGvector toFloatArray(java.sql.Array sqlArray) throws SQLException {
        PGvector pGvector = new PGvector(new float[0]);
        if (sqlArray == null) {
            return pGvector;
        }
        pGvector.setValue(sqlArray.toString());
        return pGvector;
    }

}
