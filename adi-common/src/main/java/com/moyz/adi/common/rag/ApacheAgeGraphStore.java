package com.moyz.adi.common.rag;

import com.google.common.base.Joiner;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.util.AdiStringUtil;
import com.moyz.adi.common.util.GraphStoreUtil;
import com.moyz.adi.common.util.JsonUtil;
import com.moyz.adi.common.vo.*;
import lombok.Builder;
import org.apache.age.jdbc.base.Agtype;
import org.apache.age.jdbc.base.type.AgtypeMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.postgresql.jdbc.PgConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.moyz.adi.common.enums.ErrorEnum.B_DB_ERROR;
import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.*;

/**
 * 基于 Apache AGE 的图谱存储实现。
 */
public class ApacheAgeGraphStore implements GraphStore {

    /**
     * 日志记录器。
     */
    private static final Logger log = LoggerFactory.getLogger(ApacheAgeGraphStore.class);

    /**
     * 数据库主机。
     */
    private final String host;
    /**
     * 数据库端口。
     */
    private final Integer port;
    /**
     * 数据库用户名。
     */
    private final String user;
    /**
     * 数据库密码。
     */
    private final String password;
    /**
     * 数据库名称。
     */
    private final String database;
    /**
     * 图谱名称。
     */
    private final String graph;

    /**
     * 构建 Apache AGE 图谱存储。
     *
     * @param host 主机
     * @param port 端口
     * @param user 用户名
     * @param password 密码
     * @param database 数据库名称
     * @param graphName 图谱名称
     * @param createGraph 是否创建图谱
     * @param dropGraphFirst 是否先删除旧图谱
     */
    @Builder
    public ApacheAgeGraphStore(String host,
                               Integer port,
                               String user,
                               String password,
                               String database,
                               String graphName,
                               Boolean createGraph,
                               Boolean dropGraphFirst) {
        this.host = ensureNotBlank(host, "host");
        this.port = ensureGreaterThanZero(port, "port");
        this.user = ensureNotBlank(user, "user");
        this.password = ensureNotBlank(password, "password");
        this.database = ensureNotBlank(database, "database");
        this.graph = ensureNotBlank(graphName, "graph");

        createGraph = getOrDefault(createGraph, true);
        dropGraphFirst = getOrDefault(dropGraphFirst, false);

        try (Connection connection = setupConnection();
             Statement stmt = connection.createStatement()) {
            if (Boolean.TRUE.equals(dropGraphFirst)) {
                stmt.executeUpdate(String.format("SELECT * FROM drop_graph('%s', true)", graph));
            }
            if (Boolean.TRUE.equals(createGraph)) {
                ResultSet resultSet = stmt.executeQuery(String.format("SELECT * FROM ag_graph WHERE name = '%s'", graph));
                if (!resultSet.isBeforeFirst() && resultSet.getRow() == 0) {
                    stmt.execute(String.format("SELECT * FROM ag_catalog.create_graph('%s')", graph));
                }
            }
        } catch (SQLException e) {
            log.error("ApacheAgeGraphStore init error", e);
            throw new BaseException(B_DB_ERROR);
        }
    }

    /**
     * 批量新增顶点。
     *
     * @param vertexes 顶点列表
     * @return 是否新增成功
     */
    @Override
    public boolean addVertexes(List<GraphVertex> vertexes) {
        ensureNotEmpty(vertexes, vertexes.toString());
        try (Connection connection = setupConnection()) {
            for (GraphVertex vertex : vertexes) {
                vertex.setName(AdiStringUtil.tail(vertex.getName(), 20));
                String label = vertex.getLabel();
                String prepareSql = """
                        SELECT *
                        FROM cypher('%s', $$
                            create (%s {name:$name,text_segment_id:$text_segment_id,description:$description,metadata:$metadata})
                        $$, ?) as (a agtype);
                        """.formatted(graph, StringUtils.isNotBlank(label) ? ":" + label : "");
                log.info("addVertex prepareSql:{}", prepareSql);
                try (PreparedStatement upsertStmt = connection.prepareStatement(prepareSql)) {
                    Agtype agtype = new Agtype();
                    agtype.setValue(JsonUtil.toJson(vertex));
                    upsertStmt.setObject(1, agtype);
                    upsertStmt.execute();
                }
            }
        } catch (SQLException e) {
            log.error("addVertex error", e);
            throw new BaseException(B_DB_ERROR);
        }
        return true;
    }

