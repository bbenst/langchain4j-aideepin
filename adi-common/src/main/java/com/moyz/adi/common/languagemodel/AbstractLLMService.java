package com.moyz.adi.common.languagemodel;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.moyz.adi.common.cosntant.AdiConstant;
import com.moyz.adi.common.entity.AiModel;
import com.moyz.adi.common.entity.ModelPlatform;
import com.moyz.adi.common.enums.ErrorEnum;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.helper.SSEEmitterHelper;
import com.moyz.adi.common.helper.TtsModelContext;
import com.moyz.adi.common.interfaces.TriConsumer;
import com.moyz.adi.common.languagemodel.data.InnerStreamChatParams;
import com.moyz.adi.common.languagemodel.data.LLMException;
import com.moyz.adi.common.languagemodel.data.LLMResponseContent;
import com.moyz.adi.common.memory.shortterm.MapDBChatMemoryStore;
import com.moyz.adi.common.rag.TokenEstimatorFactory;
import com.moyz.adi.common.rag.TokenEstimatorThreadLocal;
import com.moyz.adi.common.util.*;
import com.moyz.adi.common.vo.*;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.*;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.request.DefaultChatRequestParameters;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolService;
import dev.langchain4j.service.tool.ToolServiceContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.moyz.adi.common.cosntant.AdiConstant.CustomChatRequestParameterKeys.ENABLE_WEB_SEARCH;
import static com.moyz.adi.common.cosntant.AdiConstant.CustomChatRequestParameterKeys.ENABLE_THINKING;
import static com.moyz.adi.common.cosntant.AdiConstant.LLM_MAX_INPUT_TOKENS_DEFAULT;
import static com.moyz.adi.common.cosntant.AdiConstant.RESPONSE_FORMAT_TYPE_JSON_OBJECT;
import static com.moyz.adi.common.enums.ErrorEnum.A_PARAMS_ERROR;
import static com.moyz.adi.common.enums.ErrorEnum.B_LLM_SERVICE_DISABLED;

/**
 * 抽象大模型服务基类，封装流式/非流式对话与工具调用流程。
 */
@Slf4j
public abstract class AbstractLLMService extends CommonModelService {

    /**
     * Redis 模板，用于 token 统计与临时缓存。
     */
    protected StringRedisTemplate stringRedisTemplate;

    /**
     * 用户 UUID 与 TTS 任务信息的缓存映射。
     */
    private final Cache<String, TtsJobInfo> ttsJobCache;

    /**
     * 语音合成设置。
     */
    @Getter
    private final TtsSetting ttsSetting;

    /**
     * 构建大模型服务基类。
     *
     * @param aiModel        模型配置
     * @param modelPlatform  模型平台
     */
    protected AbstractLLMService(AiModel aiModel, ModelPlatform modelPlatform) {
        super(aiModel, modelPlatform);

        initMaxInputTokens();
        ttsSetting = JsonUtil.fromJson(LocalCache.CONFIGS.get(AdiConstant.SysConfigKey.TTS_SETTING), TtsSetting.class);
        if (null == ttsSetting) {
            log.error("TTS配置未找到，请检查配置文件，请检查 adi_sys_config 中是否有 tts_setting 配置项");
            throw new BaseException(ErrorEnum.B_TTS_SETTING_NOT_FOUND);
        }

        ttsJobCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();
    }

    /**
     * 初始化模型最大输入 token 数。
     *
     * @return 无
     */
    private void initMaxInputTokens() {
        if (this.aiModel.getMaxInputTokens() < 1) {
            this.aiModel.setMaxInputTokens(LLM_MAX_INPUT_TOKENS_DEFAULT);
        }
    }

    /**
     * 获取 Redis 模板，懒加载。
     *
     * @return Redis 模板
     */
    public StringRedisTemplate getStringRedisTemplate() {
        if (null == this.stringRedisTemplate) {
            this.stringRedisTemplate = SpringUtil.getBean(StringRedisTemplate.class);
        }
        return this.stringRedisTemplate;
    }

