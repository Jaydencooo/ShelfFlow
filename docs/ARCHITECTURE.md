# ShelfFlow 架构说明

## 1. 项目目标

ShelfFlow 是一个围绕临期库存流转构建的双端业务系统，目标不是 demo，而是具备真实业务闭环、可维护性和可扩展性的 production-ready Java 后端项目。

当前第一阶段已落地两条主线：

- 管理端：商品管理、批次管理、动态定价、订单履约
- 用户端：商品目录、购物车、订单提交、支付、取消、订单详情

## 2. 总体架构

```mermaid
flowchart LR
    AdminWeb["管理端 Web (Next.js)"]
    UserWeb["用户端 Web (Next.js)"]
    Gateway["shelfflow-gateway"]
    Auth["shelfflow-auth-service"]
    Admin["shelfflow-admin-service"]
    User["shelfflow-user-service"]
    Legacy["shelfflow-backend (legacy)"]
    MySQL[("MySQL")]
    Redis[("Redis")]

    AdminWeb --> Gateway
    UserWeb --> Gateway

    Gateway --> Auth
    Gateway --> Admin
    Gateway --> User

    Auth --> Legacy
    Admin --> MySQL
    User --> MySQL
    Legacy --> MySQL
    Legacy --> Redis
```

## 3. 仓库结构

```text
ShelfFlow
├── apps
│   ├── shelfflow-admin-web
│   └── shelfflow-user-web
├── services
│   ├── shelfflow-gateway
│   ├── shelfflow-auth-service
│   ├── shelfflow-admin-service
│   ├── shelfflow-user-service
│   └── shelfflow-service-common
├── packages
│   ├── shelfflow-config
│   ├── shelfflow-contracts
│   └── shelfflow-shared
├── docs
├── scripts
└── shelfflow-backend
```

## 4. 服务职责

### 4.1 `shelfflow-gateway`

负责统一 API 入口：

- 暴露 `/api/**`
- 路由到 auth/admin/user 三个业务服务
- 统一跨域策略
- 统一请求入口

### 4.2 `shelfflow-auth-service`

负责管理员认证和会话语义：

- 管理员登录
- 当前管理员会话
- JWT 载荷输出
- 对 legacy 登录结果做统一错误映射

### 4.3 `shelfflow-admin-service`

负责管理端核心业务：

- 商品分页、创建、更新
- 批次分页、详情、创建、更新、状态流转
- 定价规则分页、创建、更新、启停
- 基于临期批次生成 AI 定价建议
- 损耗统计总览、分类聚合和处置建议
- AI 运营助手建议、知识库和本地规则问答
- 订单分页、详情、履约状态流转

当前服务层按以下分层组织：

```text
controller -> application service -> domain policy -> persistence mapper
```

### 4.4 `shelfflow-user-service`

负责用户端核心业务：

- 用户登录 / 会话
- 商品目录 / 商品详情
- 购物车
- 订单提交 / 支付 / 取消 / 详情 / 列表

同样采用：

```text
controller -> application service -> domain policy -> persistence mapper
```

### 4.5 `shelfflow-service-common`

负责沉淀横切能力：

- 统一响应结构
- 错误码
- 分页模型
- 状态枚举
- 管理员 / 用户鉴权上下文
- 全局异常处理
- MVC 拦截与参数转换

### 4.6 `shelfflow-backend`

当前仍保留为第一阶段运行依赖和迁移来源：

- 认证和部分历史数据结构来自 legacy
- 随着 admin/user 服务继续演进，legacy 依赖会逐步下降

## 5. 关键业务模型

### 5.1 商品

商品是目录与库存批次的上游实体，承担：

- 商品展示信息
- 分类归属
- 标价信息
- 商品可售状态

### 5.2 批次

批次是 ShelfFlow 的核心聚合，承担：

- 临期库存承载
- 到期时间
- 动态价格基础口径
- 可售库存、锁定库存、已售库存
- 批次状态流转

### 5.3 订单

订单连接用户端购买行为和管理端履约行为：

- 用户端提交订单时锁定库存
- 取消订单时释放锁定库存
- 履约完成时将锁定库存结转到已售库存
- 每次关键状态变化都会写入 `order_event_log` 审计轨迹

### 5.4 定价规则

定价规则负责把临期策略从页面操作沉淀为后端业务能力：

