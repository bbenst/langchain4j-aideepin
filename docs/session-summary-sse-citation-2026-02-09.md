# 完整对话记录：SSE 回答与知识库引用（2026-02-09）

> 说明：以下为本次会话的逐轮问答记录（用户/助手），保留问题与回答原意，按发生顺序整理。

## 1
**用户：**
com.moyz.adi.common.service.ConversationMessageService#sseAsk 模拟响应的数据

**助手：**
`ConversationMessageService#sseAsk`（接口：`POST /conversation/message/process`）可按下面模拟 SSE 响应。

```text
event: [START]
data:

event: [STATE_CHANGED]
data: {"state":"question_analysing","remark":"问题分析中"}

event: [STATE_CHANGED]
data: {"state":"knowledge_searching","remark":"知识库搜索中"}

event: [THINKING]
data: 我先拆解你的问题并整理答案结构...

data:  这是回答第一段
data: -_wrap_-
data:  这是回答第二段（换行后）

event: [AUDIO]
data: UklGRiQAAABXQVZFZm10...   // 可选，base64 音频分片

event: [DONE]
data:  [META]{"question":{"tokens":123,"uuid":"q_20260209143001abc"},"answer":{"tokens":456,"uuid":"a_20260209143002def","isRefEmbedding":true,"isRefGraph":false},"audioInfo":{"path":"/tmp/tts/a_20260209143002def.wav","uuid":"file_xxx","url":"https://xxx/audio/file_xxx","duration":12}}
```

错误场景可模拟为：

```text
event: [START]
data:

event: [ERROR]
data: 音频解析失败，请检查音频文件是否正确
```

---

## 2
**用户：**
回答和 回答中对知识库的引用，前端怎么区分

**助手：**
前端可以这样区分：

1. 回答正文  
- 来自 **无 event 名称的 SSE `data` 分片**（`parseAndSendPartialMsg(..., "", content)`）。  
- 你把这些分片拼接起来，就是最终回答文本。  

2. 是否有知识库引用  
- 在结束事件 `event: [DONE]` 的 `data` 里，包含 `[META]{...}`。  
- 解析 `meta.answer` 中两个标记：  
  - `isRefEmbedding`：是否有向量检索引用  
  - `isRefGraph`：是否有图谱引用  

3. 引用内容本体怎么拿  
- `meta.answer.uuid` 就是答案消息 UUID。  
- 用这个 UUID 调接口拿引用详情：  
  - `/conversation/message/embedding-ref/{uuid}`  
  - `/conversation/message/graph-ref/{uuid}`  

简化流程：  
- 流式阶段只渲染回答文本。  
- 收到 `[DONE] + [META]` 后，如果 `isRefEmbedding/isRefGraph=true`，再按 `answer.uuid` 拉引用并渲染“来源/知识库引用”区域。  

---

## 3
**用户：**
一个回答有很多引用，并且是在不同的段落，项目有处理吗，前端又是怎么区分

**助手：**
当前项目**没有做“段落级引用绑定”**，是“整条回答级别”的引用。

1. 后端处理现状  
- 回答流式正文：直接按 SSE 分片发送纯文本，不带引用 ID 或段落位置信息。  
- 引用落库：`createRef(...)` 只按 `messageId` 保存引用，没有段落号、start/end offset。  
- 向量引用表字段仅 `message_id/embedding_id/score`。  
- 图谱引用表也是 `message_id + 图谱JSON`。  

2. 前端现在怎么区分  
- 先渲染回答文本（SSE 普通 `data` 分片）。  
- 收到 `[DONE]` 里的 `[META]` 后，看 `answer.isRefEmbedding / answer.isRefGraph` 判断“这条回答是否有引用”。  
- 再用 `answer.uuid` 调：  
  - `/conversation/message/embedding-ref/{uuid}`  
  - `/conversation/message/graph-ref/{uuid}`  
  接口返回的是**整条回答的引用集合**，不是按段落返回。  

3. 结论  
- “一个回答很多引用且分布在不同段落”这个场景，当前后端不支持精确区分到每段。  
- 前端最多做到“回答整体有这些引用”，不能可靠知道“第几段对应哪条引用”。

---

## 4
**用户：**
如果要实现这个功能，有什么思路

**助手：**
• 我先给你一版可落地的方案框架，并通过两个关键选型把实现
边界锁定，这样后续可以直接拆任务开发。

• Questions 2/2 answered
• 段落与引用的绑定方式你希望采用哪一种？
answer: 模型显式标注(Recommended)
• 前端接收引用信息时，你更倾向哪种协议？
answer: SSE增量引用事件(Recommended)

• Proposed Plan


### 标题

对话回答“段落级知识库引用”实现方案（SSE 增量 + 模型显
式标注）

### 摘要

基于你选定的方向：

- 绑定策略：模型显式标注
- 返回协议：SSE 增量引用事件

核心思路是把“回答文本”从纯字符串升级为“段落数组（每段
携带引用ID列表）”，服务端在流式输出时同步发送段落与引
用映射，前端按段落实时渲染“这段用了哪些知识库片段”。