    /**
     * 设置代理地址。
     *
     * @param proxyAddress 代理地址
     * @return 当前服务实例
     */
    public AbstractLLMService setProxyAddress(InetSocketAddress proxyAddress) {
        this.proxyAddress = proxyAddress;
        return this;
    }

    /**
     * 检测该服务是否可用（不可用的情况通常是没有配置 key）。
     *
     * @return 是否可用
     */
    public abstract boolean isEnabled();

    /**
     * 聊天前置校验，子类可覆写。
     *
     * @param params 请求参数
     * @return true 表示通过校验
     */
    protected boolean checkBeforeChat(SseAskParams params) {
        return true;
    }

    /**
     * 构建非流式 ChatModel。
     *
     * @param properties 构建参数
     * @return ChatModel 实例
     */
    public ChatModel buildChatLLM(ChatModelBuilderProperties properties) {
        ChatModelBuilderProperties tmpProperties = properties;
        if (null == properties) {
            // 未传参数时使用默认温度，保证输出稳定性
            tmpProperties = new ChatModelBuilderProperties();
            tmpProperties.setTemperature(0.7);
            log.info("llmBuilderProperties is null, set default temperature:{}", tmpProperties.getTemperature());
        }
        if (null == tmpProperties.getTemperature() || tmpProperties.getTemperature() <= 0 || tmpProperties.getTemperature() > 1) {
            // 超出合法区间时回退默认值，避免模型拒绝请求
            tmpProperties.setTemperature(0.7);
            log.info("llmBuilderProperties temperature is invalid, set default temperature:{}", tmpProperties.getTemperature());
        }
        return doBuildChatModel(tmpProperties);
    }

    /**
     * 构建具体 ChatModel，由子类实现。
     *
     * @param properties 构建参数
     * @return ChatModel 实例
     */
    protected abstract ChatModel doBuildChatModel(ChatModelBuilderProperties properties);

    /**
     * 构建流式 ChatModel，由子类实现。
     *
     * @param properties 构建参数
     * @return StreamingChatModel 实例
     */
    public abstract StreamingChatModel buildStreamingChatModel(ChatModelBuilderProperties properties);

    /**
     * 解析底层异常并转换为业务异常。
     *
     * @param error 原始异常
     * @return 解析后的异常
     */
    protected abstract LLMException parseError(Object error);

    /**
     * 获取当前模型的 token 估算器。
     *
     * @return token 估算器
     */
    public abstract TokenCountEstimator getTokenEstimator();

    /**
     * 普通聊天，将原始的用户问题及历史消息发送给AI
     *
     * @param params   请求参数
     * @param consumer 响应结果回调
     * @return 无
     * @throws BaseException 服务不可用或参数校验失败时抛出异常
     */
    public void streamingChat(SseAskParams params, TriConsumer<LLMResponseContent, PromptMeta, AnswerMeta> consumer) {
        if (!isEnabled()) {
            log.error("llm service is disabled");
            throw new BaseException(B_LLM_SERVICE_DISABLED);
        }
        if (!checkBeforeChat(params)) {
            log.error("对话参数校验不通过");
            throw new BaseException(A_PARAMS_ERROR);
        }
        ChatModelRequestParams httpRequestParams = params.getHttpRequestParams();
        ChatModelBuilderProperties modelProperties = params.getModelProperties();
        log.info("sseChat,messageId:{}", httpRequestParams.getMemoryId());
        // 统一用流式模型实例承接后续分片、思考过程与工具调用递归
        StreamingChatModel streamingChatModel = buildStreamingChatModel(modelProperties);

        ChatRequest chatRequest = createChatRequest(httpRequestParams);
        InnerStreamChatParams innerStreamChatParams = InnerStreamChatParams.builder()
                .uuid(params.getUuid())
                .user(params.getUser())
                .streamingChatModel(streamingChatModel)
                .chatRequest(chatRequest)
                .sseEmitter(params.getSseEmitter())
                .mcpClients(httpRequestParams.getMcpClients())
                .answerContentType(params.getAnswerContentType())
                .consumer(consumer)
                .build();
        try {

            // 如果系统设置后端语音合成且返回内容为音频，则初始化 TTS 任务并注册回调
            if (TtsUtil.needTts(ttsSetting, params.getAnswerContentType())) {
                String ttsJobId = UuidUtil.createShort();
                TtsJobInfo jobInfo = new TtsJobInfo();
                TtsModelContext ttsModelContext = new TtsModelContext();
                jobInfo.setJobId(ttsJobId);
                jobInfo.setTtsModelContext(ttsModelContext);
                ttsJobCache.put(params.getUser().getUuid(), jobInfo);
                ttsModelContext.startTtsJob(ttsJobId, params.getVoice(), (ByteBuffer audioFrame) -> {
                    byte[] frameBytes = new byte[audioFrame.remaining()];
                    audioFrame.get(frameBytes);
                    String base64Audio = Base64.getEncoder().encodeToString(frameBytes);
                    // 增量音频帧实时透传前端，降低“文本结束后才出音频”的感知延迟
                    SSEEmitterHelper.sendAudio(params.getSseEmitter(), base64Audio);
                }, jobInfo::setFilePath, (String errorMsg) -> log.error("tts error: {}", errorMsg));
            }

            // 无论是否返回音频，都需要走流式聊天主流程
            innerStreamingChat(innerStreamChatParams);
        } catch (Exception e) {
            ttsJobCache.invalidate(params.getUser().getUuid());
            closeMcpClients(params.getHttpRequestParams().getMcpClients());
            throw e;
        }

    }

