package com.moyz.adi.common.service;

import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.cosntant.AdiConstant;
import com.moyz.adi.common.dto.AiSearchReq;
import com.moyz.adi.common.dto.SearchEngineResp;
import com.moyz.adi.common.dto.SearchReturn;
import com.moyz.adi.common.dto.SearchReturnWebPage;
import com.moyz.adi.common.entity.AiModel;
import com.moyz.adi.common.entity.AiSearchRecord;
import com.moyz.adi.common.entity.User;
import com.moyz.adi.common.helper.LLMContext;
import com.moyz.adi.common.helper.SSEEmitterHelper;
import com.moyz.adi.common.rag.CompositeRag;
import com.moyz.adi.common.rag.EmbeddingRag;
import com.moyz.adi.common.rag.EmbeddingRagContext;
import com.moyz.adi.common.searchengine.SearchEngineServiceContext;
import com.moyz.adi.common.util.PromptUtil;
import com.moyz.adi.common.util.UuidUtil;
import com.moyz.adi.common.vo.ChatModelRequestParams;
import com.moyz.adi.common.vo.RetrieverCreateParam;
import com.moyz.adi.common.vo.SseAskParams;
import dev.langchain4j.data.document.DefaultDocument;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.moyz.adi.common.cosntant.AdiConstant.RetrieveContentFrom.WEB;
import static com.moyz.adi.common.cosntant.AdiConstant.SSE_TIMEOUT;
import static com.moyz.adi.common.enums.ErrorEnum.B_NO_ANSWER;

/**
 * RAG 搜索服务。
 */
@Slf4j
@Service
public class SearchService {

    /**
     * 自身代理对象（用于异步调用）。
     */
    @Lazy
    @Resource
    private SearchService self;

    /**
     * SSE 发送辅助。
     */
    @Resource
    private SSEEmitterHelper sseEmitterHelper;

    /**
     * 搜索记录服务。
     */
    @Resource
    private AiSearchRecordService aiSearchRecordService;

    /**
     * 用户日消耗统计服务。
     */
    @Resource
    private UserDayCostService userDayCostService;

    /**
     * 模型服务。
     */
    @Resource
    private AiModelService aiModelService;

    /**
     * 异步执行器。
     */
    @Resource
    private AsyncTaskExecutor mainExecutor;

    /**
     * 发起搜索请求（SSE）。
     *
     * @param req 搜索请求
     * @return SSE 连接
     */
    public SseEmitter search(AiSearchReq req) {
        User user = ThreadContext.getCurrentUser();
        SseEmitter sseEmitter = new SseEmitter(SSE_TIMEOUT);
        if (!sseEmitterHelper.checkOrComplete(user, sseEmitter)) {
            return sseEmitter;
        }
        sseEmitterHelper.startSse(user, sseEmitter);
        self.asyncSearch(user, sseEmitter, req);
        return sseEmitter;
    }

    /**
     * 异步执行搜索流程。
     *
     * @param user       用户
     * @param sseEmitter SSE 连接
     * @param req        搜索请求
     */
    @Async
    public void asyncSearch(User user, SseEmitter sseEmitter, AiSearchReq req) {
        SearchReturn searchResult = SearchEngineServiceContext.getService(req.getEngineName()).search(req.getSearchText(), "", "", 5);
        if (StringUtils.isNotBlank(searchResult.getErrorMessage())) {
            sseEmitterHelper.sendStartAndComplete(user.getId(), sseEmitter, searchResult.getErrorMessage());
            return;
        }
        if (CollectionUtils.isEmpty(searchResult.getItems())) {
            sseEmitterHelper.sendStartAndComplete(user.getId(), sseEmitter, B_NO_ANSWER.getInfo());
            return;
        }
        boolean sendFail = false;
        try {
            sseEmitter.send(SseEmitter.event().name(AdiConstant.SSEEventName.AI_SEARCH_SOURCE_LINKS).data(searchResult.getItems()));
        } catch (IOException e) {
            sendFail = true;
            log.error("asyncSearch error", e);
            sseEmitterHelper.sendErrorAndComplete(user.getId(), sseEmitter, e.getMessage());
        }
        if (sendFail) {
            return;
        }
        if (req.isBriefSearch()) {
            briefSearch(user, req.getSearchText(), req.getModelName(), searchResult.getItems(), sseEmitter);
        } else {
            detailSearch(user, req.getSearchText(), req.getEngineName(), req.getModelPlatform(), req.getModelName(), searchResult.getItems(), sseEmitter);
        }
    }

