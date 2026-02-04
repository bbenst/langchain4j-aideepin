# Repository Guidelines

## 项目结构与模块
- `adi-bootstrap/`: 主启动模块，包含 `BootstrapApplication` 与运行时配置（`src/main/resources/application*.yml`）。
- `adi-chat/`: 用户侧相关能力与接口实现。
- `adi-admin/`: 管理侧相关能力与接口实现。
- `adi-common/`: 通用组件与共享模型。
- `docs/`: 数据库初始化脚本（`docs/create.sql`）与文档。
- `docker-compose/`: 相关部署与依赖编排文件。
 - 目录示例：`adi-chat/src/main/java`、`adi-chat/src/main/resources`、`adi-common/src/main/java`。

## 架构概览
- 入口在 `adi-bootstrap`，负责装配 Spring Boot 与基础配置。
- 业务能力按用户侧与管理侧拆分到 `adi-chat` 与 `adi-admin`，公共对象与工具沉淀在 `adi-common`。
- 数据库初始化与结构演进优先落地到 `docs/create.sql`，避免分散在多处脚本。

## 构建、测试与本地运行
- 打包（跳过测试）：`mvn clean package -Dmaven.test.skip=true`
  - 生成各模块的可运行产物（主要在 `adi-bootstrap/target/`）。
- 只构建单模块：`mvn -pl adi-chat -am package` 或 `mvn -pl adi-admin -am package`。
- 运行（Jar）：
  - `cd adi-bootstrap/target`
  - `nohup java -jar -Xms768m -Xmx1024m -XX:+HeapDumpOnOutOfMemoryError adi-bootstrap-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev`
- 运行（Docker）：
  - `cd adi-bootstrap`
  - `docker build . -t aideepin:0.0.1`
  - `docker run -d -p 8888:9999 -e APP_PROFILE=dev -v "/data/aideepin/logs:/data/logs" aideepin:0.0.1`

## 编码风格与命名约定
- Java 17 + Spring Boot；优先遵循现有代码格式与结构。
- 命名：类/接口用 `PascalCase`，方法/变量用 `camelCase`，包名全小写。
- 配置文件使用 `application.yml` 与 `application-*.yml` 分环境管理。
- 资源与脚本保持可追溯：SQL 放 `docs/`，部署相关放 `docker-compose/`。

## 测试指南
- 当前仓库未包含 `src/test/java` 用例；如新增测试，请使用 Spring Boot Test（JUnit 5）。
- 测试类命名建议 `*Test`，放在模块对应的 `src/test/java` 下。
- 运行测试：`mvn test` 或 `mvn -pl adi-bootstrap -am test`。
 - 若新增集成测试，请说明依赖的数据库/向量库版本与启动方式。

## 提交与 PR 规范
- 提交信息保持简短、动词开头，可中英文混用（常见如 `fix ...`、`update ...`）。
- 若关联问题/PR 编号，使用 `(#123)` 结尾。
- PR 需说明：改动范围、影响模块、配置或 SQL 变更（如 `docs/create.sql`），以及必要的运行验证说明。
 - 若涉及接口变更，建议补充示例请求与响应片段，便于回归验证。

## 安全与配置提示
- 不要提交真实密钥；配置项使用占位符并在本地 `application-*.yml` 中覆盖。
- 数据库与模型平台的初始化参考 `docs/create.sql`，并确保配置与环境一致。
- 配置或依赖有变化时，同步更新 `README.md` 与 `docs/README.md`（如适用）。

## Agent 指令（必须遵守）
- 所有 Java 类、接口、成员变量与方法必须使用中文 JavaDoc 注释。
- 复杂逻辑需在代码行上方补充中文行内注释，解释“为什么这么做”。
- 已有中文注释请保留，英文注释需翻译为中文。
