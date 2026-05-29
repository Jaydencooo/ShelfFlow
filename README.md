# ShelfFlow

ShelfFlow 是一个面向社区自提场景的临期特惠零售系统。项目围绕“临期库存、动态定价、社区自提点、用户下单、订单履约、经营分析、AI 运营助手”形成管理端与用户端闭环，目标是作为 Java 后端面试项目展示企业级分层、微服务拆分、前后端联调、数据库版本化和业务建模能力。

## 项目结构

```text
ShelfFlow/
├── apps/
│   ├── shelfflow-admin-web/          # 管理端 Web，运营后台、AI 助手、订单履约
│   └── shelfflow-user-web/           # 用户端 Web，商城首页、购物车、订单、自提信息
├── services/
│   ├── shelfflow-auth-service/       # 管理端认证服务
│   ├── shelfflow-admin-service/      # 管理端业务服务
│   ├── shelfflow-user-service/       # 用户端业务服务
│   ├── shelfflow-gateway/            # Spring Cloud Gateway
│   ├── shelfflow-migration-service/  # Flyway 数据库迁移服务
│   └── shelfflow-service-common/     # 公共 DTO、安全、异常与通用能力
├── packages/                         # 前端共享配置与类型包
├── docs/
│   ├── nacos/                        # Nacos 配置中心样例
│   ├── seed/                         # 演示数据 SQL
│   ├── ACCEPTANCE_CHECKLIST.md       # 验收清单
│   ├── API_CONTRACT.md               # API 契约
│   ├── ARCHITECTURE.md               # 架构说明
│   ├── INTERVIEW_GUIDE.md            # 面试讲解材料
│   └── LOCAL_RUNBOOK.md              # 本地运行手册
├── scripts/                          # 本地前端启动脚本
├── docker-compose.yml                # MySQL / Redis / RabbitMQ
├── docker-compose.alibaba.yml        # Nacos / Sentinel Dashboard
├── .env.example
├── package.json
└── services/pom.xml
```

## 设计思路

- **业务闭环优先**：管理端维护商品、分类、批次、定价、自提点和订单履约；用户端浏览商品、加入购物车、选择自提点、下单支付并查看自提码。
- **微服务边界清晰**：按 gateway、auth、admin、user、migration、common 拆分，避免业务能力散落在前端或脚本中。
- **分层清晰**：Java 服务按 controller、application service、domain policy、persistence mapper 组织。
- **配置外置**：数据库、Redis、JWT、AI 大模型、邮箱、Nacos、Sentinel、Flyway 均通过环境变量或配置中心管理。
- **阿里巴巴组件增强**：接入 Nacos Discovery/Config 与 Sentinel，支持服务注册发现、集中配置、接口限流和 Gateway 路由保护。
- **数据库版本化**：独立 Flyway migration service 管理表结构版本，业务服务不抢占式建表。
- **库存并发保护**：用户下单支持可选 Redis Lua 原子预占，数据库条件更新仍作为最终一致性防线。
- **事件驱动扩展**：用户订单支持可选 RabbitMQ 领域事件发布，事务提交后异步通知下游履约、分析或消息模块。

## 核心模块职责

- **Admin Web**：商品/分类/批次管理、定价规则、自提点、订单履约、经营分析、操作日志、AI 运营助手。
- **User Web**：商城首页、商品筛选、购物车、注册登录、验证码、个人资料、自提信息、订单列表与详情。
- **Auth Service**：管理端登录认证和会话签发。
- **Admin Service**：运营后台业务，包括商品库存、订单流转、自提核销、AI 建议、操作日志和经营指标。
- **User Service**：用户侧业务，包括账号注册、登录、验证码、商品目录、购物车、下单、支付、自提点查询。
- **Gateway**：统一入口、路由转发、跨域边界、Nacos 服务名路由和 Sentinel 网关保护。
- **Migration Service**：执行 Flyway 数据库迁移，沉淀基础结构和增量变更。
- **Common**：共享 DTO、权限模型、响应结构、异常处理和安全工具。

## 环境要求

- JDK 17+
- Maven 3.9+
- Node.js 24+
- npm 11+
- MySQL 8+
- 可选：Redis、RabbitMQ、Nacos、Sentinel Dashboard、Docker Desktop

## 初始化配置

```bash
cd /Users/coconut/Desktop/ShelfFlow
npm install
cp .env.example .env.local
```

按本地环境修改 `.env.local`：

```text
DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=shelfflow
DB_USER=root
DB_PASSWORD=你的数据库密码
SHELFFLOW_GATEWAY_BASE_URL=http://127.0.0.1:4010
AI_OPS_API_KEY=你的大模型 Key
SHELFFLOW_MAIL_USERNAME=你的邮箱账号
SHELFFLOW_MAIL_PASSWORD=你的邮箱 SMTP 授权码
```