    /**
     * 1. 通过搜索引擎检索
     * 2. 基于搜索结果构建提示词
     * 3. 发送提示词给大模型
     *
     * @param user        用户
     * @param searchText  搜索文本
     * @param modelName   模型名称
     * @param resultItems 搜索结果
     * @param sseEmitter  SSE 连接
     */
    public void briefSearch(User user, String searchText, String modelName, List<SearchReturnWebPage> resultItems, SseEmitter sseEmitter) {
        log.info("briefSearch,searchText:{}", searchText);
        StringBuilder builder = new StringBuilder();
        for (SearchReturnWebPage item : resultItems) {
            builder.append(item.getSnippet()).append("\n\n");
        }
        String ragQuestion = builder.toString();
        String prompt = PromptUtil.createPrompt(searchText, "", ragQuestion, "");

        SearchEngineResp resp = new SearchEngineResp().setItems(resultItems);

        SseAskParams sseAskParams = new SseAskParams();
        sseAskParams.setUuid(UuidUtil.createShort());
        sseAskParams.setHttpRequestParams(ChatModelRequestParams.builder().systemMessage(StringUtils.EMPTY).userMessage(prompt).build());
        sseAskParams.setSseEmitter(sseEmitter);
        sseAskParams.setModelName(modelName);
        sseAskParams.setUser(user);
        sseEmitterHelper.call(sseAskParams, (response, promptMeta, answerMeta) -> {
            sseEmitterHelper.sendComplete(user.getId(), sseEmitter);

            AiModel aiModel = aiModelService.getByName(modelName);

            AiSearchRecord newRecord = new AiSearchRecord();
            newRecord.setUuid(sseAskParams.getUuid());
            newRecord.setQuestion(searchText);
            newRecord.setSearchEngineResp(resp);
            newRecord.setPrompt(prompt);
            newRecord.setPromptTokens(promptMeta.getTokens());
            newRecord.setAnswer(response.getContent());
            newRecord.setAnswerTokens(answerMeta.getTokens());
            newRecord.setUserUuid(user.getUuid());
            newRecord.setUserId(user.getId());
            newRecord.setAiModelId(null != aiModel ? aiModel.getId() : 0L);
            aiSearchRecordService.save(newRecord);

            if (null != aiModel) {
                userDayCostService.appendCostToUser(user, promptMeta.getTokens() + answerMeta.getTokens(), aiModel.getIsFree());
            }
        });
    }

