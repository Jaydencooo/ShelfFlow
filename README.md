# ShelfFlow

ShelfFlow 是一个临期库存智能流转平台。当前仓库已经从旧的外卖单体改造为新的工程主线：

- `apps/shelfflow-admin-web`：管理端 Web
- `apps/shelfflow-user-web`：用户端 Web，已接真实 Java 后端接口
- `services/shelfflow-gateway`：Spring Cloud Gateway 统一入口
- `services/shelfflow-auth-service`：Spring Boot 管理端认证服务
- `services/shelfflow-admin-service`：Spring Boot 管理端聚合服务
- `services/shelfflow-user-service`：Spring Boot 用户端服务
- `packages/shelfflow-contracts`：共享契约、DTO schema、领域枚举
- `packages/shelfflow-config`：配置加载与校验
- `packages/shelfflow-shared`：统一错误、响应和日志工具
- `shelfflow-backend`：legacy Java backend，作为第一阶段迁移来源和当前运行依赖

## 当前完成范围

当前仓库已经形成管理端、用户端和 Java 微服务的可运行主线：

- 管理员登录
- 商品管理：分页、创建、更新
- 批次管理：分页、创建、更新、状态流转
- 订单生命周期：用户下单、支付，管理端流转到完成
- 定价规则：规则 CRUD、状态管理、运营建议
- 损耗统计：概览、分类明细、优化建议
- AI 运营助手：知识库维护、运营建议、问答
- 目录分类
- 商品分页
- 商品详情
- 用户注册 / 登录 / 找回密码 / 会话
- 购物车列表 / 加购 / 删除 / 清空
- 订单提交 / 支付 / 取消 / 详情
- 订单审计事件时间线

## 目录结构

```text
ShelfFlow
├── apps
│   ├── shelfflow-admin-web
│   └── shelfflow-user-web
├── packages
│   ├── shelfflow-config
│   ├── shelfflow-contracts
│   └── shelfflow-shared
├── services
│   ├── shelfflow-gateway
│   ├── shelfflow-auth-service
│   ├── shelfflow-admin-service
│   ├── shelfflow-user-service
│   └── shelfflow-service-common
├── shelfflow-backend
├── docs
└── scripts
```

## 架构与面试资料

- 架构说明：[/Users/coconut/Desktop/ShelfFlow/docs/ARCHITECTURE.md](/Users/coconut/Desktop/ShelfFlow/docs/ARCHITECTURE.md)
- 面试讲解指南：[/Users/coconut/Desktop/ShelfFlow/docs/INTERVIEW_GUIDE.md](/Users/coconut/Desktop/ShelfFlow/docs/INTERVIEW_GUIDE.md)
- 联调验收手册：[/Users/coconut/Desktop/ShelfFlow/docs/联调验收手册.md](/Users/coconut/Desktop/ShelfFlow/docs/联调验收手册.md)

## 环境与端口

统一环境变量入口在 [scripts/shelfflow-env.sh](/Users/coconut/Desktop/ShelfFlow/scripts/shelfflow-env.sh)，可选模板见 [.env.example](/Users/coconut/Desktop/ShelfFlow/.env.example)：

- 管理端 Web：`3000`
- Gateway：`4010`
- Auth Service：`4011`
- Admin Service：`4012`
- User Service：`4013`
- Legacy Backend：`8082`
- MySQL：`3306`
- Redis：`6379`
- RabbitMQ：`5672` / 管理台 `15672`

## 安装依赖

```bash
cd /Users/coconut/Desktop/ShelfFlow
npm install
```

服务侧使用 Maven 多模块工程，依赖由 Maven 管理。

## 本地启动

先准备本地环境变量：

```bash
cd /Users/coconut/Desktop/ShelfFlow
cp .env.example .env.local
```

如果本机没有手动启动 MySQL / Redis / RabbitMQ，可以先用 Docker Compose 启动基础设施：

```bash
cd /Users/coconut/Desktop/ShelfFlow
bash scripts/start-infra.sh
```

停止 Docker 基础设施：

```bash
cd /Users/coconut/Desktop/ShelfFlow
bash scripts/stop-infra.sh
```

如果要连同 MySQL / Redis / RabbitMQ 数据卷一起删除：

```bash
cd /Users/coconut/Desktop/ShelfFlow
SHELFFLOW_INFRA_REMOVE_VOLUMES=true bash scripts/stop-infra.sh
```

也可以让完整启动脚本自动拉起基础设施：

```bash
cd /Users/coconut/Desktop/ShelfFlow
START_INFRA=true bash scripts/start-local-all.sh
```

如果只启动后端服务层和管理端：

```bash
cd /Users/coconut/Desktop/ShelfFlow
bash scripts/start-local-all.sh
```