### 公共接口与数据结构变更

1. 新增 SSE 事件名

- event: [CITATION_PARAGRAPH]：发送单个段落增量。
- event: [CITATION_REF]：发送引用实体（去重后按
  citationId 下发）。
- event: [CITATION_DONE]：引用流结束标记（可选）。

2. DONE 的 META 扩展

- 在现有 answer.isRefEmbedding/isRefGraph 基础上新
  增：
  - citationMode: "paragraph"
  - paragraphCount
  - refCount
  - hasCitationError（模型输出结构异常时标记）

3. 新增后端内部 DTO（建议）

- CitationParagraphChunk { paragraphIndex, text,
  citationIds[] }
- CitationRefItem { citationId, type(embedding|
  graph), payload }
- CitationAggregateResult { paragraphs[], refs[] }

4. 保持现有接口兼容

- /conversation/message/embedding-ref/{uuid}、/graph-
  ref/{uuid} 保留。
- 新前端优先用 SSE 引用事件；旧前端仍可走原“回答完成
  后查整条引用”。

### 后端实现设计

1. 检索阶段

- 保留当前 retrieverWrappers 逻辑。
- 生成稳定 citationId（如 E1/E2... 对 embedding，
  G1... 对 graph）。

2. 提示词与模型输出约束

- 在系统提示词中强约束模型输出 JSON：
  - paragraphs: [{text, citationIds[]}]
  - citationIds 只能使用已提供 ID。
- 若模型不按 JSON 返回，进入降级流程（见失败模式）。

3. 流式输出策略

- 模型流式返回时先在服务端做“结构累积解析”（增量 JSON
  parser）。
- 每解析出一个完整段落，立即发 [CITATION_PARAGRAPH]。
- 引用实体首次被段落引用时，发 [CITATION_REF]（去
  重）。
- 文本仍可继续保留原 data: 分片，确保旧前端不受影响。

4. 持久化

- 现有引用表继续保存“messageId -> 引用实体”。
- 新增段落映射表（建议）：
  - adi_conversation_message_citation_paragraph
  - 字段：id, message_id, paragraph_index, text,
    citation_ids_json, created_at
- 若短期不加新表，可先把段落映射 JSON 挂到回答消息扩
  展字段（不推荐长期）。

5. 降级与容错

- 模型结构不合法：
  - hasCitationError=true
  - 继续输出纯文本（不中断回答）
  - 仅保留“整条回答引用集合”
- 引用ID越界/不存在：忽略该ID并记录告警日志。

### 前端渲染方案

1. 状态机

- START -> 正文流 -> 引用段落流 -> DONE
- 正文与引用分离渲染，避免互相阻塞。

2. 渲染规则

- 收到 [CITATION_PARAGRAPH]：按 paragraphIndex 插入/
  更新段落。
- 收到 [CITATION_REF]：更新引用字典 citationId -> 引
  用详情。
- 段落内点击 citationId，可展开对应引用原文或图谱信
  息。

3. 兼容

- 若收不到新事件，回退到旧逻辑：根据 answer.uuid 调引
  用接口并整体展示。

### 测试与验收

1. 单元测试

- JSON 增量解析：跨 chunk、半包、非法结构。
- citationId 去重与映射正确性。
- 降级分支：非法输出不影响主回答。

2. 集成测试

- SSE 事件顺序：
  - [START] -> [STATE_CHANGED] -> data/[THINKING]
    -> [CITATION_*] -> [DONE]
- 多段落多引用场景：每段 citationIds 精确可追踪。
- 无引用场景：不发送 [CITATION_REF]，refCount=0。

3. 前端验收场景

- 一条回答 5 段、每段不同引用。
- 同一引用被多段复用。
- 流中断/重连时段落不乱序（按 paragraphIndex 幂等更
  新）。

### 默认假设与已选定项

- 已选：模型显式标注 + SSE增量引用事件。
- 默认保留现有 SSE 文本分片协议，确保旧前端兼容。
- 默认新增段落映射持久化（推荐新表），避免只靠运行时
  缓存。
---

## 5
**用户：**
整个流程和交互有什么区别，数据有什么变化

**助手：**
下面按“现状 vs 新方案”对比。

流程区别

1. 现状流程

- SSE 推正文分片（data）
- 结束时 [DONE]+[META] 只告诉“有没有引用”
- 前端再用 answer.uuid 拉整条引用列表
- 前端只能“整条回答展示引用”

2. 新方案流程

- SSE 推正文分片（兼容保留）
- 同步推段落级事件 [CITATION_PARAGRAPH]（段落
  +citationIds）
- 同步推引用实体 [CITATION_REF]（citationId -> 引用内
  容）
- [DONE]+[META] 给统计与状态
- 前端可“边生成边按段落挂引用”

交互区别

1. 用户体验

- 现状：回答结束后才看到引用，且是整体一坨。
- 新方案：回答过程中就能看到“第N段对应哪些引用”。

2. 前端交互

- 现状：点击“引用”通常是全局列表。
- 新方案：每段旁边有 citation 标记，可段落内展开/高亮对
  应来源。

3. 容错行为

