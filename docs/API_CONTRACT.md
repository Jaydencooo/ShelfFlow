# ShelfFlow API Contract

第一阶段外部接口以 Java gateway 暴露的 `/api/**` 契约为准。`shelfflow-backend` 仅作为 legacy 适配来源，不对前端暴露。

## 管理端

- `POST /api/admin/auth/login` 管理员登录
- `GET /api/admin/auth/me` 当前管理员会话
- `GET /api/admin/products` 商品分页
- `POST /api/admin/products` 新增商品
- `PUT /api/admin/products/{id}` 修改商品
- `GET /api/admin/inventory-batches` 批次分页
- `GET /api/admin/inventory-batches/{id}` 批次详情
- `POST /api/admin/inventory-batches` 新增库存批次
- `PUT /api/admin/inventory-batches/{id}` 修改库存批次
- `POST /api/admin/inventory-batches/{id}/status` 批次状态流转
- `GET /api/admin/orders` 管理端订单分页
- `GET /api/admin/orders/{id}` 管理端订单详情
- `POST /api/admin/orders/{id}/status` 管理端订单履约状态流转
- `GET /api/admin/pricing-rules` 定价规则分页
- `GET /api/admin/pricing-rules/{id}` 定价规则详情
- `POST /api/admin/pricing-rules` 新增定价规则
- `PUT /api/admin/pricing-rules/{id}` 修改定价规则
- `POST /api/admin/pricing-rules/{id}/status` 启用 / 禁用定价规则
- `GET /api/admin/pricing-rules/suggestions` 查询 AI 定价建议
- `POST /api/admin/pricing-rules/suggestions/{batchId}/accept` 采纳 AI 定价建议并生成规则
- `GET /api/admin/loss-stats/overview` 损耗统计总览、分类聚合和处置建议
- `GET /api/admin/ai-ops/suggestions` AI 运营建议
- `GET /api/admin/ai-ops/knowledge` 知识库分页
- `POST /api/admin/ai-ops/knowledge` 新增知识条目
- `PUT /api/admin/ai-ops/knowledge/{id}` 更新知识条目
- `DELETE /api/admin/ai-ops/knowledge/{id}` 删除知识条目
- `POST /api/admin/ai-ops/chat` AI 运营问答

管理端批次查询固定支持：

- `keyword`
- `categoryId`
- `batchStatus`
- `pricingStatus`
- `page`
- `pageSize`

核心字段固定为：

- `productId`
- `productName`
- `categoryId`
- `batchCode`
- `productionDate`
- `expiryDate`
- `shelfLifeDays`
- `basePrice`
- `currentPrice`
- `availableStock`
- `lockedStock`
- `soldStock`
- `wasteStock`
- `batchStatus`
- `pricingStatus`

管理端定价规则查询固定支持：

- `keyword`
- `status`
- `page`
- `pageSize`
- `sortBy`: `updatedAt` / `createdAt` / `priority` / `discountRate`
- `sortOrder`: `asc` / `desc`

定价规则核心字段固定为：

- `id`
- `name`
- `minDaysToExpire`
- `maxDaysToExpire`
- `discountRate`
- `priority`
- `status`: `enabled` / `disabled`
- `createTime`
- `updateTime`

AI 定价建议核心字段固定为：

- `batchId`
- `batchCode`
- `productId`
- `productName`
- `daysToExpire`
- `availableStock`
- `currentPrice`
- `suggestedDiscountRate`
- `suggestedPrice`
- `confidence`
- `reason`

损耗统计总览核心字段固定为：

- `totalBatchCount`
- `expiredBatchCount`
- `expiredStockQuantity`
- `expiringSoonBatchCount`
- `expiringSoonStockQuantity`
- `soldOutBatchCount`
- `saleableStockQuantity`
- `estimatedLossAmount`
- `lossRate`
- `categoryStats`
- `suggestions`

AI 运营助手核心字段固定为：

- `suggestions`: `type` / `priority` / `title` / `content` / `suggestedAction`
- `knowledge`: `id` / `title` / `category` / `content` / `createTime` / `updateTime`
- `chat`: `provider` / `model` / `answer` / `references`

## 后续阶段接口

以下接口已从第一阶段主流程移出，待领域模型稳定后再接入：

- `GET /admin/fulfillment-task/page` 履约任务分页
- `GET /admin/fulfillment-task/order/{orderId}` 查询订单履约任务
- `GET /admin/fulfillment-task/statistics` 履约任务状态统计
- `PUT /admin/fulfillment-task/ready-for-pickup/{orderId}` 履约任务转待自提
- `PUT /admin/fulfillment-task/verify` 履约任务按核销码完成
- `PUT /admin/fulfillment-task/complete/{orderId}` 履约任务直接完成
- `PUT /admin/fulfillment-task/cancel` 取消履约任务并取消订单

## 用户端

- `GET /api/user/catalog/categories` 查询启用商品分类
- `GET /api/user/catalog/products` 查询可售商品和推荐批次
- `GET /api/user/catalog/products/{id}` 查询商品详情
- `POST /api/user/auth/login` 用户登录
- `GET /api/user/auth/me` 当前用户会话
- `PUT /api/user/auth/me` 更新当前用户资料
- `GET /api/user/pickup-contacts` 查询自提联系人
- `POST /api/user/pickup-contacts` 新增自提联系人
- `PUT /api/user/pickup-contacts/{id}` 更新自提联系人
- `PATCH /api/user/pickup-contacts/{id}/default` 设置默认自提联系人
- `DELETE /api/user/pickup-contacts/{id}` 删除自提联系人
- `GET /api/user/cart/items` 查询购物车
- `POST /api/user/cart/items` 加入购物车
- `DELETE /api/user/cart/items/{id}` 删除购物车项
- `DELETE /api/user/cart/items` 清空购物车
- `POST /api/user/orders` 提交订单并锁定批次库存，可选传入 `pickupContactId`
- `GET /api/user/orders` 历史订单
- `GET /api/user/orders/{id}` 订单详情
- `DELETE /api/user/orders/{id}` 取消订单并释放库存，可选传入 `cancelReason`
- `POST /api/user/orders/{id}/pay` 支付确认

## 响应结构

项目使用统一响应：

```json
{
  "code": "ok",
  "message": "查询成功",
  "data": {},
  "requestId": "request-id",
  "timestamp": "2026-05-12T22:00:00+08:00"
}
```

统一错误码：

- `validation_error`
- `unauthorized`
- `forbidden`
- `not_found`
- `conflict`
- `rate_limited`
- `internal_error`
- `dependency_error`

## 数据脚本与迁移

- 全量初始化：`shelfflow-backend/mysql.sql`
- 增量迁移：`shelfflow-backend/docs/migrations/*.sql`
- 迁移记录表：`schema_migration`
- 初始化入口：`scripts/init-db.sh`
- 本地基础设施入口：`scripts/start-infra.sh`
- 本地基础设施停止入口：`scripts/stop-infra.sh`