- 规则按剩余保质期区间命中批次
- `priority` 决定多条规则同时命中时的优先级
- `discountRate` 统一作为动态价计算口径
- 管理端支持规则启停，避免删除规则导致审计和复盘困难
- AI 定价建议从真实临期批次生成，采纳后写入同一套规则模型

### 5.5 损耗统计

损耗统计以批次为核心聚合：

- 总览统计总批次、临期批次、售罄批次、可售库存和预估损耗金额
- 分类统计按商品分类聚合临期、售罄、过期库存和损耗率
- 处置建议按剩余过期天数、可用库存和预估损耗金额排序
- 临期窗口和建议条数通过 `shelfflow.admin.loss-stats` 配置管理

### 5.6 AI 运营助手

AI 运营助手当前采用可配置 provider 的本地规则引擎：

- `provider=local` 时基于运营知识库和实时批次建议生成回答
- 知识库支持分页、搜索、新增、编辑和删除
- 实时建议复用批次库存、剩余效期、售罄和高库存规则
- `shelfflow.admin.ai-ops` 管理 provider、model、建议条数和检索条数
- 后续替换外部大模型时保持 `/api/admin/ai-ops/chat` 契约稳定

## 6. 关键状态流转

### 6.1 用户订单状态

```mermaid
stateDiagram-v2
    [*] --> pending_payment
    pending_payment --> cancelled
    pending_payment --> to_prepare: pay
    to_prepare --> cancelled
    to_prepare --> preparing
    preparing --> ready_for_pickup
    ready_for_pickup --> completed
```

### 6.2 批次状态

批次状态当前至少覆盖：

- `active`
- `paused`
- `expired`

批次分页查询会在读取链路中执行到期刷新，保证状态与时间窗口一致。

## 7. 库存一致性设计

第一阶段库存一致性重点落在订单提交、取消、完成三个动作：

1. 提交订单  
   - 校验购物车项可售性
   - 校验可用库存
   - 将 `locked_quantity` 增加
   - 写入订单和订单明细

2. 取消订单  
   - 仅允许特定状态取消
   - 将 `locked_quantity` 减少

3. 完成订单  
   - 由管理端履约完成
   - 将 `locked_quantity` 结转到 `sold_quantity`

这套设计的重点是把状态变化和库存变化保持在同一业务语义下，而不是散落在多个脚本或页面逻辑里。

## 8. 测试与验收

当前项目已具备三层验收：

### 8.1 Java 测试

- service unit test
- MVC integration test
- H2 数据库级 integration test

### 8.2 后端 smoke

- `scripts/backend-smoke.sh`
- `scripts/order-lifecycle-smoke.sh`
- `scripts/user-backend-smoke.sh`

### 8.3 前端 UI smoke

- `scripts/user-web-ui-smoke.sh`

## 9. 审计与可追踪性

订单链路当前已经补齐结构化审计日志：

- 表：`order_event_log`
- 记录范围：
  - 用户提交订单
  - 用户支付
  - 用户取消
  - 管理端履约状态推进

每条日志记录：

- `event_type`
- `actor_type`
- `actor_id`
- `from_status / to_status`
- `from_pay_status / to_pay_status`
- `note`
- `create_time`

这样做的价值不是“多一张表”，而是把订单生命周期从黑盒操作变成可回放的业务轨迹。  
在排查状态异常、库存争议和履约问题时，可以直接沿事件时间线回溯。

## 10. 当前架构取舍

### 已做出的取舍

- 前端统一 Next.js，而不是保留多个前端栈
- 后端固定 Java / Spring Boot / Spring Cloud Alibaba
- 不为了“看起来是微服务”而过早拆更多服务
- 用 `service-common` 承载共享能力，而不是在各服务复制实现

### 暂未继续拆分的模块

以下模块保留为后续阶段，而不是在第一阶段过度拆分：

- order 独立服务
- fulfillment 独立服务
- ai ops

这是有意为之：当前 pricing、loss stats、ai ops 已作为 admin-service 内的领域模块落地，order / fulfillment 会在主流程稳定后再评估是否独立服务化，避免为了“拆服务”牺牲一致性和交付质量。

## 11. 后续演进方向

建议的下一阶段方向：

1. 幂等保护
2. 并发库存保护加强
3. 缓存与降级策略
4. 更细粒度权限模型
5. 订单独立服务化评估
