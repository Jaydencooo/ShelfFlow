# ShelfFlow 改造日志

## 2026-04-28 项目身份改造

- 将 Maven 多模块从旧命名统一改为 `shelfflow`、`shelfflow-common`、`shelfflow-pojo`、`shelfflow-server`。
- 将 Java 包名统一为 `com.shelfflow`，启动类改为 `ShelfFlowApplication`。
- 将配置前缀、数据库名、缓存键和业务文案统一到 ShelfFlow。
- 将商品、组合包、选品车、自提联系人、运营人员等领域对象调整为 ShelfFlow 语义。
- 清理 README、SQL 示例数据和注释中的旧项目来源说明与旧业务示例。

## 当前后端能力

- 管理端运营人员、分类、商品、规格、组合包管理。
- 用户端登录、浏览、选品车、自提联系人、下单、支付确认和订单查询。
- 管理端订单搜索、状态统计、接单、拒单、取消、备货、待自提和核销完成。
- 工作台指标、流转额统计、用户统计、订单统计、流转 Top10 和 Excel 导出。
- Redis 缓存、JWT 鉴权、WebSocket 提醒、Spring Task 超时订单处理。

## 后续建议

- 继续把管理端前端接入 `/admin/**` 接口，形成完整可操作后台。
- 根据真实 ShelfFlow 业务补充批次、定价规则、损耗统计和 AI 运营助手接口。
- 将 `mysql.sql` 示例数据替换为生产前可复用的初始化数据和最小演示数据集。
