# ShelfFlow 最终验收清单

本文档用于本地验收 ShelfFlow 的核心业务闭环。所有命令默认在项目根目录执行：

```bash
cd /Users/coconut/Desktop/ShelfFlow
```

## 1. 验收目标

ShelfFlow 当前验收重点是“社区自提型临期零售系统”的最小上线闭环：

- 管理端能维护分类、商品、图片、批次、定价、自提点和订单履约。
- 用户端能浏览真实可售商品、注册登录、加入购物车、提交订单、支付、查看订单。
- 管理端操作后，用户端按真实可售规则同步展示。
- 订单从用户下单到管理端备货、自提核销、库存结转形成闭环。
- AI 运营助手能进行问答、生成运营建议、记录建议执行历史。
- 操作日志能记录管理端关键写操作，便于排查和复盘。

## 2. 启动前检查

### 2.1 数据库

Navicat 中确认存在数据库：

```sql
SHOW DATABASES LIKE 'shelfflow';
```

必要 SQL 导入顺序：

```text
1. /Users/coconut/Desktop/ShelfFlow/shelfflow-backend/mysql.sql
2. /Users/coconut/Desktop/ShelfFlow/shelfflow-backend/docs/migrations/20260520_user_account_auth.sql
3. /Users/coconut/Desktop/ShelfFlow/shelfflow-backend/docs/migrations/20260512_order_fulfillment_columns.sql
4. /Users/coconut/Desktop/ShelfFlow/shelfflow-backend/docs/migrations/20260525_user_auth_enterprise_flow.sql
5. /Users/coconut/Desktop/ShelfFlow/shelfflow-backend/docs/migrations/20260525_pickup_points.sql
6. /Users/coconut/Desktop/ShelfFlow/shelfflow-backend/docs/migrations/20260526_pickup_contact_user_fields.sql
7. /Users/coconut/Desktop/ShelfFlow/shelfflow-backend/docs/migrations/20260522_order_event_log.sql
8. /Users/coconut/Desktop/ShelfFlow/shelfflow-backend/docs/migrations/20260524_ai_ops_chat_and_suggestions.sql
9. /Users/coconut/Desktop/ShelfFlow/shelfflow-backend/docs/migrations/20260525_admin_operation_ai_actions.sql
10. /Users/coconut/Desktop/ShelfFlow/docs/seed/shelfflow-demo-data.sql
```

如果只是补用户端商城商品，导入：

```text
/Users/coconut/Desktop/ShelfFlow/docs/seed/20260527_user_storefront_products.sql
```

### 2.2 后端服务

在 IDEA 中启动：

```text
ShelfFlowAuthServiceApplication
ShelfFlowAdminServiceApplication
ShelfFlowUserServiceApplication
ShelfFlowGatewayApplication
```

网关健康检查：

```bash
curl -fsS http://127.0.0.1:4010/health
```

### 2.3 前端服务

同时启动管理端和用户端：

```bash
bash scripts/start-web.sh
```

访问地址：

```text
管理端：http://127.0.0.1:3000
用户端：http://127.0.0.1:3001
```

## 3. 验收账号

用户端测试账号：

| 登录方式 | 账号 | 密码 |
| --- | --- | --- |
| 手机号 | `13800138111` | `Passw0rd!` |
| 邮箱 | `qa.user@shelfflow.local` | `Passw0rd!` |

管理端账号以本地管理员数据为准。

## 4. 管理端验收

### 4.1 系统概览

- 能看到批次、订单、经营分析、AI 建议、最近动态。
- 快速入口包含商品管理、批次管理、定价规则、订单履约、经营分析、AI 运营助手。
- 最近动态展示近 10 条管理端关键操作。

### 4.2 商品管理

- 能查看商品列表。
- 能新增商品并上传商品图片。
- 能编辑商品信息。
- 能停售商品。
- 商品起售显示绿色，停售显示红色。
- 能删除商品。
- 能新增、删除分类。
- 分类列表展示分类商品数量。
- 按分类筛选后，只展示对应商品。

### 4.3 批次管理

- 创建批次时能搜索商品。
- 批次号自动生成，不需要人工记忆编码。
- 能编辑批次库存、生产日期、到期日期、基础价格。
- 能删除批次。
- 商品停售后，对应批次不应继续作为用户端可售商品展示。
- 批次列表操作按钮对齐。

### 4.4 定价规则

- 能创建定价规则。
- 能编辑定价规则。
- 能启用、停用规则。
- 能删除规则。
- 启用状态显示绿色，停用状态显示红色。
- 用户端展示价格应受可用规则影响。

### 4.5 订单履约