    /**
     * 1. 通过搜索引擎检索
     * 2. 保存响应到向量库
     * 3. 召回文档并构建提示词
     * 4. 发送提示词给大模型
     *
     * @param user        用户
     * @param searchText  搜索文本
     * @param engineName  搜索引擎名称
     * @param modelPlatform 模型平台
     * @param modelName   模型名称
     * @param resultItems 搜索结果
     * @param sseEmitter  SSE 连接
     */
    public void detailSearch(User user, String searchText, String engineName, String modelPlatform, String modelName, List<SearchReturnWebPage> resultItems, SseEmitter sseEmitter) {
        log.info("detailSearch,searchText:{}", searchText);
        AiModel aiModel = LLMContext.getAiModel(modelPlatform, modelName);
        // 保存搜索记录到数据库
        SearchEngineResp resp = new SearchEngineResp().setItems(resultItems);
        AiSearchRecord newRecord = new AiSearchRecord();
        String searchUuid = UuidUtil.createShort();
        newRecord.setUuid(searchUuid);
        newRecord.setQuestion(searchText);
        newRecord.setSearchEngineResp(resp);
        newRecord.setUserId(user.getId());
        newRecord.setUserUuid(user.getUuid());
        newRecord.setAiModelId(aiModel.getId());
        aiSearchRecordService.save(newRecord);

        CountDownLatch countDownLatch = new CountDownLatch(resultItems.size());
        for (int i = 0; i < resultItems.size(); i++) {
            int finalI = i;
            mainExecutor.execute(() -> {
                try {
                    SearchReturnWebPage item = resultItems.get(finalI);
                    String content;
                    if (finalI < 2) {
                        content = getContentFromRemote(item);

                        // 用网页正文内容填充
                        item.setContent(content);
                    } else {
                        content = item.getSnippet();
                    }

                    // 向量化入库
                    if (StringUtils.isNotBlank(content)) {
                        Metadata metadata = new Metadata();
                        metadata.put(AdiConstant.MetadataKey.ENGINE_NAME, engineName);
                        metadata.put(AdiConstant.MetadataKey.SEARCH_UUID, searchUuid);
                        Document document = new DefaultDocument(content, metadata);
                        EmbeddingRagContext.get(WEB).ingest(document, 0, "", null);
                    }
                } catch (Exception e) {
                    log.error("Detail search error,uuid:{}", searchUuid, e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("CountDownLatch await error,uuid:{}", searchUuid, e);
            Thread.currentThread().interrupt();
        }

        log.info("Create prompt");
        int maxInputTokens = aiModel.getMaxInputTokens();
        int maxResults = EmbeddingRag.getRetrieveMaxResults(searchText, maxInputTokens);
        RetrieverCreateParam createParam = RetrieverCreateParam.builder()
                .filter(new IsEqualTo(AdiConstant.MetadataKey.SEARCH_UUID, searchUuid))
                .maxResults(maxResults)
                .minScore(0)
                .breakIfSearchMissed(false)
                .build();
        ContentRetriever contentRetriever = EmbeddingRagContext.get(WEB).createRetriever(createParam);
        SseAskParams sseAskParams = new SseAskParams();
        sseAskParams.setUuid(searchUuid);
        sseAskParams.setUser(user);
        sseAskParams.setHttpRequestParams(
                ChatModelRequestParams.builder()
                        .memoryId(user.getUuid() + "-search")
                        .systemMessage(StringUtils.EMPTY)
                        .userMessage(searchText)
                        .build()
        );
        sseAskParams.setSseEmitter(sseEmitter);
        sseAskParams.setModelName(modelName);
        new CompositeRag(WEB).ragChat(List.of(contentRetriever), sseAskParams, (response, promptMeta, answerMeta) -> {

            sseEmitterHelper.sendComplete(user.getId(), sseAskParams.getSseEmitter());

            AiSearchRecord existRecord = aiSearchRecordService.lambdaQuery().eq(AiSearchRecord::getUuid, searchUuid).one();

            AiSearchRecord updateRecord = new AiSearchRecord();
            updateRecord.setId(existRecord.getId());
            // 更新搜索结果内容（包含正文）
            updateRecord.setSearchEngineResp(new SearchEngineResp().setItems(resultItems));
            // 待办：增强后的 prompt
            updateRecord.setPrompt("");
            updateRecord.setPromptTokens(promptMeta.getTokens());
            updateRecord.setAnswer(response);
            updateRecord.setAnswerTokens(answerMeta.getTokens());
            aiSearchRecordService.updateById(updateRecord);

            userDayCostService.appendCostToUser(user, promptMeta.getTokens() + answerMeta.getTokens(), aiModel.getIsFree());
        });
    }

    /**
     * 从远程网页获取正文内容。
     *
     * @param item 搜索结果条目
     * @return 正文内容
     */
    private String getContentFromRemote(SearchReturnWebPage item) {
        String result = "";
        try {
            String url = item.getLink();
            if (StringUtils.isBlank(url) || !url.startsWith("http")) {
                return result;
            }
            org.jsoup.nodes.Document doc = Jsoup.connect(url).ignoreContentType(true).get();
            if (!doc.getElementsByTag("main").isEmpty()) {
                result = doc.getElementsByTag("main").get(0).html();
            } else {
                result = doc.body().html();
            }
            if (StringUtils.isBlank(result)) {
                log.error("Empty content from {}, use snippet instead", item.getLink());
                return item.getSnippet();
            }
        } catch (Exception e) {
            log.error("Failed to load document from {}, use snippet instead", item.getLink(), e);
        }
        Cleaner cleaner = new Cleaner(Safelist.none());
        return cleaner.clean(Jsoup.parse(result)).text();
    }
}
