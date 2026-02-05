package com.moyz.adi.common.memory.longterm;

import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.cosntant.AdiConstant;
import com.moyz.adi.common.helper.LLMContext;
import com.moyz.adi.common.memory.vo.ActionMemories;
import com.moyz.adi.common.memory.vo.ExtractedFact;
import com.moyz.adi.common.rag.EmbeddingRagContext;
import com.moyz.adi.common.languagemodel.AbstractLLMService;
import com.moyz.adi.common.util.AdiStringUtil;
import com.moyz.adi.common.util.JsonUtil;
import com.moyz.adi.common.util.UuidUtil;
import com.moyz.adi.common.vo.ChatModelRequestParams;
import com.moyz.adi.common.vo.SseAskParams;
import dev.langchain4j.data.document.DefaultDocument;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.moyz.adi.common.cosntant.AdiConstant.MetadataKey.CONVERSATION_ID;
import static com.moyz.adi.common.cosntant.AdiConstant.RESPONSE_FORMAT_TYPE_JSON_OBJECT;

/**
 * 长期记忆<br/>
 * 目前只支持角色的长期记忆
 */
@Slf4j
@Service
public class LongTermMemoryService {

    /**
     * 会话长期记忆向量存储。
     */
    @Resource
    private EmbeddingStore<TextSegment> convMemoryEmbeddingStore;
    /**
     * 向量模型，用于文本向量化。
     */
    @Resource
    private EmbeddingModel embeddingModel;

    /**
     * 异步抽取事实并写入长期记忆。
     *
     * @param convId           会话 ID
     * @param modelPlatform    模型平台
     * @param modelName        模型名称
     * @param userMessage      用户消息
     * @param assistantMessage 助手消息
     * @return 无
     * @throws @BaseException 模型不可用或配置异常时抛出异常
     */
    @Async
    public void asyncAdd(Long convId, String modelPlatform, String modelName, String userMessage, String assistantMessage) {
        log.info("将信息转为记忆，convId: {}", convId);
        // 将对话拼装为结构化输入，便于事实抽取
        String inputMessage = toInputMessage(userMessage, assistantMessage);
        log.info("inputMessage: {}", inputMessage);
        AbstractLLMService llmService = LLMContext.getServiceOrDefault(modelPlatform, modelName);
        SseAskParams sseAskParams = new SseAskParams();
        sseAskParams.setUuid(UuidUtil.createShort());
        sseAskParams.setHttpRequestParams(
                ChatModelRequestParams.builder()
                        .systemMessage(LongTermMemoryPrompt.FACT_RETRIEVAL_PROMPT)
                        .userMessage(inputMessage)
                        .responseFormat(RESPONSE_FORMAT_TYPE_JSON_OBJECT)
                        .build()
        );
        sseAskParams.setModelName(modelName);
        sseAskParams.setUser(ThreadContext.getCurrentUser());
        log.info("request:{}", sseAskParams);
        ChatResponse response = llmService.chat(sseAskParams);
        log.info("Fact extraction response: {}", response.aiMessage().text());
        // 去除代码块包装，避免 JSON 解析失败
        String factResponse = AdiStringUtil.removeCodeBlock(response.aiMessage().text());
        if (StringUtils.isBlank(factResponse)) {
            log.warn("无法针对本次内容整理出事实性信息");
            return;
        }
        // 虽然指定了返回结构，但模型仍可能输出非标准内容，需要兼容数组形式
        List<String> facts;
        if (factResponse.trim().startsWith("[")) {
            facts = JsonUtil.toList(factResponse, String.class);
        } else {
            ExtractedFact extractedFact = JsonUtil.fromJson(factResponse, ExtractedFact.class);
            if (null == extractedFact || CollectionUtils.isEmpty(extractedFact.getFacts())) {
                log.warn("内容无法解析为ExtractedFact对象，原始内容：{}", factResponse);
                return;
            }
            facts = new ArrayList<>(extractedFact.getFacts());
        }
        if (null == facts) {
            log.warn("内容无法解析为事实性信息，原始内容：{}", factResponse);
            return;
        }

        // 先检索旧记忆，便于判断新增/更新/删除
        for (String fact : facts) {
            if (StringUtils.isBlank(fact)) {
                continue;
            }
            Embedding embedding = embeddingModel.embed(fact).content();
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(embedding)
                    .maxResults(5)
                    .minScore(0.7)
                    .filter(new IsEqualTo(CONVERSATION_ID, convId))
                    .build();
            Map<String, EmbeddingMatch<TextSegment>> oldMemoryEmbeddingToContent = new HashMap<>();
            EmbeddingSearchResult<TextSegment> searchResult = convMemoryEmbeddingStore.search(searchRequest);
            searchResult.matches().forEach(item -> oldMemoryEmbeddingToContent.put(item.embeddingId(), item));

            // 将 UUID 映射为整数，降低模型对 UUID 幻觉导致的解析失败
            Map<Integer, String> tmpIdToEmbeddingId = new HashMap<>();
            List<Map<String, String>> retrievedOldMemory = new ArrayList<>();
            int i = 0;
            for (Map.Entry<String, EmbeddingMatch<TextSegment>> entry : oldMemoryEmbeddingToContent.entrySet()) {
                retrievedOldMemory.add(Map.of("id", String.valueOf(i), "text", entry.getValue().embedded().text()));
                tmpIdToEmbeddingId.put(i, entry.getKey());
                i++;
            }

            // 交给模型判断是否需要新增/更新/删除，避免规则硬编码
            String analyzePrompt = getUpdateMemoryMessages(retrievedOldMemory, facts);
            String resp = llmService.chat(SseAskParams.builder()
                    .uuid(UuidUtil.createShort())
                    .httpRequestParams(
                            ChatModelRequestParams.builder()
                                    .userMessage(analyzePrompt)
                                    .responseFormat(RESPONSE_FORMAT_TYPE_JSON_OBJECT)
                                    .build()
                    )
                    .modelName(modelName)
                    .user(ThreadContext.getCurrentUser())
                    .build()
            ).aiMessage().text();
            log.info("Memory analysis response: {}", resp);
            String analyzedMsg = AdiStringUtil.removeCodeBlock(resp);
            ActionMemories actionMemories = JsonUtil.fromJson(analyzedMsg, ActionMemories.class);
            if (null == actionMemories || null == actionMemories.getMemory() || actionMemories.getMemory().isEmpty()) {
                // 无可执行动作时直接退出，避免误写入
                return;
            }
            for (ActionMemories.ActionMemory actionMemory : actionMemories.getMemory()) {
                if ("NONE".equalsIgnoreCase(actionMemory.getEvent())) {
                    log.info(" No changes required for memory id: {}", actionMemory.getId());
                } else if (AdiConstant.MemoryEvent.DELETE.equalsIgnoreCase(actionMemory.getEvent())) {
                    // 删除旧记忆，确保过期事实不再被召回
                    String embeddingId = tmpIdToEmbeddingId.get(Integer.parseInt(actionMemory.getId()));
                    convMemoryEmbeddingStore.remove(embeddingId);
                } else if (AdiConstant.MemoryEvent.UPDATE.equalsIgnoreCase(actionMemory.getEvent())) {

                    // 先删除再重建，避免重复向量导致召回噪声
                    String oldEmbeddingId = tmpIdToEmbeddingId.get(Integer.parseInt(actionMemory.getId()));
                    convMemoryEmbeddingStore.remove(oldEmbeddingId);

                    EmbeddingMatch<TextSegment> match = oldMemoryEmbeddingToContent.get(oldEmbeddingId);
                    Metadata metadata = new Metadata(Map.of(CONVERSATION_ID, convId));
                    TextSegment newSegment = TextSegment.from(actionMemory.getText(), metadata);
                    // 复用旧向量 ID 写入新文本，保证引用关系稳定
                    convMemoryEmbeddingStore.addAll(List.of(oldEmbeddingId), List.of(match.embedding()), List.of(newSegment));

                } else if (AdiConstant.MemoryEvent.ADD.equalsIgnoreCase(actionMemory.getEvent())) {
                    // 走统一 ingest 流程，复用分段与索引策略
                    Metadata metadata = new Metadata(Map.of(CONVERSATION_ID, convId));
                    Document document = new DefaultDocument(actionMemory.getText(), metadata);
                    EmbeddingRagContext.get(AdiConstant.RetrieveContentFrom.CONV_MEMORY).ingest(document, 20, null, null);
                }
            }
        }
    }