如果要连用户端 Web 一起启动：

```bash
cd /Users/coconut/Desktop/ShelfFlow
START_USER_WEB=true bash scripts/start-local-all.sh
```

如果要同时自动启动基础设施、管理端和用户端：

```bash
cd /Users/coconut/Desktop/ShelfFlow
START_INFRA=true START_USER_WEB=true bash scripts/start-local-all.sh
```

单独启动管理端前端：

```bash
cd /Users/coconut/Desktop/ShelfFlow
bash scripts/dev-admin.sh
```

`start-local-all.sh` 会按顺序初始化数据库、启动 legacy backend、四个 Java 服务和管理端 Web；`START_INFRA=true` 会先执行 `scripts/start-infra.sh`；`dev-admin.sh` 用于单独调试前端。

停止本地链路：

```bash
cd /Users/coconut/Desktop/ShelfFlow
bash scripts/stop-local-all.sh
```

单独启动用户端前端：

```bash
cd /Users/coconut/Desktop/ShelfFlow
bash scripts/dev-user-web.sh
```

用户端默认地址：

```text
http://127.0.0.1:3001
```

如果要一次性完成本地联调和冒烟，可直接执行：

```bash
cd /Users/coconut/Desktop/ShelfFlow
bash scripts/admin-e2e.sh
```

如果要在验收通过后保留服务，便于继续做浏览器联调：

```bash
cd /Users/coconut/Desktop/ShelfFlow
KEEP_SERVICES_RUNNING=true START_USER_WEB=true bash scripts/admin-e2e.sh
```

如果希望把用户端浏览器级 UI smoke 一并纳入整体验收：

```bash
cd /Users/coconut/Desktop/ShelfFlow
KEEP_SERVICES_RUNNING=true START_USER_WEB=true RUN_USER_WEB_UI_SMOKE=true bash scripts/admin-e2e.sh
```

如果希望 e2e 脚本自动启动基础设施：

```bash
cd /Users/coconut/Desktop/ShelfFlow
START_INFRA=true KEEP_SERVICES_RUNNING=true START_USER_WEB=true RUN_USER_WEB_UI_SMOKE=true bash scripts/admin-e2e.sh
```

这会在通过管理端和跨端订单 smoke 后保留：

- Gateway
- legacy backend
- auth-service
- admin-service
- user-service
- admin web
- user web

`start-infra.sh` 会读取 `.env.local` 中的 `DB_*`、`REDIS_*`、`RABBITMQ_*` 配置启动 Docker 基础设施，不再在 `docker-compose.yml` 中硬编码密码和端口。`stop-infra.sh` 默认只停止容器并保留数据卷，只有显式设置 `SHELFFLOW_INFRA_REMOVE_VOLUMES=true` 时才删除数据。

`init-db.sh` 会按 `.env.local` 连接本地 MySQL，自动创建 `shelfflow` 库，并在缺失 `staff/product/inventory_batch` 表时导入 [shelfflow-backend/mysql.sql](/Users/coconut/Desktop/ShelfFlow/shelfflow-backend/mysql.sql)。增量 SQL 放在 [migrations](/Users/coconut/Desktop/ShelfFlow/shelfflow-backend/docs/migrations) 下，脚本会写入 `schema_migration`，同一个迁移只执行一次；如果同名迁移内容发生变化会直接失败，避免本地库和代码漂移。

## 校验

后端微服务冒烟：

```bash
cd /Users/coconut/Desktop/ShelfFlow
bash scripts/backend-smoke.sh
```

用户侧后端冒烟：

```bash
cd /Users/coconut/Desktop/ShelfFlow
bash scripts/user-backend-smoke.sh
```

用户端浏览器级 UI 冒烟：

```bash
cd /Users/coconut/Desktop/ShelfFlow
bash scripts/user-web-ui-smoke.sh
```

准备用户端演示账号：

```bash
cd /Users/coconut/Desktop/ShelfFlow
bash scripts/seed-user-demo-data.sh
```

Java 服务构建：

```bash
cd /Users/coconut/Desktop/ShelfFlow/services
mvn -DskipTests compile
```

管理端构建：

```bash
cd /Users/coconut/Desktop/ShelfFlow
npm run build -w @shelfflow/admin-web
```

用户端构建：

```bash
cd /Users/coconut/Desktop/ShelfFlow
npm run build -w @shelfflow/user-web
```

## 目录治理

- 已废弃的 TS 微服务树 `services-ts-deprecated` 已从正式主线移除。
- `shelfflow-backend` 继续保留，直到第一阶段 legacy 适配完全退出。
- 构建产物、日志、IDE 配置、本地 Maven 仓库不属于交付目录。
