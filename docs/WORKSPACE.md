# ShelfFlow 工作区分工

## 正式目录

- `apps/shelfflow-admin-web`：管理端 Web，第一阶段主交付。
- `apps/shelfflow-user-web`：用户端 Web，负责真实商品目录、购物车、订单与会话页面。
- `services`：Java/Spring Cloud Alibaba 服务层，包含 gateway、auth、admin、user 四个可独立部署服务，以及内部 Maven common 模块。
- `packages`：共享 contracts、config、shared 工具包。
- `shelfflow-backend`：legacy Java backend，作为迁移来源和第一阶段运行依赖。
- `docs`：项目文档、接口约定、联调手册、分工说明。
- `scripts`：本地启动、冒烟测试、运维辅助脚本。

## 模块分工建议

- 管理端前端：登录、批次管理、商品/批次创建、状态流转与内部 API 代理。
- 认证服务：管理员登录、会话读取、权限载荷。
- 管理服务：商品、库存批次、订单履约状态流转和 legacy backend 适配。
- 用户服务：商品目录、用户会话、购物车、订单提交/支付/取消/详情。
- 网关：统一入口、跨域、请求 ID、服务转发。
- 共享层：契约 schema、配置校验、错误与响应模型。
- Legacy Backend：保留 MySQL/Redis/业务实现能力，作为微服务迁移来源。

## 清理原则

- 不再保留旧的 `admin-frontend` 和 miniapp 路线。
- 不再保留 `services-ts-deprecated` 这类废弃服务树。
- 不在根目录新增散落业务代码。
- 构建产物 `dist/`、`target/`、日志、依赖缓存不作为交付内容。
- 新接口优先沉淀到 `packages/shelfflow-contracts`，并同步更新 `docs` 中的联调文档。