- 现状：无段落绑定概念。
- 新方案：若结构化失败，自动降级为现状（不影响回答）。

数据变化

1. SSE 协议新增

- 新增事件：[CITATION_PARAGRAPH]、[CITATION_REF]（可选
  [CITATION_DONE]）。

2. META 扩展

- 新增字段：citationMode、paragraphCount、refCount、
  hasCitationError。

3. 持久化变化

- 现状：仅“messageId -> 引用实体（embedding/graph）”。
- 新方案：增加“messageId -> paragraphIndex ->
  citationIds[]”映射（建议新表）。

4. 前端状态数据

- 现状：answerText + refs[]。
- 新方案：paragraphs[] + refDict(citationId->detail) +
  legacy answerText。

如果你愿意，我下一步可以把这部分整理成“前后端字段清单 +
示例 SSE 报文（逐条）”的实现规格稿。


› - 同步推段落级事件 [CITATION_PARAGRAPH]（段落
+citationIds）
- 同步推引用实体 [CITATION_REF]（citationId -> 引用
内
容）
- [DONE]+[META] 给统计与状态
- 前端可“边生成边按段落挂引用”,这是要怎么实现，用自
然语言描述


• 可以，把它想象成“直播字幕 + 脚注卡片”两条流并行。

1. 后端先做一件准备工作

- 在开始回答前，先把检索到的知识片段编号，比如 E1、E2、
  E3（向量引用），G1（图谱引用）。
- 这些编号在本次回答里固定不变，后面段落只引用编号，不
  重复传大文本。

2. 大模型在生成回答时，不只给“纯文本”，而是按段落给结构

- 每一段都包含两部分：
  - 这段文本内容
  - 这段使用了哪些引用编号（如 ["E1","G1"]）

3. 后端把这段结构实时拆成两类 SSE 事件发给前端

- [CITATION_PARAGRAPH]：发“第几段 + 段落文本 +
  citationIds”
- [CITATION_REF]：发“citationId 对应的引用详情”（标题、
  原文片段、来源等）
- 规则是：引用详情只在第一次出现时发一次，后面段落复用
  同一个 ID。

4. 前端维护两个内存区

- paragraphs：按段落顺序存文本和 citationIds。
- refDict：按 citationId 存引用详情。

5. 前端渲染时“就地挂引用”

- 收到段落事件就立刻显示该段文字。
- 段落里有 citationIds，就从 refDict 查详情，查到了就显
  示脚注/悬浮卡。
- 如果段落先到、详情后到，先显示占位，等 [CITATION_REF]
  到了自动补全。

6. 结束时用 [DONE]+[META] 收口

- 告诉前端这次总段落数、总引用数、是否有引用解析异常。
- 前端据此做最终校验和状态收敛（如去掉“加载中”）。

一句话：
以前是“整篇写完再给参考资料”，现在是“每写一段就顺带告诉
你这段参考了哪几条资料”。

---

## 7
**用户：**
怎么解决llm的幻觉、不按提示输出等问题

**助手：**

• 用“多层防线”，不要指望只靠提示词。针对你这个“段落+引
用”场景，建议按下面做：

1. 输入侧先降幻觉

- 只给模型“可引用候选集”（E1/E2/G1...），禁止自由编造来
  源。
- 提示词明确“引用ID只能从候选集中选；没有证据就写不知
  道”。
- 温度降到低值（如 0~0.3），减少随机发挥。

2. 生成侧强约束输出格式

- 用 JSON schema / structured output / function
  calling，不要纯文本约定。
- 要求输出固定结构：paragraphs[].text +
  paragraphs[].citationIds[]。
- 若模型支持 response_format=json_schema，优先启用。

3. 服务端校验器（核心）

- 每个 chunk/段落都做程序校验：
  - JSON 是否合法
  - citationIds 是否都在候选集
  - 段落是否空、顺序是否错
- 校验失败不直接透传，进入“修复或重试”。

4. 自动修复与重试策略

- 第1步：小修复（JSON repair）
- 第2步：定向重试（只重生当前段落，不重跑整篇）
- 第3步：超过重试上限就降级：
  - 输出纯文本
  - hasCitationError=true
  - 前端走“整条引用”展示

5. 事实一致性检查（抑制幻觉）

- 对“段落文本 vs 引用片段”做语义对齐检查（NLI/相似度/关
  键词覆盖）。
- 不满足阈值就去掉该引用或重试该段。
- 高风险回答加“拒答策略”：证据不足时明确说“未在知识库中
  找到依据”。

6. 前端只信后端“已验证结果”

- 前端不自行猜引用绑定，只渲染后端下发的 citationIds。
- 对 hasCitationError 显示“引用可能不完整”的降级提示。

7. 持续治理

- 建离线评测集（你们真实问答样本），跟踪：
  - 格式合规率
  - 非法 citationId 率
  - 引用一致性通过率
  - 幻觉率（人工抽检）
- 每次改提示词/模型后跑回归，不靠感觉调。

最关键一句：提示词只是第一层，真正稳定靠“结构化输出 +
服务端强校验 + 失败降级”。
---