    /**
     * 新增单个顶点。
     *
     * @param vertex 顶点
     * @return 是否新增成功
     */
    @Override
    public boolean addVertex(GraphVertex vertex) {
        log.info("Add vertex:{}", vertex);
        ensureNotNull(vertex, vertex.toString());
        ensureNotEmpty(vertex.getMetadata(), "Metadata");
        return addVertexes(List.of(vertex));
    }

    /**
     * 更新顶点信息。
     *
     * @param updateInfo 更新信息
     * @return 更新后的顶点
     */
    @Override
    public GraphVertex updateVertex(GraphVertexUpdateInfo updateInfo) {
        log.info("Update vertex:{}", updateInfo.getNewData());
        ensureNotNull(updateInfo.getMetadataFilter(), "Metadata filter");
        GraphVertex newData = updateInfo.getNewData();
        ensureNotNull(newData, newData.toString());

        try (Connection connection = setupConnection()) {

            GraphSearchCondition whereCondition = GraphSearchCondition.builder()
                    .names(List.of(updateInfo.getName()))
                    .metadataFilter(updateInfo.getMetadataFilter())
                    .build();
            String whereClause = GraphStoreUtil.buildWhereClause(whereCondition, "v");
            String setClause = GraphStoreUtil.buildSetClause(updateInfo.getNewData().getMetadata());
            String prepareSql = """
                    select * from cypher('%s', $$
                       match (v)
                       where %s
                       set v.text_segment_id=$new_text_segment_id,v.description=$new_description%s
                       return v
                       limit 1
                    $$, ?) as (v agtype);
                    """.formatted(graph, whereClause, setClause);
            log.info("updateVertex prepareSql:{}", prepareSql);
            PreparedStatement stmt = connection.prepareStatement(prepareSql);

            Map<String, Object> whereArgs = GraphStoreUtil.buildWhereArgs(whereCondition, "v");
            Map<String, Object> setArgs = GraphStoreUtil.buildSetArgs(updateInfo.getNewData().getMetadata());
            whereArgs.putAll(setArgs);
            whereArgs.putAll(Map.of("new_text_segment_id", newData.getTextSegmentId(), "new_description", newData.getDescription()));
            log.info("updateVertex args:{}", whereArgs);

            Agtype agtype = new Agtype();
            agtype.setValue(JsonUtil.toJson(whereArgs));
            stmt.setObject(1, agtype);
            stmt.execute();
            return getVertexFromResultSet(stmt.getResultSet());
        } catch (SQLException e) {
            log.error("updateVertex error", e);
            throw new BaseException(B_DB_ERROR);
        }
    }

