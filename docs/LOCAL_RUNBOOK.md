# ShelfFlow 本地运行手册

本文档用于本地开发、验收和排查启动问题。所有命令默认在项目根目录执行：

```bash
cd /Users/coconut/Desktop/ShelfFlow
```

## 1. 项目结构

```text
ShelfFlow
├── apps
│   ├── shelfflow-admin-web      # 管理端 Next.js
│   └── shelfflow-user-web       # 用户端 Next.js
├── services
│   ├── shelfflow-auth-service   # 登录认证服务
│   ├── shelfflow-admin-service  # 管理端业务服务
│   ├── shelfflow-user-service   # 用户端业务服务
│   ├── shelfflow-gateway        # API 网关
│   └── shelfflow-service-common # Java 公共模块
├── docs
│   ├── seed                     # 测试数据 SQL
│   └── LOCAL_RUNBOOK.md         # 本地运行手册
├── scripts
│   ├── start-admin-web.sh       # 启动管理端前端
│   ├── start-user-web.sh        # 启动用户端前端
│   ├── start-web.sh             # 同时启动管理端和用户端前端
│   └── lib                      # 启动脚本公共函数
└── .env.local                   # 本地环境变量
```

## 2. 设计思路

ShelfFlow 按“社区自提型临期零售系统”设计，本地运行时保持前后端分离：

- 管理端负责商品、分类、批次、定价、订单履约、经营分析、AI 运营助手。
- 用户端负责浏览商品、注册登录、购物车、提交订单、支付、订单跟踪、自提信息维护。
- 前端统一通过 `shelfflow-gateway` 访问 Java 服务，不直接访问数据库。
- 数据库由 MySQL 管理，推荐用 Navicat 维护和查看。

## 3. 核心模块职责

| 模块 | 端口 | 职责 |
| --- | --- | --- |
| `shelfflow-gateway` | `4010` | 统一 API 入口，路由到 auth/admin/user 服务 |
| `shelfflow-auth-service` | `4011` | 管理端认证 |
| `shelfflow-admin-service` | `4012` | 管理端商品、批次、订单、经营分析、AI 运营 |
| `shelfflow-user-service` | `4013` | 用户端账号、目录、购物车、订单、自提信息 |
| `shelfflow-admin-web` | `3000` | 管理端页面 |
| `shelfflow-user-web` | `3001` | 用户端页面 |

## 4. 初始化数据库

如果你已经创建过 `shelfflow` 数据库，不需要每次删除。只有当表结构很乱、迁移多次失败、测试数据污染严重时，才建议备份后重建。

推荐顺序：

1. 在 Navicat 创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS shelfflow DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 导入基础表结构：

```text
/Users/coconut/Desktop/ShelfFlow/shelfflow-backend/mysql.sql
```

3. 导入迁移 SQL。

推荐迁移顺序：

```text
/Users/coconut/Desktop/ShelfFlow/shelfflow-backend/docs/migrations/20260520_user_account_auth.sql
/Users/coconut/Desktop/ShelfFlow/shelfflow-backend/docs/migrations/20260512_order_fulfillment_columns.sql
/Users/coconut/Desktop/ShelfFlow/shelfflow-backend/docs/migrations/20260525_user_auth_enterprise_flow.sql
/Users/coconut/Desktop/ShelfFlow/shelfflow-backend/docs/migrations/20260525_pickup_points.sql
/Users/coconut/Desktop/ShelfFlow/shelfflow-backend/docs/migrations/20260526_pickup_contact_user_fields.sql
/Users/coconut/Desktop/ShelfFlow/shelfflow-backend/docs/migrations/20260522_order_event_log.sql
/Users/coconut/Desktop/ShelfFlow/shelfflow-backend/docs/migrations/20260524_ai_ops_chat_and_suggestions.sql
/Users/coconut/Desktop/ShelfFlow/shelfflow-backend/docs/migrations/20260525_admin_operation_ai_actions.sql
```

如果某个迁移提示字段或表已经存在，说明你的库已经应用过这段迁移。不要反复重建库，先确认 Navicat 中对应表结构是否已经存在。

4. 导入测试数据：

```text
/Users/coconut/Desktop/ShelfFlow/docs/seed/shelfflow-demo-data.sql
```

测试数据会写入演示分类、商品、可售批次、定价规则、自提点、用户端测试账号和默认自提联系人。

如果只想补用户端商城可售商品数据，可以导入：

```text
/Users/coconut/Desktop/ShelfFlow/docs/seed/20260527_user_storefront_products.sql
```

