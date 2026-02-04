# asyncCheckAndChat(...) 代码流程说明和数据流

本文说明 `ConversationMessageService.asyncCheckAndChat(...)` 的整体代码流程与数据流、变更点，并用一个具体例子串联。

## 代码流程说明（分步）

1. **业务校验**
   - 调用 `checkConversation(...)` 检查：对话是否删除、对话数量是否超限、用户配额是否充足。
   - 若失败：通过 SSE 返回错误并结束。

2. **对话读取**
   - 读取 `Conversation` 配置，如系统消息、知识库、上下文理解、联网搜索等。
   - 若对话不存在：通过 SSE 返回错误并结束。

3. **状态通知**
   - 发送 “问题分析中” 状态到前端，提升交互反馈。

4. **语音转文本（如有）**
   - 若 `askReq.audioUuid` 不为空，调用 ASR 将音频转为文本。
   - 失败则提示并结束。

5. **知识库筛选与状态通知**
   - 若对话关联知识库：过滤有效知识库并向前端发送“知识库检索中”状态。

6. **检索与提示词增强**
   - 并行检索对话记忆与知识库内容。
   - 构建增强提示词 `processedPrompt`（原始问题 + 记忆 + 知识库 + 语音指令）。
   - 若增强后不同：写回 `askReq.processedPrompt`。

7. **构建模型请求参数**
   - 组装 `ChatModelRequestParams` 与 `SseAskParams`，包含温度、系统消息、上下文、MCP、推理过程与联网搜索等参数。

8. **模型调用与 SSE 输出**
   - 通过 `sseEmitterHelper.call(...)` 发起模型调用。
   - 处理返回内容（包括可选音频路径），并向前端发送完成事件。

9. **落库与成本统计**
   - 保存问题与回答消息。
   - 保存向量/图谱引用。
   - 统计 token 消耗并更新用户日成本。

10. **记忆更新**
    - 短期记忆：更新 `MapDBChatMemoryStore`。
    - 长期记忆：异步写入 `LongTermMemoryService`。

## 数据流与变更（示例）

**示例设定**
- 用户 `U1` 在对话 `C-uuid` 中提问：“公司年报在哪里？”
- 对话关联知识库 `KB1/KB2`
- 启用上下文理解与联网搜索
- 无语音输入，模型为非免费且支持推理过程

### 1. 入参数据流（入口）
- `askReq` 初始：
  - `conversationUuid = C-uuid`
  - `prompt = "公司年报在哪里？"`
  - `modelPlatform/modelName = 某模型`
  - `audioUuid = ""`
  - `imageUrls = []`
- `user = U1`

### 2. 校验阶段（可能中断，无数据写入）
- 校验对话删除、数量、用户配额。
- 若失败：SSE 返回错误并结束。

### 3. 对话与知识库读取（只读）
- 读取 `Conversation`：
  - `kbIds = "1,2"`
  - `understandContextEnable = true`
  - `isEnableWebSearch = true`

### 4. 检索阶段（生成增强上下文）
- 并行检索：
  - 记忆检索器（`RetrieveContentFrom.CONV_MEMORY`）
  - 知识库检索器（`RetrieveContentFrom.KNOWLEDGE_BASE`）
- 形成：
  - `memoryText = "上次讨论了年报下载入口..."`
  - `knowledgeText = "KB1: 年报在官网投资者关系页..."`

### 5. Prompt 处理（askReq 发生变更）
- 生成 `processedPrompt`：
  - 原始问题 + 记忆 + 知识库片段（+语音指令可选）
- 若不同：`askReq.setProcessedPrompt(processedPrompt)`

### 6. 构建请求参数（生成新对象）
- `ChatModelRequestParams`：
  - `systemMessage`（来自对话）
  - `memoryId = conversationUuid`（启用上下文）
  - `userMessage = processedPrompt`
  - `enableWebSearch = true`
  - `returnThinking = true/false`
- `SseAskParams`：
  - `uuid = questionUuid`
  - `answerContentType`

### 7. 模型调用与 SSE 输出（生成 response + 元信息）
- 返回：
  - `response.content = "年报在官网投资者关系页面..."`
  - `response.thinkingContent = "...推理过程..."`
  - `response.audioPath = ""`
- SSE：
  - 返回 `questionMeta/answerMeta` 和引用标记

### 8. 落库与费用（持久化变更）
- 新增问题消息 `ConversationMessage`
  - `remark = 原始 prompt`
  - `processedRemark = processedPrompt`
- 新增回答消息 `ConversationMessage`
  - `remark = response.content`
  - `thinkingContent = response.thinkingContent`
- 引用记录：
  - `ConversationMessageRefEmbedding` / `ConversationMessageRefGraph`（视检索结果）
- 成本统计：
  - 更新 `Conversation.tokens`
  - 更新 `UserDayCost`

### 9. 记忆更新（状态变更）
- 短期记忆：`MapDBChatMemoryStore` 追加 AI 回复
- 长期记忆：`LongTermMemoryService.asyncAdd(...)` 异步写入

## 变更清单（简明）
- `askReq.processedPrompt` 可能被写入
- DB 新增：
  - `ConversationMessage`（问题/回答）
  - `ConversationMessageRefEmbedding` / `ConversationMessageRefGraph`
- DB 更新：
  - `Conversation.tokens`
  - `UserDayCost`
- 内存更新：
  - 短期记忆 MapDB
  - 长期记忆异步队列
