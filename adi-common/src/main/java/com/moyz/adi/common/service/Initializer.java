package com.moyz.adi.common.service;

import com.moyz.adi.common.config.AdiProperties;
import com.moyz.adi.common.cosntant.AdiConstant;
import com.moyz.adi.common.file.AliyunOssFileHelper;
import com.moyz.adi.common.file.AliyunOssFileOperator;
import com.moyz.adi.common.file.LocalFileOperator;
import com.moyz.adi.common.rag.*;
import com.moyz.adi.common.util.AesUtil;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 系统启动初始化服务。
 */
@Slf4j
@Service
public class Initializer {

    /**
     * 图片存储路径。
     */
    @Value("${local.images}")
    private String imagePath;
    /**
     * 临时图片路径。
     */
    @Value("${local.tmp-images}")
    private String tmpImagePath;
    /**
     * 缩略图路径。
     */
    @Value("${local.thumbnails}")
    private String thumbnailsPath;
    /**
     * 文件存储路径。
     */
    @Value("${local.files}")
    private String filePath;
    /**
     * 对话记忆存储路径。
     */
    @Value("${local.chat-memory}")
    private String chatMemoryPath;
    /**
     * 应用配置属性。
     */
    @Resource
    private AdiProperties adiProperties;
    /**
     * 模型服务。
     */
    @Resource
    private AiModelService aiModelService;
    /**
     * 系统配置服务。
     */
    @Resource
    private SysConfigService sysConfigService;
    /**
     * 阿里云 OSS 文件助手。
     */
    @Resource
    private AliyunOssFileHelper aliyunOssFileHelper;

    /**
     * 知识库图存储。
     */
    @Lazy
    @Resource
    private GraphStore kbGraphStore;
    /**
     * 知识库向量存储。
     */
    @Lazy
    @Resource
    private EmbeddingStore<TextSegment> kbEmbeddingStore;
    /**
     * 对话记忆向量存储。
     */
    @Lazy
    @Resource
    private EmbeddingStore<TextSegment> convMemoryEmbeddingStore;
    /**
     * AI 搜索向量存储。
     */
    @Lazy
    @Resource
    private EmbeddingStore<TextSegment> aiSearchEmbeddingStore;
    /**
     * 向量模型。
     */
    @Lazy
    @Resource
    private EmbeddingModel embeddingModel;

    /**
     * 应用初始化。
     */
    @PostConstruct
    public void init() {
        if (adiProperties.getEncrypt().getAesKey().equals("Ap9da0CopbjiKGc1")) {
            throw new RuntimeException("不能使用默认的AES key，请设置属于你自己的Key，AES相关的加解密都会用到该key，设置路径: application.yml => adi.encrypt.aes-key");
        }
        sysConfigService.loadAndCache();
        aiModelService.init();
        checkAndInitFileOperator();

        AesUtil.AES_KEY = adiProperties.getEncrypt().getAesKey();

        // 使用召回内容来源(RetrieveContentFrom)做为RAG名称以区分不同RAG实例
        EmbeddingRagContext.add(new EmbeddingRag(AdiConstant.RetrieveContentFrom.KNOWLEDGE_BASE, embeddingModel, kbEmbeddingStore));
        EmbeddingRagContext.add(new EmbeddingRag(AdiConstant.RetrieveContentFrom.CONV_MEMORY, embeddingModel, convMemoryEmbeddingStore));
        EmbeddingRagContext.add(new EmbeddingRag(AdiConstant.RetrieveContentFrom.WEB, embeddingModel, aiSearchEmbeddingStore));

        GraphRagContext.add(new GraphRag(AdiConstant.RetrieveContentFrom.KNOWLEDGE_BASE, kbGraphStore));
    }

    /**
     * 每 10 分钟刷新一次配置信息。
     */
    @Scheduled(initialDelay = 10 * 60 * 1000, fixedDelay = 10 * 60 * 1000)
    public void reloadConfig() {
        sysConfigService.loadAndCache();
    }

    /**
     * 初始化文件存储与目录结构。
     */
    public void checkAndInitFileOperator() {
        log.info("Initializing file operator...");
        LocalFileOperator.checkAndCreateDir(imagePath);
        LocalFileOperator.checkAndCreateDir(tmpImagePath);
        LocalFileOperator.checkAndCreateDir(thumbnailsPath);
        LocalFileOperator.checkAndCreateDir(filePath);
        LocalFileOperator.checkAndCreateDir(chatMemoryPath);
        LocalFileOperator.init(imagePath, filePath);
        AliyunOssFileOperator.init(aliyunOssFileHelper);
    }
}