`.env.local` 已被 `.gitignore` 忽略，不要提交真实密码、SMTP 授权码或大模型 Key。

## 数据库

创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS shelfflow
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

执行 Flyway 迁移：

```bash
cd /Users/coconut/Desktop/ShelfFlow/services
mvn -q -pl shelfflow-migration-service spring-boot:run
```

迁移脚本位于：

```text
services/shelfflow-migration-service/src/main/resources/db/migration/
```

演示数据位于：

```text
docs/seed/shelfflow-demo-data.sql
docs/seed/20260527_user_storefront_products.sql
docs/seed/20260527_fix_storefront_catalog_visibility.sql
docs/seed/20260527_fix_user_qa_password.sql
```

如果你已经从 Navicat 导出了完整数据库，可使用 `docs/seed/shelfflow.sql` 作为本地全量恢复文件。

## 启动后端

在 IDEA 中打开 `/Users/coconut/Desktop/ShelfFlow/services/pom.xml` 作为 Maven 工程。第一次启动业务服务前，先运行 `ShelfFlowMigrationServiceApplication` 或 Maven 迁移命令。

然后按顺序启动：

1. `ShelfFlowAuthServiceApplication`
2. `ShelfFlowAdminServiceApplication`
3. `ShelfFlowUserServiceApplication`
4. `ShelfFlowGatewayApplication`

默认端口：

```text
auth-service   http://127.0.0.1:4011
admin-service  http://127.0.0.1:4012
user-service   http://127.0.0.1:4013
gateway        http://127.0.0.1:4010
```

健康检查：

```bash
curl -fsS http://127.0.0.1:4010/health
```

## 可选开启 Nacos + Sentinel

默认本地启动不依赖 Nacos 和 Sentinel。需要演示 Spring Cloud Alibaba 能力时，先启动阿里巴巴组件：

```bash
cd /Users/coconut/Desktop/ShelfFlow
docker compose -f docker-compose.alibaba.yml up -d
```

访问地址：

```text
Nacos 控制台：http://127.0.0.1:8848/nacos
Sentinel 控制台：http://127.0.0.1:8858
```

给四个 Java 服务增加环境变量：

```text
SHELFFLOW_NACOS_ENABLED=true
SHELFFLOW_NACOS_CONFIG_ENABLED=true
SHELFFLOW_NACOS_SERVER_ADDR=127.0.0.1:8848
SHELFFLOW_SENTINEL_ENABLED=true
SHELFFLOW_SENTINEL_DASHBOARD=127.0.0.1:8858
SPRING_PROFILES_ACTIVE=nacos
```

其中 `SPRING_PROFILES_ACTIVE=nacos` 会让 Gateway 使用 `lb://shelfflow-auth-service`、`lb://shelfflow-admin-service`、`lb://shelfflow-user-service` 进行服务发现路由。

Nacos 配置中心样例位于：

```text
docs/nacos/
```

## 启动前端

启动管理端：

```bash
cd /Users/coconut/Desktop/ShelfFlow
bash scripts/start-admin-web.sh
```

启动用户端：

```bash
cd /Users/coconut/Desktop/ShelfFlow
bash scripts/start-user-web.sh
```

同时启动管理端和用户端：

```bash
cd /Users/coconut/Desktop/ShelfFlow
bash scripts/start-web.sh
```

访问地址：

```text
管理端：http://127.0.0.1:3000
用户端：http://127.0.0.1:3001
```

## 构建与验证

前端构建：

```bash
npm run build
```

Java 服务测试：

```bash
cd /Users/coconut/Desktop/ShelfFlow/services
mvn -q test
```

指定模块测试：

```bash
cd /Users/coconut/Desktop/ShelfFlow/services
mvn -q -pl shelfflow-user-service -am test
mvn -q -pl shelfflow-admin-service -am test
```

## 文档索引

- [架构说明](docs/ARCHITECTURE.md)
- [API 契约](docs/API_CONTRACT.md)
- [本地运行手册](docs/LOCAL_RUNBOOK.md)
- [验收清单](docs/ACCEPTANCE_CHECKLIST.md)
- [面试讲解](docs/INTERVIEW_GUIDE.md)

## 安全注意事项

- 不提交 `.env.local`、日志、构建产物、上传文件、IDE 配置和依赖目录。
- GitHub 推送前执行敏感信息扫描。
- 生产环境必须替换 `JWT_SECRET`、数据库密码、AI Key、SMTP 授权码。
- 上传文件建议在生产环境接入对象存储或独立静态资源服务。