    /**
     * 获取单个顶点。
     *
     * @param search 查询条件
     * @return 顶点信息
     */
    @Override
    public GraphVertex getVertex(GraphVertexSearch search) {
        List<GraphVertex> list = this.searchVertices(search);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 批量获取顶点。
     *
     * @param ids 顶点 ID 列表
     * @return 顶点列表
     */
    @Override
    public List<GraphVertex> getVertices(List<String> ids) {
        List<Long> longIds = ids.stream().map(Long::parseLong).toList();
        try (Connection connection = setupConnection()) {
            String query = """
                    select * from cypher('%s', $$
                        match (v)
                        where id(v) in [%s]
                        return v
                    $$) as (v agtype);
                    """.formatted(graph, Joiner.on(",").join(longIds));
            log.info("getVertices query:{}", query);
            try (Statement stmt = connection.createStatement()) {
                ResultSet resultSet = stmt.executeQuery(query);
                return getVerticesFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            log.error("getVertices error", e);
            throw new BaseException(B_DB_ERROR);
        }
    }

    /**
     * 搜索顶点。
     *
     * @param search 查询条件
     * @return 顶点列表
     */
    @Override
    public List<GraphVertex> searchVertices(GraphVertexSearch search) {
        try (Connection connection = setupConnection()) {
            String label = search.getLabel();
            String whereClause = GraphStoreUtil.buildWhereClause(search, "v");
            String query = """
                    select * from cypher('%s', $$
                        match (%s)
                        with v
                        order by id(v) desc
                        where %s and id(v) < %d
                        return v
                        limit %d
                    $$,?) as (v agtype);
                    """.formatted(graph, StringUtils.isNotBlank(label) ? "v:" + label : "v", whereClause, search.getMaxId(), search.getLimit());
            log.info("SearchVertices prepareSql:{}", query);
            try (PreparedStatement selectStmt = connection.prepareStatement(query)) {
                Map<String, Object> whereArgs = GraphStoreUtil.buildWhereArgs(search, "v");
                log.info("getVertex args:{}", whereArgs);
                Agtype agtype = new Agtype();
                agtype.setValue(JsonUtil.toJson(whereArgs));
                selectStmt.setObject(1, agtype);
                ResultSet resultSet = selectStmt.executeQuery();
                return getVerticesFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            log.error("searchVertices error", e);
            throw new BaseException(B_DB_ERROR);
        }
    }

    /**
     * 批量获取边及两端顶点。
     *
     * @param ids 边 ID 列表
     * @return 边与顶点三元组列表
     */
    @Override
    public List<Triple<GraphVertex, GraphEdge, GraphVertex>> getEdges(List<String> ids) {
        try (Connection connection = setupConnection()) {
            String query = """
                    select * from cypher('%s', $$
                        match (v1)-[e]->(v2)
                        where id(e) in [%s]
                        return v1,e,v2
                    $$) as (e agtype);
                    """.formatted(graph, Joiner.on(",").join(ids));
            log.info("getEdges query:{}", query);
            try (Statement stmt = connection.createStatement()) {
                ResultSet resultSet = stmt.executeQuery(query);
                return getEdgesFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            log.error("getEdges error", e);
            throw new BaseException(B_DB_ERROR);
        }
    }

    /**
     * 搜索边及两端顶点。
     *
     * @param search 查询条件
     * @return 边与顶点三元组列表
     */
    @Override
    public List<Triple<GraphVertex, GraphEdge, GraphVertex>> searchEdges(GraphEdgeSearch search) {
        try (Connection connection = setupConnection()) {
            String filterClause1 = GraphStoreUtil.buildWhereClause(search.getSource(), "v1");
            String filterClause2 = GraphStoreUtil.buildWhereClause(search.getTarget(), "v2");
            String filterClause3 = GraphStoreUtil.buildWhereClause(search.getEdge(), "e");
            String filterClause = filterClause1;
            if (StringUtils.isNotBlank(filterClause2)) {
                filterClause += StringUtils.isNotBlank(filterClause) ? " and " + filterClause2 : filterClause2;
            }
            if (StringUtils.isNotBlank(filterClause3)) {
                filterClause += StringUtils.isNotBlank(filterClause) ? " and " + filterClause3 : filterClause3;
            }
            String query = """
                    select * from cypher('%s', $$
                        match (v1)-[e]-(v2)
                        with v1,e,v2
                        order by id(e) desc
                        where %s and id(e) < %d
                        return v1,e,v2
                        limit %d
                    $$,?) as (v1 agtype,e agtype,v2 agtype);
                    """.formatted(graph, filterClause, search.getMaxId(), search.getLimit());
            log.info("Search edges prepareSql:\n{}", query);
            try (PreparedStatement selectStmt = connection.prepareStatement(query)) {
                Map<String, Object> whereArgs1 = GraphStoreUtil.buildWhereArgs(search.getSource(), "v1");
                Map<String, Object> whereArgs2 = GraphStoreUtil.buildWhereArgs(search.getTarget(), "v2");
                Map<String, Object> whereArgs3 = GraphStoreUtil.buildWhereArgs(search.getEdge(), "e");
                whereArgs1.putAll(whereArgs2);
                whereArgs1.putAll(whereArgs3);
                Agtype agtype = new Agtype();
                agtype.setValue(JsonUtil.toJson(whereArgs1));
                selectStmt.setObject(1, agtype);
                log.info("Search edges args:{}", agtype);
                ResultSet resultSet = selectStmt.executeQuery();
                return getEdgesFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            log.error("searchEdges error", e);
            throw new BaseException(B_DB_ERROR);
        }
    }

    /**
     * 获取单条边及两端顶点。
     *
     * @param search 查询条件
     * @return 边与顶点三元组
     */
    @Override
    public Triple<GraphVertex, GraphEdge, GraphVertex> getEdge(GraphEdgeSearch search) {
        List<Triple<GraphVertex, GraphEdge, GraphVertex>> list = this.searchEdges(search);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 新增边并返回完整信息。
     *
     * @param addInfo 新增信息
     * @return 边与顶点三元组
     */
    @Override
    public Triple<GraphVertex, GraphEdge, GraphVertex> addEdge(GraphEdgeAddInfo addInfo) {
        ensureNotNull(addInfo.getEdge(), "Grahp edge");
        try (Connection connection = setupConnection()) {
            String whereClause1 = GraphStoreUtil.buildWhereClause(addInfo.getSourceFilter(), "v1");
            String whereClause2 = GraphStoreUtil.buildWhereClause(addInfo.getTargetFilter(), "v2");
            String prepareSql = """
                    select * from cypher('%s', $$
                      match (v1), (v2)
                      where %s
                      create (v1)-[e:RELTYPE {text_segment_id:$text_segment_id,weight:$weight,description:$description,metadata:$metadata}]->(v2)
                      return v1,e,v2
                    $$, ?) as (v1 agtype,e agtype,v2 agtype);
                    """.formatted(graph, whereClause1 + " and " + whereClause2);
            log.info("Add edge prepareSql:{}", prepareSql);
            try (PreparedStatement preparedStatement = connection.prepareStatement(prepareSql)) {
                Map<String, Object> whereArgs1 = GraphStoreUtil.buildWhereArgs(addInfo.getSourceFilter(), "v1");
                Map<String, Object> whereArgs2 = GraphStoreUtil.buildWhereArgs(addInfo.getTargetFilter(), "v2");
                whereArgs1.putAll(whereArgs2);
                whereArgs1.putAll(JsonUtil.toMap(addInfo.getEdge()));
                Agtype agtype = new Agtype();
                agtype.setValue(JsonUtil.toJson(whereArgs1));
                preparedStatement.setObject(1, agtype);
                preparedStatement.execute();
                return getEdgeFromResultSet(preparedStatement.getResultSet());
            }
        } catch (SQLException e) {
            log.error("addEdge error", e);
            throw new BaseException(B_DB_ERROR);
        }
    }

    /**
     * 更新边并返回完整信息。
     *
     * @param edgeEditInfo 编辑信息
     * @return 边与顶点三元组
     */
    @Override
    public Triple<GraphVertex, GraphEdge, GraphVertex> updateEdge(GraphEdgeEditInfo edgeEditInfo) {
        log.info("Update edge:{}", edgeEditInfo);
        ensureNotNull(edgeEditInfo.getEdge(), "Graph edit info");
        GraphEdge newData = edgeEditInfo.getEdge();
        try (Connection connection = setupConnection()) {
            String whereClause1 = GraphStoreUtil.buildWhereClause(edgeEditInfo.getSourceFilter(), "v1");
            String whereClause2 = GraphStoreUtil.buildWhereClause(edgeEditInfo.getTargetFilter(), "v2");
            String setClause = GraphStoreUtil.buildSetClause(edgeEditInfo.getEdge().getMetadata());
            String prepareSql = """
                    select * from cypher('%s', $$
                       match (v1)-[e]->(v2)
                       where %s
                       set e.weight=$new_weight,e.text_segment_id=$new_text_segment_id,e.description=$new_description %s
                       return v1,e,v2
                    $$, ?) as (v1 agtype,e agtype,v2 agtype);
                    """.formatted(graph, whereClause1 + " and " + whereClause2, setClause);
            log.info("updateEdge prepareSql:{}", prepareSql);
            try (PreparedStatement upsertStmt = connection.prepareStatement(prepareSql)) {
                Map<String, Object> whereArgs1 = GraphStoreUtil.buildWhereArgs(edgeEditInfo.getSourceFilter(), "v1");
                Map<String, Object> whereArgs2 = GraphStoreUtil.buildWhereArgs(edgeEditInfo.getTargetFilter(), "v2");
                Map<String, Object> setArgs = GraphStoreUtil.buildSetArgs(edgeEditInfo.getEdge().getMetadata());
                whereArgs1.putAll(whereArgs2);
                whereArgs1.putAll(setArgs);
                whereArgs1.putAll(
                        Map.of(
                                "new_text_segment_id", newData.getTextSegmentId(),
                                "new_weight", newData.getWeight(),
                                "new_description", newData.getDescription()
                        )
                );
                Agtype agtype = new Agtype();
                agtype.setValue(JsonUtil.toJson(whereArgs1));
                upsertStmt.setObject(1, agtype);
                upsertStmt.execute();
                return getEdgeFromResultSet(upsertStmt.getResultSet());
            }
        } catch (SQLException e) {
            log.error("updateEdge error", e);
            throw new BaseException(B_DB_ERROR);
        }
    }

    /**
     * 删除顶点（可选删除关联边）。
     *
     * @param filter 过滤条件
     * @param includeEdges 是否包含边
     */
    @Override
    public void deleteVertices(GraphSearchCondition filter, boolean includeEdges) {
        ensureNotNull(filter, "Data filter");
        ensureNotNull(filter.getMetadataFilter(), "Metadata filter");
        try (Connection connection = setupConnection()) {
            String whereClause = GraphStoreUtil.buildWhereClause(filter, "v");
            String prepareSql = """
                     select * from cypher('%s', $$
                      match (v)
                      where %s %s
                    $$,?) as (v agtype);
                    """.formatted(graph, whereClause, includeEdges ? "DETACH DELETE v" : "DELETE v");
            log.info("deleteVertices prepareSql:{}", prepareSql);
            try (PreparedStatement upsertStmt = connection.prepareStatement(prepareSql)) {
                Map<String, Object> whereArgs = GraphStoreUtil.buildWhereArgs(filter, "v");
                Agtype agtype = new Agtype();
                agtype.setValue(JsonUtil.toJson(whereArgs));
                upsertStmt.setObject(1, agtype);
                upsertStmt.execute();
            }
        } catch (SQLException e) {
            log.error("deleteVertices error", e);
            throw new BaseException(B_DB_ERROR);
        }
    }

    /**
     * 单独删除边。
     *
     * @param filter 过滤条件
     */
    @Override
    public void deleteEdges(GraphSearchCondition filter) {
        ensureNotNull(filter, "Data filter");
        try (Connection connection = setupConnection()) {
            String whereClause = GraphStoreUtil.buildWhereClause(filter, "r");
            String prepareSql = """
                    select * from cypher('%s', $$
                        match ()-[r]->()
                        where %s
                        delete r
                    $$,?) as (r agtype);
                    """.formatted(graph, whereClause);
            log.info("deleteEdges prepareSql:{}", prepareSql);
            try (PreparedStatement upsertStmt = connection.prepareStatement(prepareSql)) {
                Map<String, Object> whereArgs = GraphStoreUtil.buildWhereArgs(filter, "r");
                Agtype agtype = new Agtype();
                agtype.setValue(JsonUtil.toJson(whereArgs));
                upsertStmt.setObject(1, agtype);
                upsertStmt.execute();
            }
        } catch (SQLException e) {
            log.error("deleteEdges sql exception", e);
            throw new BaseException(B_DB_ERROR);
        }
    }

    /**
     * 将结果集转换为边与顶点三元组列表。
     *
     * @param resultSet 结果集
     * @return 三元组列表
     */
    private List<Triple<GraphVertex, GraphEdge, GraphVertex>> getEdgesFromResultSet(ResultSet resultSet) {
        List<Triple<GraphVertex, GraphEdge, GraphVertex>> result = new ArrayList<>();
        try {
            while (resultSet.next()) {
                Agtype source = resultSet.getObject(1, Agtype.class);
                Agtype edge = resultSet.getObject(2, Agtype.class);
                Agtype target = resultSet.getObject(3, Agtype.class);
                result.add(Triple.of(agTypeToVertex(source), agTypeToEdge(edge), agTypeToVertex(target)));
            }
        } catch (SQLException e) {
            log.error("getEdgesFromResultSet error", e);
            throw new BaseException(B_DB_ERROR);
        }
        return result;
    }

    /**
     * 获取单个边的结果。
     *
     * @param resultSet 结果集
     * @return 边与顶点三元组
     */
    public Triple<GraphVertex, GraphEdge, GraphVertex> getEdgeFromResultSet(ResultSet resultSet) {
        List<Triple<GraphVertex, GraphEdge, GraphVertex>> list = getEdgesFromResultSet(resultSet);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 获取单个顶点的结果。
     *
     * @param resultSet 结果集
     * @return 顶点
     */
    public GraphVertex getVertexFromResultSet(ResultSet resultSet) {
        List<GraphVertex> vertices = getVerticesFromResultSet(resultSet);
        if (vertices.isEmpty()) {
            return null;
        }
        return vertices.get(0);
    }

    /**
     * 将结果集转换为顶点列表。
     *
     * @param resultSet 结果集
     * @return 顶点列表
     */
    public List<GraphVertex> getVerticesFromResultSet(ResultSet resultSet) {
        List<GraphVertex> vertices = new ArrayList<>();
        try {
            while (resultSet.next()) {
                Agtype returnedAgtype = resultSet.getObject(1, Agtype.class);
                vertices.add(agTypeToVertex(returnedAgtype));
            }
        } catch (SQLException e) {
            log.error("getVerticesFromResultSet error", e);
            throw new BaseException(B_DB_ERROR);
        }
        return vertices;
    }

    /**
     * 将 Agtype 转换为 GraphVertex。
     *
     * @param agtype AGE 类型对象
     * @return 顶点对象
     */
    public GraphVertex agTypeToVertex(Agtype agtype) {
        AgtypeMap agtypeMap = agtype.getMap();
        String id = String.valueOf(agtypeMap.getLong("id"));
        String label = agtypeMap.getObject("label").toString();
        AgtypeMap nodeProps = agtypeMap.getMap("properties");
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : nodeProps.getMap("metadata").entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return GraphVertex.builder()
                .id(id)
                .label(label)
                .name(nodeProps.getString("name"))
                .description(nodeProps.getString("description"))
                .textSegmentId(nodeProps.getString("text_segment_id"))
                .metadata(map)
                .build();
    }

    /**
     * 将 Agtype 转换为 GraphEdge。
     *
     * @param agtype AGE 类型对象
     * @return 边对象
     */
    private GraphEdge agTypeToEdge(Agtype agtype) {
        AgtypeMap agtypeMap = agtype.getMap();
        Long id = agtypeMap.getLong("id");
        Long startId = agtypeMap.getLong("start_id");
        Long endId = agtypeMap.getLong("end_id");
        String nodeLabel = agtypeMap.getObject("label").toString();
        AgtypeMap nodeProps = agtypeMap.getMap("properties");
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : nodeProps.getMap("metadata").entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return GraphEdge.builder()
                .id(id + "")
                .startId(startId + "")
                .endId(endId + "")
                .label(nodeLabel)
                .weight(null == nodeProps.getObject("weight") ? 0 : nodeProps.getDouble("weight"))
                .description(nodeProps.getString("description"))
                .textSegmentId(nodeProps.getString("text_segment_id"))
                .metadata(map)
                .build();
    }

    @SuppressWarnings("java:S2095")
    /**
     * 创建数据库连接并完成必要初始化。
     *
     * @return 数据库连接
     * @throws SQLException SQL 异常
     */
    private Connection setupConnection() throws SQLException {
        PgConnection connection = DriverManager.getConnection(
                String.format("jdbc:postgresql://%s:%s/%s", host, port, database),
                user,
                password
        ).unwrap(PgConnection.class);
        try (Statement stmt = connection.createStatement()) {
            connection.addDataType("agtype", Agtype.class);
            stmt.execute("LOAD 'age'");
            stmt.execute("SET search_path = ag_catalog, \"$user\", public;");
        }
        return connection;
    }
}