    /**
     * 长期记忆检索入口（预留）。
     *
     * @param text 查询文本
     * @return 无
     */
    public void search(String text) {

    }

    /**
     * 将对话拼装为统一输入格式。
     *
     * @param userMessage      用户消息
     * @param assistantMessage 助手消息
     * @return 拼装后的输入文本
     */
    private String toInputMessage(String userMessage, String assistantMessage) {
        return """
                Input:
                user: %s
                assistant: %s
                """.formatted(userMessage, assistantMessage);
    }

    /**
     * 构造用于更新记忆的提示词。
     *
     * @param retrievedOldMemory 已检索到的旧记忆
     * @param newFacts           新抽取的事实
     * @return 更新记忆的提示词
     */
    private String getUpdateMemoryMessages(List<Map<String, String>> retrievedOldMemory, List<String> newFacts) {
        String currentMemoryPart;
        if (retrievedOldMemory.isEmpty()) {
            currentMemoryPart = """
                    Current memory is empty.

                    """;
        } else {
            currentMemoryPart = """
                    Below is the current content of my memory which I have collected till now. You have to update it in the following format only:

                    ```
                    %s
                    ```

                    """.formatted(JsonUtil.toJson(retrievedOldMemory));
        }

        return """
                %s

                %s

                The new retrieved facts are mentioned in the triple backticks. You have to analyze the new retrieved facts and determine whether these facts should be added, updated, or deleted in the memory.

                ```
                %s
                ```

                You must return your response in the following JSON structure only:

                {
                    "memory" : [
                        {
                            "id" : "<ID of the memory>",                # Use existing ID for updates/deletes, or new ID for additions
                            "text" : "<Content of the memory>",         # Content of the memory
                            "event" : "<Operation to be performed>",    # Must be "ADD", "UPDATE", "DELETE", or "NONE"
                            "old_memory" : "<Old memory content>"       # Required only if the event is "UPDATE"
                        },
                        ...
                    ]
                }

                Follow the instruction mentioned below:
                - Do not return anything from the custom few shot prompts provided above.
                - If the current memory is empty, then you have to add the new retrieved facts to the memory.
                - You should return the updated memory in only JSON format as shown below. The memory key should be the same if no changes are made.
                - If there is an addition, generate a new key and add the new memory corresponding to it.
                - If there is a deletion, the memory key-value pair should be removed from the memory.
                - If there is an update, the ID key should remain the same and only the value needs to be updated.

                Do not return anything except the JSON format.
                """.formatted(LongTermMemoryPrompt.UPDATE_MEMORY_PROMPT, currentMemoryPart, JsonUtil.toJson(newFacts));
    }


}
