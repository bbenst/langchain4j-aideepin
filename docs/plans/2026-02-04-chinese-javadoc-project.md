# 中文注释全量补充 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 为全项目所有 Java 类、接口、成员变量与方法补充规范的中文 JavaDoc，并在复杂逻辑上方补充中文行内注释。

**Architecture:** 以模块为单位分批处理，先基础模块（`adi-common`）再业务模块（`adi-chat`、`adi-admin`）最后启动模块（`adi-bootstrap`）。每个批次逐文件补注释，优先保留已有中文注释，翻译英文注释，并为复杂逻辑添加“为什么这么做”的行内注释。

**Tech Stack:** Java 17, Spring Boot, MyBatis, Maven

---

### Task 1: 建立改动范围与基线检查

**Files:**
- Modify: `adi-common/src/main/java/**`
- Modify: `adi-chat/src/main/java/**`
- Modify: `adi-admin/src/main/java/**`
- Modify: `adi-bootstrap/src/main/java/**`

**Step 1: 统计 Java 文件清单**

Run: `rg --files -g '*.java'`
Expected: 输出全量 Java 文件列表，后续分批处理

**Step 2: 选择第一批处理目录（基础通用模块）**

Run: `rg --files -g '*.java' adi-common/src/main/java`
Expected: 仅 `adi-common` 模块文件列表

**Step 3: 记录处理顺序与批次**

Document in this plan: 先 `adi-common`，再 `adi-chat`，再 `adi-admin`，最后 `adi-bootstrap`

**Step 4: Commit**

```bash
git status -sb
# 不提交，仅确认清洁或记录已有变更
```

---

### Task 2: 完成 `adi-common` 注释补全

**Files:**
- Modify: `adi-common/src/main/java/**`

**Step 1: 选择一个子包，逐文件补注释（如 `config/`）**

Example:
- `adi-common/src/main/java/com/moyz/adi/common/config/WebMvcConfig.java`
- `adi-common/src/main/java/com/moyz/adi/common/config/GlobalExceptionHandler.java`

**Step 2: 为类/接口/成员变量/方法添加中文 JavaDoc**

规则：
- 使用 `/** ... */`
- 方法包含 `@param`、`@return`、`@throws`

**Step 3: 在复杂逻辑上方添加中文行内注释**

示例：
```java
// 为了保证请求体可重复读取，这里使用包装器缓存流内容
```

**Step 4: 翻译英文注释并保留已有中文注释**

**Step 5: 继续下一个子包，直至 `adi-common` 全部完成**

**Step 6: Commit**

```bash
git add adi-common/src/main/java
# 不提交，等待全量完成后统一确认
```

---

### Task 3: 完成 `adi-chat` 注释补全

**Files:**
- Modify: `adi-chat/src/main/java/**`

**Step 1: 从 controller 包开始**

Example:
- `adi-chat/src/main/java/com/moyz/adi/chat/controller/ConversationController.java`
- `adi-chat/src/main/java/com/moyz/adi/chat/controller/AuthController.java`

**Step 2: 为类/方法补全中文 JavaDoc**

**Step 3: 复杂逻辑添加中文行内注释**

**Step 4: 翻译英文注释并保留已有中文注释**

**Step 5: 覆盖所有子包直到完成**

**Step 6: Commit**

```bash
git add adi-chat/src/main/java
```

---

### Task 4: 完成 `adi-admin` 注释补全

**Files:**
- Modify: `adi-admin/src/main/java/**`

**Step 1: 从 controller 包开始**

Example:
- `adi-admin/src/main/java/com/moyz/adi/admin/controller/AdminUserController.java`

**Step 2: 为类/方法补全中文 JavaDoc**

**Step 3: 复杂逻辑添加中文行内注释**

**Step 4: 翻译英文注释并保留已有中文注释**

**Step 5: 覆盖所有子包直到完成**

**Step 6: Commit**

```bash
git add adi-admin/src/main/java
```

---

### Task 5: 完成 `adi-bootstrap` 注释补全

**Files:**
- Modify: `adi-bootstrap/src/main/java/**`

**Step 1: 处理启动类与配置类**

Example:
- `adi-bootstrap/src/main/java/com/moyz/adi/BootstrapApplication.java`

**Step 2: 为类/方法补全中文 JavaDoc**

**Step 3: 复杂逻辑添加中文行内注释**

**Step 4: 翻译英文注释并保留已有中文注释**

**Step 5: Commit**

```bash
git add adi-bootstrap/src/main/java
```

---

### Task 6: 全量复查与一致性清理

**Files:**
- Modify: `adi-*/src/main/java/**`

**Step 1: 全局搜索英文注释与缺失 JavaDoc**

Run: `rg "//" adi-*/src/main/java`
Expected: 定位英文注释或缺失注释点，逐一补齐

**Step 2: 检查 JavaDoc 完整性**

Run: `rg "^\s*public .*\(" adi-*/src/main/java`
Expected: 所有公共方法均有 JavaDoc

**Step 3: 统一提交前检查**

Run: `git status -sb`
Expected: 仅包含注释改动

**Step 4: Commit**

```bash
git add adi-*/src/main/java
# 由用户决定提交信息
```