如果只想修复用户端测试账号密码，可以导入：

```text
/Users/coconut/Desktop/ShelfFlow/docs/seed/20260527_fix_user_qa_password.sql
```

## 5. 本地环境变量

本地配置文件在：

```text
/Users/coconut/Desktop/ShelfFlow/.env.local
```

最少需要确认这些配置：

```bash
SHELFFLOW_GATEWAY_BASE_URL=http://127.0.0.1:4010
SHELFFLOW_ADMIN_PORT=3000
SHELFFLOW_USER_WEB_PORT=3001

AI_OPS_PROVIDER=dashscope
AI_OPS_MODEL=qwen-plus
AI_OPS_API_KEY=你的阿里云百炼 API Key
AI_OPS_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode
AI_OPS_CHAT_COMPLETIONS_PATH=/v1/chat/completions
```

注意：`.env.local` 是隐藏文件，Finder 默认看不到。可以在项目根目录按 `Command + Shift + .` 显示隐藏文件，或在 IDEA 里直接打开项目根目录查看。

## 6. 启动后端

后端建议在 IDEA 里启动以下 Spring Boot Application：

1. `ShelfFlowAuthServiceApplication`
2. `ShelfFlowAdminServiceApplication`
3. `ShelfFlowUserServiceApplication`
4. `ShelfFlowGatewayApplication`

启动后检查网关：

```bash
curl -fsS http://127.0.0.1:4010/health
```

返回正常后再启动前端。

后端服务端口必须保持：

| 服务 | 端口 |
| --- | --- |
| `shelfflow-auth-service` | `4011` |
| `shelfflow-admin-service` | `4012` |
| `shelfflow-user-service` | `4013` |
| `shelfflow-gateway` | `4010` |

## 7. 启动管理端

```bash
bash scripts/start-admin-web.sh
```

访问：

```text
http://127.0.0.1:3000
```

如果提示 `next: command not found`，说明依赖没有安装或 `node_modules` 不完整，执行：

```bash
npm install
```

## 8. 启动用户端

```bash
bash scripts/start-user-web.sh
```

访问：

```text
http://127.0.0.1:3001
```

## 9. 一键启动管理端和用户端

后端和网关已经在 IDEA 启动后，可以用一个终端同时启动两个前端：

```bash
cd /Users/coconut/Desktop/ShelfFlow
bash scripts/start-web.sh
```

访问地址：

```text
管理端：http://127.0.0.1:3000
用户端：http://127.0.0.1:3001
网关：  http://127.0.0.1:4010
```

## 10. 测试账号

测试数据 SQL 中包含用户端验收账号：

| 类型 | 账号 | 密码 |
| --- | --- | --- |
| 手机号登录 | `13800138111` | `Passw0rd!` |
| 邮箱登录 | `qa.user@shelfflow.local` | `Passw0rd!` |

管理端账号以你当前数据库中的管理员数据为准。

## 11. 常见问题

### 11.1 管理端或用户端打开 Internal Server Error

优先检查：

```bash
curl -fsS http://127.0.0.1:4010/health
```

如果网关未启动，前端 API 代理会失败。

### 11.2 登录 400 Bad Request 或 401 Unauthorized

常见原因：

- 账号不存在，先导入 `docs/seed/shelfflow-demo-data.sql`。
- 密码错误，测试账号密码是 `Passw0rd!`。
- 后端连接的不是你导入数据的那个 MySQL 数据库。
- 浏览器里有旧 cookie，可以退出登录，或清理 `127.0.0.1:3001` / `localhost:3001` 的站点数据后重试。

### 11.3 购物车或订单跳登录

这是正常保护逻辑。用户端允许未登录浏览首页和商品目录，购物车、订单、我的页面都要求登录。

### 11.4 用户端看不到管理端起售商品

用户端只展示“商品起售 + 分类启用 + 存在可售批次 + 批次未过期 + 可售库存大于 0”的商品。管理端新建商品后，还需要创建并启用对应批次。

### 11.5 运营建议没有内容

先确认：

- `.env.local` 已配置 `AI_OPS_API_KEY`。
- 管理端服务启动时读取到了 `.env.local`。
- 数据库中存在可分析的商品、批次、库存和订单数据。

### 11.6 端口被占用

`start-admin-web.sh` 和 `start-user-web.sh` 会自动释放对应前端端口。如果后端端口被占用，需要在 IDEA 停止旧进程后重新启动。
