package com.moyz.adi.common.rag;

import java.util.HashMap;
import java.util.Map;

/**
 * 图谱 RAG 实例的全局注册表。
 */
public class GraphRagContext {

    /**
     * 实例名称与 RAG 实例的映射。
     */
    protected static final Map<String, GraphRag> NAME_TO_RAG = new HashMap<>();

    /**
     * 禁止实例化。
     */
    private GraphRagContext() {

    }

    /**
     * 获取指定名称的 RAG 实例。
     *
     * @param name 实例名称
     * @return RAG 实例
     */
    public static GraphRag get(String name) {
        return NAME_TO_RAG.get(name);
    }

    /**
     * 注册 RAG 实例。
     *
     * @param rag RAG 实例
     */
    public static void add(GraphRag rag) {
        NAME_TO_RAG.put(rag.getName(), rag);
    }
}
