# ShelfFlow

ShelfFlow 是一个面向临期零售和门店库存周转的 Spring Boot 后端项目。系统覆盖管理端与用户端接口，支持商品批次、分类、组合包、订单履约、运营报表、缓存、身份认证和实时订单提醒等业务能力。

## 技术栈

- Java 17
- Spring Boot 2.7.3
- Spring MVC
- MyBatis
- MySQL
- Redis
- JWT
- WebSocket
- Apache POI
- Lombok

## 项目结构

```text
shelfflow
├── shelfflow-common    # 通用常量、上下文、异常、工具、配置属性
├── shelfflow-pojo      # DTO、Entity、VO
└── shelfflow-server    # Controller、Service、Mapper、拦截器、任务调度
```

## 核心能力

- 管理端运营人员登录、鉴权、分页查询、启停和资料维护。
- 分类、商品、规格、组合包的全流程管理。
- 商品上架、停售、组合包联动校验和 Redis 缓存刷新。
- 库存批次管理，记录生产时间、过期时间、总库存、锁定库存和已售库存。
- 动态定价规则，按临期天数自动计算批次成交价。
- 用户端登录、商品浏览、选品车、自提联系人管理、下单、支付确认和历史订单。
- 管理端订单监控、接单、拒单、取消、备货、待自提、核销完成以及状态统计。
- WebSocket 来单提醒和用户催单提醒。
- 定时处理超时未支付订单，减少人工巡检成本。
- 工作台、流转额、订单、用户和流转 Top10 报表。
- 基于 Excel 模板导出运营日报。

## 快速启动

1. 安装 JDK 17、Maven、MySQL 和 Redis。
2. 在 MySQL 中执行 `mysql.sql` 初始化 `shelfflow` 数据库。
3. 复制 `shelfflow-server/src/main/resources/application-dev.yml.template` 为 `application-dev.yml`，填写数据库、Redis、JWT、对象存储和微信小程序配置；本地端口统一由 `scripts/shelfflow-env.sh` 或环境变量控制。
4. 编译项目：

```bash
mvn -DskipTests compile
```

5. 启动服务：

```bash
./scripts/dev-backend.sh
```

默认接口基路径包括：

- 管理端：`/admin/**`
- 库存批次：`/admin/inventory-batch/**`
- 动态定价：`/admin/pricing-rule/**`
- 用户端：`/user/**`
- 支付通知：`/notify/**`

## 默认账号

初始化 SQL 中包含一个管理端账号：

- 用户名：`admin`
- 密码：`123456`

## 业务说明

ShelfFlow 的业务重点是降低临期商品损耗并提升货架周转效率。后台可以维护商品、组合包和分类，用户端可以浏览商品并下单。订单进入履约流程后，运营人员可以在后台完成接单、分拣、备货、自提核销、取消和完成等动作，报表模块用于追踪库存流转、用户、订单和商品动销情况。

## 接口文档

管理端和用户端核心接口见 `docs/admin-api.md`。
