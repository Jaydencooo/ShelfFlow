# ShelfFlow 后端接口清单

## 库存批次

- `POST /admin/inventory-batch` 新增库存批次
- `PUT /admin/inventory-batch` 修改库存批次
- `GET /admin/inventory-batch/page` 分页查询库存批次
- `GET /admin/inventory-batch/{id}` 查询批次详情
- `GET /admin/inventory-batch/product/{productId}` 查询商品下的批次
- `POST /admin/inventory-batch/status/{status}?id=` 启用或停用批次
- `POST /admin/inventory-batch/refresh-status` 刷新过期、售罄、恢复可售状态

## 动态定价

- `POST /admin/pricing-rule` 新增定价规则
- `PUT /admin/pricing-rule` 修改定价规则
- `GET /admin/pricing-rule/page` 分页查询定价规则
- `GET /admin/pricing-rule/{id}` 查询规则详情
- `POST /admin/pricing-rule/status/{status}?id=` 启用或停用规则
- `GET /admin/pricing-rule/calculate?productId=&batchId=` 计算批次动态价格

## 订单履约

- `GET /admin/order/conditionSearch` 订单搜索
- `GET /admin/order/statistics` 订单状态统计
- `GET /admin/order/details/{id}` 订单详情
- `PUT /admin/order/confirm` 接单进入备货
- `PUT /admin/order/ready-for-pickup/{id}` 备货完成并进入待自提
- `PUT /admin/order/verify` 按订单和自提码核销
- `PUT /admin/order/complete/{id}` 直接完成核销
- `PUT /admin/order/rejection` 拒单并释放锁定库存
- `PUT /admin/order/cancel` 取消订单并释放锁定库存

## 履约任务

- `GET /admin/fulfillment-task/page` 履约任务分页查询
- `GET /admin/fulfillment-task/order/{orderId}` 按订单查询履约任务
- `GET /admin/fulfillment-task/statistics` 履约任务状态统计（待备货/待自提/已完成/已取消/总数）
- `PUT /admin/fulfillment-task/ready-for-pickup/{orderId}` 标记为待自提
- `PUT /admin/fulfillment-task/verify` 按订单和核销码完成核销
- `PUT /admin/fulfillment-task/complete/{orderId}` 直接完成核销
- `PUT /admin/fulfillment-task/cancel` 取消任务并取消订单（body: `id`,`cancelReason`）

## 损耗统计

- `GET /admin/loss-stats/overview` 返回过期批次数、过期库存、三天内临期批次数、三天内临期库存、售罄批次数

## AI 运营助手

- `GET /admin/ai-ops/suggestions` 返回基于批次库存、效期和规则的运营建议

## 商品浏览

- `GET /user/product/list?categoryId=` 返回可售商品，同时带出推荐批次、可售库存、最近过期时间、临期天数和动态价
- `GET /user/category/list?type=` 查询分类
- `POST /user/cartItem/add` 加入选品车，支持 `batchId`
- `GET /user/cartItem/list` 查询选品车
- `POST /user/order/submit` 提交订单并锁定批次库存

## 数据脚本

- 全量初始化脚本：`mysql.sql`
- 旧库字段迁移脚本：`docs/migrations/20260512_order_fulfillment_columns.sql`
