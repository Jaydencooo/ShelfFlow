# ShelfFlow User Web

用户端 Web 已进入主仓第一阶段开发，当前已接通以下真实链路：

- 用户注册
- 用户登录
- 找回密码
- 商品目录（搜索、类目筛选、分页）
- 商品详情（数量选择、加入购物车）
- 购物车（数量调整、删除、清空）
- 订单列表（状态筛选、分页）
- 订单详情（订单轨迹、取消原因）
- 订单支付 / 取消
- 账户中心（登录态、资料编辑、购物车概览、最近订单、快捷入口）
- 自提联系人管理（新增、编辑、删除、默认联系人）

另外已补一条浏览器级 UI smoke，用于回归以下页面链路：

- 未登录访问账户中心自动跳转登录
- 注册
- 找回密码
- 新密码登录
- 账户中心加载当前用户
- 用户资料编辑
- 自提联系人维护
- 商品详情
- 加购
- 购物车选择自提联系人
- 购物车数量调整
- 提交订单确认弹窗
- 提交订单
- 订单支付
- 订单列表查看
- 账户中心最近订单回显

## Local Development

```bash
cd /Users/coconut/Desktop/ShelfFlow
npm run dev:user-web
```

或：

```bash
cd /Users/coconut/Desktop/ShelfFlow
bash scripts/dev-user-web.sh
```

默认依赖：

- `SHELFFLOW_GATEWAY_BASE_URL=http://127.0.0.1:4010`

准备用户端演示账号：

```bash
cd /Users/coconut/Desktop/ShelfFlow
bash scripts/seed-user-demo-data.sh
```

默认 smoke 账号配置：

- `SHELFFLOW_USER_SMOKE_OPEN_ID`
- `SHELFFLOW_USER_SMOKE_PHONE`
- `SHELFFLOW_USER_SMOKE_PASSWORD`

建议先启动后端整链：

```bash
KEEP_SERVICES_RUNNING=true START_USER_WEB=true bash scripts/admin-e2e.sh
```

如果只想长期保留本地服务做页面联调，也可以使用：

```bash
START_USER_WEB=true bash scripts/start-local-all.sh
```

浏览器级 UI smoke：

```bash
cd /Users/coconut/Desktop/ShelfFlow
bash scripts/user-web-ui-smoke.sh
```

如果当前没有可售商品，建议先运行：

```bash
cd /Users/coconut/Desktop/ShelfFlow
KEEP_SERVICES_RUNNING=true START_USER_WEB=true bash scripts/admin-e2e.sh
```

这会创建并校验跨端订单闭环，同时保留本地服务环境供 UI smoke 继续使用。