- 能看到用户端提交的订单。
- 支付状态和订单状态清晰可见。
- 能流转订单：待备货 -> 备货中 -> 待自提。
- 待自提订单能输入自提码核销。
- 核销成功后订单变为已完成。
- 订单详情能看到商品、用户、自提点、状态轨迹。

### 4.6 自提点管理

- 能新增自提点。
- 能编辑自提点。
- 能启用、停用自提点。
- 用户端提交订单时能使用启用自提点。

### 4.7 经营分析

- 能看到营业额、应收、净利润、损耗估算等经营指标。
- 能看到分类损耗统计。
- 能看到处置建议。
- 执行建议前有确认弹窗，说明具体将要操作的数据。

### 4.8 AI 运营助手

- 智能问答能返回结果。
- 聊天记录能保存。
- 实时运营建议能展示。
- 执行建议后，建议状态变化。
- 能查看建议执行记录。

### 4.9 操作日志

- 能按模块筛选。
- 能按操作类型筛选。
- 能查看成功和失败记录。
- 能看到请求方法、路径、HTTP 状态码、操作者和时间。

## 5. 用户端验收

### 5.1 首页商城

- 未登录也能浏览首页商品。
- 首页和商品目录合并为商城首页。
- 能搜索商品。
- 能按分类筛选。
- 能按最新、价格、临期时间排序。
- 商品卡片展示图片、名称、分类、价格、库存、到期时间。
- 加入购物车后有成功反馈。

### 5.2 登录注册

- 手机号或邮箱可以登录。
- 注册包含手机号或邮箱、用户名、密码、确认密码、验证码。
- 同一个手机号只能注册一个用户。
- 登录后右上角展示用户入口。
- 未登录访问购物车、订单、我的页面时跳转登录。

### 5.3 我的页面

- 能维护自提信息。
- 默认使用登录账号的姓名、手机号或邮箱。
- 修改手机号或邮箱需要验证码。
- 修改密码需要确认当前登录密码。

### 5.4 购物车

- 能选择单个或多个购物车商品。
- 能调整数量。
- 能删除商品。
- 能选择自提点和联系人。
- 能统一结算所选商品。

### 5.5 订单

- 能提交订单。
- 能支付订单。
- 订单列表筛选不应导致页面无法切换其他状态。
- 订单详情展示状态、商品、金额、自提点、取货码。
- 管理端核销后，用户端订单状态同步为已完成。

## 6. 管理端与用户端打通规则

用户端只展示真正可售的商品。一个管理端商品要在用户端出现，必须同时满足：

```text
商品状态 = 起售
分类状态 = 启用
存在至少一个启用批次
批次未过期
批次可售库存 > 0
```

因此，“管理端起售商品但用户端不显示”时，优先检查：

```sql
SELECT
    p.id,
    p.name,
    p.status AS product_status,
    c.status AS category_status,
    b.id AS batch_id,
    b.status AS batch_status,
    b.expiration_time,
    b.stock_quantity,
    b.locked_quantity,
    b.sold_quantity
FROM product p
LEFT JOIN category c ON c.id = p.category_id
LEFT JOIN inventory_batch b ON b.product_id = p.id
WHERE p.id = 你的商品ID;
```

## 7. 最小闭环验收路径

按这个顺序验收，最容易定位问题：

1. 管理端新增分类。
2. 管理端新增商品，选择分类并上传图片。
3. 管理端创建该商品的可售批次。
4. 用户端首页搜索该商品。
5. 用户端加入购物车。
6. 用户端提交订单。
7. 用户端支付订单。
8. 管理端订单履约流转到待自提。
9. 管理端输入用户订单详情里的自提码完成核销。
10. 用户端订单详情确认状态为已完成。
11. 管理端操作日志确认记录了商品、批次、订单相关操作。

## 8. 验收失败优先排查

| 现象 | 优先检查 |
| --- | --- |
| 前端 Internal Server Error | 网关 `http://127.0.0.1:4010/health` 是否正常 |
| 用户端登录 401 | 测试账号 SQL 是否导入、旧 cookie 是否清理 |
| 用户端商品数量少 | 商品是否有启用且未过期的可售批次 |
| 分类显示少 | 分类下是否存在真实可售商品 |
| 加购失败 | 批次库存是否大于 0，商品和批次是否启用 |
| 提交订单失败 | 是否选择自提联系人，自提点是否启用 |
| 管理端 AI 无建议 | 是否配置 `AI_OPS_API_KEY`，数据库是否有批次和库存数据 |
| 操作日志为空 | 是否执行过 POST、PUT、DELETE 类管理端写操作 |
