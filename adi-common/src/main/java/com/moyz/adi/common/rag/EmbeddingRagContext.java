package com.moyz.adi.common.rag;

import java.util.HashMap;
import java.util.Map;

/**
 * 向量 RAG 实例的全局注册表。
 */
public class EmbeddingRagContext {

    /**
     * 实例名称与 RAG 实例的映射。
     */
    protected static final Map<String, EmbeddingRag> NAME_TO_RAG = new HashMap<>();

    /**
     * 禁止实例化。
     */
    private EmbeddingRagContext() {
    }

    /**
     * 获取指定名称的 RAG 实例。
     *
     * @param name 实例名称
     * @return RAG 实例
     */
    public static EmbeddingRag get(String name) {
        return NAME_TO_RAG.get(name);
    }

    /**
     * 注册 RAG 实例。
     *
     * @param rag RAG 实例
     */
    public static void add(EmbeddingRag rag) {
        NAME_TO_RAG.put(rag.getName(), rag);
    }
}