    /**
     * 内部流式聊天方法，处理工具调用等复杂逻辑
     *
     * @param params 参数对象，包含流式聊天所需的所有信息
     * @return 无
     */
    private void innerStreamingChat(InnerStreamChatParams params) {
        // 预构建工具映射，避免每个分片重复解析工具定义
        Map<ToolSpecification, McpClient> toolSpecificationMcpClientMap = getRequestTools(params.getMcpClients());
        params.getStreamingChatModel().chat(params.getChatRequest(), new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                // 分片文本先推送到前端，再喂给 TTS，保证文本与音频时间线尽量一致
                SSEEmitterHelper.parseAndSendPartialMsg(params.getSseEmitter(), partialResponse);
                ttsOnPartialMessage(params, partialResponse);
            }

            @Override
            public void onPartialThinking(PartialThinking partialThinking) {
                SSEEmitterHelper.sendThinking(params.getSseEmitter(), partialThinking.text());
            }

            @Override
            public void onCompleteResponse(ChatResponse response) {
                AiMessage responseAiMessage = response.aiMessage();
                if (responseAiMessage.hasToolExecutionRequests()) {
                    // 如果有工具执行请求
                    List<ToolExecutionResultMessage> toolExecutionMessages = createToolExecutionMessages(responseAiMessage, toolSpecificationMcpClientMap);

                    // MCP 调用消息格式参考：https://docs.langchain4j.dev/tutorials/tools/
                    AiMessage aiMessage = AiMessage.aiMessage(responseAiMessage.toolExecutionRequests());
                    List<ChatMessage> messages = new ArrayList<>(params.getChatRequest().messages());
                    messages.add(aiMessage);
                    messages.addAll(toolExecutionMessages);
                    params.setChatRequest(ChatRequest.builder()
                            .messages(messages)
                            .parameters(params.getChatRequest().parameters())
                            .build());
                    // 工具调用结果回灌后递归继续对话，直到模型返回最终自然语言答案
                    // 使用工具调用结果递归继续对话
                    innerStreamingChat(params);
                } else {
                    TtsJobInfo jobInfo = ttsOnComplete(params);
                    String filePath = null != jobInfo ? jobInfo.getFilePath() : null;
                    // 结束整个对话任务
                    Pair<PromptMeta, AnswerMeta> pair = SSEEmitterHelper.calculateToken(response, params.getUuid());
                    // 仅在最终完成时触发 consumer，确保上层拿到的是可持久化的完整结果
                    params.getConsumer().accept(new LLMResponseContent(response.aiMessage().thinking(), response.aiMessage().text(), filePath), pair.getLeft(), pair.getRight());
                    // 终态分支统一关闭 MCP 客户端，避免长连接泄漏
                    closeMcpClients(params.getMcpClients());
                }
            }

            @Override
            public void onError(Throwable error) {
                // 错误分支同样要释放 MCP 资源，防止后续请求复用到脏连接
                SSEEmitterHelper.errorAndShutdown(error, params.getSseEmitter());
                closeMcpClients(params.getMcpClients());
            }
        });
    }

    /**
     * 非流式聊天接口。
     *
     * @param params 请求参数
     * @return 聊天响应
     * @throws BaseException 服务不可用或参数校验失败时抛出异常
     */
    public ChatResponse chat(SseAskParams params) {
        if (!isEnabled()) {
            log.error("llm service is disabled");
            throw new BaseException(B_LLM_SERVICE_DISABLED);
        }
        if (!checkBeforeChat(params)) {
            log.error("对话参数校验不通过");
            throw new BaseException(A_PARAMS_ERROR);
        }

        ChatModelRequestParams chatModelRequestParams = params.getHttpRequestParams();
        ChatModelBuilderProperties modelProperties = params.getModelProperties();
        ChatModel chatModel = buildChatLLM(modelProperties);
        ChatRequest chatRequest = createChatRequest(chatModelRequestParams);

        ChatResponse chatResponse = chatModel.chat(chatRequest);
        if (chatResponse.aiMessage().hasToolExecutionRequests()) {
            return innerChat(params.getUuid(), chatModel, chatModelRequestParams, chatRequest);
        }

        cacheTokenUsage(params.getUuid(), chatResponse);
        return chatResponse;
    }

    /**
     * 程序内部调用的聊天方法，通常用于处理工具调用等复杂逻辑
     *
     * @param uuid                   唯一标识
     * @param chatModel              聊天模型
     * @param chatModelRequestParams 聊天模型参数
     * @param chatRequest            聊天请求
     * @return ChatResponse 聊天响应
     */
    private ChatResponse innerChat(String uuid, ChatModel chatModel, ChatModelRequestParams chatModelRequestParams, ChatRequest chatRequest) {
        try {
            ChatResponse chatResponse = chatModel.chat(chatRequest);
            AiMessage responseAiMessage = chatResponse.aiMessage();
            if (responseAiMessage.hasToolExecutionRequests()) {
                Map<ToolSpecification, McpClient> toolSpecificationMcpClientMap = getRequestTools(chatModelRequestParams.getMcpClients());
                List<ToolExecutionResultMessage> toolExecutionMessages = createToolExecutionMessages(responseAiMessage, toolSpecificationMcpClientMap);

                AiMessage aiMessage = AiMessage.aiMessage(responseAiMessage.toolExecutionRequests());
                List<ChatMessage> messages = new ArrayList<>(chatRequest.messages());
                messages.add(aiMessage);
                messages.addAll(toolExecutionMessages);

                cacheTokenUsage(uuid, chatResponse);

                // 使用工具调用结果递归继续对话
                return innerChat(uuid, chatModel, chatModelRequestParams, ChatRequest.builder()
                        .messages(messages)
                        .parameters(chatRequest.parameters())
                        .build());
            }
            cacheTokenUsage(uuid, chatResponse);
            return chatResponse;
        } finally {
            closeMcpClients(chatModelRequestParams.getMcpClients());
        }
    }

    /**
     * 缓存 token 使用情况。
     *
     * @param uuid         唯一标识
     * @param chatResponse 聊天响应
     * @return 无
     */
    private void cacheTokenUsage(String uuid, ChatResponse chatResponse) {
        int inputTokenCount = chatResponse.metadata().tokenUsage().inputTokenCount();
        int outputTokenCount = chatResponse.metadata().tokenUsage().outputTokenCount();
        log.info("ChatModel token cost,uuid:{},inputTokenCount:{},outputTokenCount:{}", uuid, inputTokenCount, outputTokenCount);
        LLMTokenUtil.cacheTokenUsage(getStringRedisTemplate(), uuid, chatResponse.metadata().tokenUsage());
    }

    /**
     * 构建聊天消息列表（包含系统消息、用户消息与图片内容）。
     *
     * @param chatModelRequestParams 聊天请求参数
     * @return 消息列表
     */
    private List<ChatMessage> createChatMessages(ChatModelRequestParams chatModelRequestParams) {
        String memoryId = chatModelRequestParams.getMemoryId();
        List<Content> userContents = new ArrayList<>();
        userContents.add(TextContent.from(chatModelRequestParams.getUserMessage()));
        List<ChatMessage> chatMessages = new ArrayList<>();
        if (StringUtils.isNotBlank(memoryId)) {

            TokenCountEstimator tokenCountEstimator;
            String tokenEstimatorName = TokenEstimatorThreadLocal.getTokenEstimator();
            if (StringUtils.isBlank(tokenEstimatorName) && null != getTokenEstimator()) {
                // 优先使用模型自带估算器，保证与服务一致
                tokenCountEstimator = getTokenEstimator();
            } else {
                // 使用线程上下文指定的估算器，确保与知识库一致
                tokenCountEstimator = TokenEstimatorFactory.create(tokenEstimatorName);
            }

            // 滑动窗口算法限制消息长度
            TokenWindowChatMemory memory = TokenWindowChatMemory.builder()
                    .chatMemoryStore(MapDBChatMemoryStore.getSingleton())
                    .id(memoryId)
                    .maxTokens(aiModel.getMaxInputTokens(), tokenCountEstimator)
                    .build();
            if (StringUtils.isNotBlank(chatModelRequestParams.getSystemMessage())) {
                memory.add(SystemMessage.from(chatModelRequestParams.getSystemMessage()));
            }

            // 处理重复的 UserMessage，避免用户消息被重复计入
            if (!memory.messages().isEmpty()) {
                ChatMessage lastMessage = memory.messages().get(memory.messages().size() - 1);
                if (lastMessage instanceof UserMessage) {
                    List<ChatMessage> list = memory.messages().subList(0, memory.messages().size() - 1);
                    memory.clear();
                    list.forEach(memory::add);
                }
            }

            memory.add(UserMessage.from(userContents));

            // 得到截断后符合 maxTokens 的文本消息
            chatMessages.addAll(memory.messages());

            // AI Services 暂不支持多模态，使用低层 API 处理：https://docs.langchain4j.dev/tutorials/ai-services#multimodality
            // 重新组装用户消息并追加图片内容
            List<Content> imageContents = ImageUtil.urlsToImageContent(chatModelRequestParams.getImageUrls());
            if (CollectionUtils.isNotEmpty(imageContents)) {
                int lastIndex = chatMessages.size() - 1;
                UserMessage lastMessage = (UserMessage) chatMessages.get(lastIndex);
                chatMessages.remove(lastIndex);
                List<Content> userMessage = new ArrayList<>();
                userMessage.addAll(lastMessage.contents());
                userMessage.addAll(imageContents);
                chatMessages.add(UserMessage.from(userMessage));
            }
            return chatMessages;
        } else {
            if (StringUtils.isNotBlank(chatModelRequestParams.getSystemMessage())) {
                chatMessages.add(SystemMessage.from(chatModelRequestParams.getSystemMessage()));
            }
            List<Content> imageContents = ImageUtil.urlsToImageContent(chatModelRequestParams.getImageUrls());
            if (CollectionUtils.isNotEmpty(imageContents)) {
                userContents.addAll(imageContents);
            }
            chatMessages.add(UserMessage.from(userContents));
        }
        return chatMessages;
    }

    /**
     * 解析当前请求可用的工具列表。
     *
     * @param mcpClients MCP 客户端列表
     * @return 工具定义与客户端的映射
     */
    private Map<ToolSpecification, McpClient> getRequestTools(List<McpClient> mcpClients) {
        Map<ToolSpecification, McpClient> tools = new HashMap<>();
        // MCP 工具
        for (McpClient mcpClient : mcpClients) {
            for (ToolSpecification toolSpecification : mcpClient.listTools()) {
                tools.put(toolSpecification, mcpClient);
            }
        }
        // 原生工具（暂未启用）
//        chatRequest.tools().forEach(tool -> {
//            ToolSpecifications.toolSpecificationsFrom(tool)
//                    .forEach(spec -> tools.put(spec,
//                            (req, mem) -> new DefaultToolExecutor(tool, req).execute(req, mem)));
//        });
        return tools;
    }

    /**
     * 构建 ChatRequest 请求。
     *
     * @param httpRequestParams 请求参数
     * @return ChatRequest
     */
    private ChatRequest createChatRequest(ChatModelRequestParams httpRequestParams) {

        log.info("sseChat,messageId:{}", httpRequestParams.getMemoryId());
        List<ChatMessage> chatMessages = createChatMessages(httpRequestParams);

        // MCP 工具规格
        List<ToolSpecification> toolSpecifications = new ArrayList<>();
        List<McpClient> mcpClients = httpRequestParams.getMcpClients();
        if (!CollectionUtils.isEmpty(mcpClients)) {
            log.info("no mcp clients configured, skip tool spec creation");
            ToolProvider toolProvider = McpToolProvider.builder()
                    .mcpClients(mcpClients)
                    .build();
            ToolService toolService = new ToolService();
            toolService.toolProvider(toolProvider);
            ToolServiceContext toolServiceContext = toolService.createContext(UuidUtil.createShort(), ((UserMessage) chatMessages.get(chatMessages.size() - 1)));
            log.info("tool specs:{}", toolServiceContext.toolSpecifications());
            toolSpecifications = toolServiceContext.toolSpecifications();
        }

        DefaultChatRequestParameters.Builder<?> builder = ChatRequestParameters.builder();
        builder.toolSpecifications(toolSpecifications);

        // 响应格式
        String responseFormat = httpRequestParams.getResponseFormat();
        log.info("Response format:{}", responseFormat);
        if (StringUtils.isNotBlank(responseFormat)) {
            if (aiModel.getResponseFormatTypes().contains(responseFormat)) {
                builder.responseFormat(RESPONSE_FORMAT_TYPE_JSON_OBJECT.equals(responseFormat) ? ResponseFormat.JSON : ResponseFormat.TEXT);
            } else {
                log.warn("当前模型不支持返回json格式（常用的LLM基本都支持返回json格式，请检查对应的模型表 ai_model.response_format_types 是否包含了 json_object），模型名称：{}, 当前支持的格式：{}", aiModel.getName(), aiModel.getResponseFormatTypes());
            }
        }

        // 启用思考与联网检索等自定义参数
        Map<String, Object> customParameters = new HashMap<>();
        if (null != httpRequestParams.getReturnThinking()) {
            customParameters.put(ENABLE_THINKING, httpRequestParams.getReturnThinking());
        }
        if (null != httpRequestParams.getEnableWebSearch()) {
            customParameters.put(ENABLE_WEB_SEARCH, httpRequestParams.getEnableWebSearch());
        }
        ChatRequestParameters parameters = doCreateChatRequestParameters(builder.build(), customParameters);

        return ChatRequest.builder()
                .messages(chatMessages)
                .parameters(parameters)
                .build();
    }

    /**
     * 由子类扩展请求参数。
     *
     * @param defaultParameters 默认参数
     * @param customParameters  自定义参数
     * @return 处理后的请求参数
     */
    protected ChatRequestParameters doCreateChatRequestParameters(ChatRequestParameters defaultParameters, Map<String, Object> customParameters) {
        return defaultParameters;
    }

    /**
     * 将工具执行请求转换为结果消息。
     *
     * @param aiMessage                     AI 返回消息
     * @param toolSpecificationMcpClientMap 工具与客户端映射
     * @return 工具执行结果消息列表
     */
    private List<ToolExecutionResultMessage> createToolExecutionMessages(AiMessage aiMessage, Map<ToolSpecification, McpClient> toolSpecificationMcpClientMap) {
        List<ToolExecutionResultMessage> toolExecutionMessages = new ArrayList<>();
        aiMessage.toolExecutionRequests().forEach(req -> {
            log.warn("tool exec request:{},", req);
            req = parseToolRequest(req);
            McpClient selectedMcpClient = null;
            for (Map.Entry<ToolSpecification, McpClient> entry : toolSpecificationMcpClientMap.entrySet()) {
                if (entry.getKey().name().equals(req.name())) {
                    selectedMcpClient = entry.getValue();
                }
            }
            if (null == selectedMcpClient) {
                toolExecutionMessages.add(ToolExecutionResultMessage.from(req,
                        "No Tool executor found for this tool request"));
                return;
            }
            try {
                final String result = selectedMcpClient.executeTool(req);
                log.info("tool execute result:{}", result);
                toolExecutionMessages.add(ToolExecutionResultMessage.from(req, result));
            } catch (Exception e) {
                log.debug("Error executing tool " + req, e);
                toolExecutionMessages.add(ToolExecutionResultMessage.from(req, e.getMessage()));
            }
        });
        return toolExecutionMessages;
    }

    /**
     * 将收到的内容转换成音频
     * 条件：系统设置了tts为服务端转换 && 答案类型为音频
     *
     * @param params          内部迭代方法入参
     * @param partialResponse 文本内容
     * @return 无
     */
    private void ttsOnPartialMessage(InnerStreamChatParams params, String partialResponse) {
        TtsJobInfo jobInfo = ttsJobCache.getIfPresent(params.getUser().getUuid());
        if (null != jobInfo && null != jobInfo.getTtsModelContext()
            && AdiConstant.TtsConstant.SYNTHESIZER_SERVER.equals(ttsSetting.getSynthesizerSide())
            && params.getAnswerContentType() == AdiConstant.ConversationConstant.ANSWER_CONTENT_TYPE_AUDIO) {
            jobInfo.getTtsModelContext().processPartialText(jobInfo.getJobId(), partialResponse);
        }
    }

    /**
     * 结束 TTS 任务并返回任务信息。
     *
     * @param params 内部迭代方法入参
     * @return TTS 任务信息
     */
    private TtsJobInfo ttsOnComplete(InnerStreamChatParams params) {
        TtsJobInfo jobInfo = ttsJobCache.getIfPresent(params.getUser().getUuid());
        if (null != jobInfo && null != jobInfo.getTtsModelContext()) {
            // TODO：停止转换任务可能导致仅合成部分音频，需后续完善
            jobInfo.getTtsModelContext().complete(jobInfo.getJobId());
        }
        // 移除任务信息，避免缓存残留
        ttsJobCache.invalidate(params.getUser().getUuid());
        return jobInfo;
    }

    /**
     * 关闭 MCP 客户端连接。
     *
     * @param mcpClients MCP 客户端列表
     * @return 无
     */
    private void closeMcpClients(List<McpClient> mcpClients) {
        mcpClients.forEach(item -> {
            try {
                item.close();
            } catch (Exception e) {
                log.error("close mcp client error", e);
            }
        });
    }

    /**
     * 如果工具请求参数中没有包含id，则手动解析该参数以补充 id 和 name
     * 部分模型（如硅基流动）返回的工具请求可能没有id和name，需要手动解析，如 ToolExecutionRequest { id = "", name = "", arguments = "maps_weather {"city": "广州"}" }
     *
     * @param req 工具请求参数
     * @return 解析后的工具请求
     */
    private ToolExecutionRequest parseToolRequest(ToolExecutionRequest req) {
        if (StringUtils.isBlank(req.id())) {
            String arguments = req.arguments();
            String name = req.name();
            if (StringUtils.isBlank(name) && StringUtils.isNotBlank(arguments) && !arguments.startsWith("{")) {
                String[] args = arguments.split(" ");
                if (args.length > 0) {
                    name = args[0];
                    arguments = arguments.substring(name.length()).trim();
                } else {
                    name = "name_" + UuidUtil.createShort();
                }
            }
            return ToolExecutionRequest.builder().id("id_" + UuidUtil.createShort()).name(name).arguments(arguments).build();
        }
        return req;
    }
}
