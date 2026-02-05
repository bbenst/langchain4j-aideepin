# 数据库设计与关联解读

本说明基于 `docs/create.sql`，用于帮助快速理解数据库结构与表之间的关联关系。

## 业务域划分
- 用户与配额：`adi_user`、`adi_user_day_cost`
- 会话与消息：`adi_conversation`、`adi_conversation_message`、`adi_conversation_preset`、`adi_conversation_preset_rel`
- 模型与平台：`adi_model_platform`、`adi_ai_model`
- 知识库：`adi_knowledge_base`、`adi_knowledge_base_item`、`adi_knowledge_base_qa`、`adi_knowledge_base_star`
- 知识库检索引用：`adi_knowledge_base_qa_ref_embedding`、`adi_knowledge_base_qa_ref_graph`、`adi_conversation_message_ref_embedding`、`adi_conversation_message_ref_graph`
- 文件与提示词：`adi_file`、`adi_prompt`
- 搜索记录：`adi_ai_search_record`
- 绘图与互动：`adi_draw`、`adi_draw_star`、`adi_draw_comment`
- 工作流：`adi_workflow`、`adi_workflow_component`、`adi_workflow_node`、`adi_workflow_edge`、`adi_workflow_runtime`、`adi_workflow_runtime_node`
- MCP：`adi_mcp`、`adi_user_mcp`
- 系统配置：`adi_sys_config`

## 核心关系（逻辑外键）
- `adi_user` 1:N `adi_conversation`，通过 `adi_conversation.user_id`
- `adi_conversation` 1:N `adi_conversation_message`，通过 `adi_conversation_message.conversation_id / conversation_uuid`
- `adi_ai_model` 1:N `adi_conversation_message`，通过 `adi_conversation_message.ai_model_id`
- `adi_model_platform` 1:N `adi_ai_model`，通过 `adi_ai_model.platform` 对应 `adi_model_platform.name`
- `adi_user` 1:N `adi_user_day_cost`，通过 `adi_user_day_cost.user_id`

## 知识库关系
- `adi_user` 1:N `adi_knowledge_base`，通过 `adi_knowledge_base.owner_id`
- `adi_knowledge_base` 1:N `adi_knowledge_base_item`，通过 `adi_knowledge_base_item.kb_id / kb_uuid`
- `adi_knowledge_base` 1:N `adi_knowledge_base_qa`，通过 `adi_knowledge_base_qa.kb_id / kb_uuid`
- `adi_knowledge_base` 1:N `adi_knowledge_base_star`，通过 `adi_knowledge_base_star.kb_id`
- `adi_knowledge_base_item` 关联 `adi_file`，通过 `adi_knowledge_base_item.source_file_id` 对应 `adi_file.id`

## 检索引用关系（向量 / 图谱）
- `adi_conversation_message` 1:N `adi_conversation_message_ref_embedding`，通过 `message_id`
- `adi_conversation_message` 1:N `adi_conversation_message_ref_graph`，通过 `message_id`
- `adi_knowledge_base_qa` 1:N `adi_knowledge_base_qa_ref_embedding`，通过 `qa_record_id`
- `adi_knowledge_base_qa` 1:N `adi_knowledge_base_qa_ref_graph`，通过 `qa_record_id`

## 绘图关系
- `adi_draw` 1:N `adi_draw_comment`，通过 `adi_draw_comment.draw_id`
- `adi_draw` 1:N `adi_draw_star`，通过 `adi_draw_star.draw_id`
- `adi_draw` 关联 `adi_ai_model`，通过 `adi_draw.ai_model_id`

## 会话预设关系
- `adi_conversation_preset` 通过 `adi_conversation_preset_rel` 关联 `adi_conversation`
- 关联字段为 `preset_conv_id` 与 `user_conv_id`

## 工作流关系
- `adi_workflow` 1:N `adi_workflow_node`，通过 `adi_workflow_node.workflow_id`
- `adi_workflow` 1:N `adi_workflow_edge`，通过 `adi_workflow_edge.workflow_id`
- `adi_workflow_runtime` 1:N `adi_workflow_runtime_node`，通过 `adi_workflow_runtime_node.workflow_runtime_id`
- `adi_workflow_node` 关联 `adi_workflow_component`，通过 `adi_workflow_node.workflow_component_id`

## MCP 关系
- `adi_mcp` 1:N `adi_user_mcp`，通过 `adi_user_mcp.mcp_id`
- `adi_user` 1:N `adi_user_mcp`，通过 `adi_user_mcp.user_id`

## 设计特点
- 主键以 `id` 为主，业务标识常以 `uuid` 为辅。
- 多数表采用 `is_deleted` 做逻辑删除。
- 表普遍包含 `create_time / update_time`，通过触发器统一维护更新时间。
- 关联关系主要依靠字段命名与代码约定维护，而非强制外键约束。

## 进一步延伸选项
如果需要更深层理解，可以选择以下方向继续补充：
- 输出文字版 ER 结构说明
- 以“聊天”或“知识库检索”为主线给出关系链示意
- 关键表字段用途的更细粒度解读
