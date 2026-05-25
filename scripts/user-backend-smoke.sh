#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "${ROOT_DIR}/scripts/shelfflow-env.sh"

BASE_URL="${1:-${BASE_URL:-${SHELFFLOW_GATEWAY_BASE_URL}}}"
CURL_MAX_TIME="${CURL_MAX_TIME:-10}"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT

step_no=0
ACCESS_TOKEN=""
OPEN_ID="sf-user-$(date +%s)"
PASSWORD="${SHELFFLOW_USER_SMOKE_PASSWORD:-ShelfFlow#2026}"
PRODUCT_ID=""
CART_ITEM_ID=""
ORDER_ID=""

next_step() {
  step_no=$((step_no + 1))
  printf '\n[%s] %s\n' "${step_no}" "$1"
}

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

request() {
  local method="$1"
  local path="$2"
  local body="${3:-}"
  local output="${TMP_DIR}/response-${step_no}.json"
  local status
  local args=(-sS --max-time "${CURL_MAX_TIME}" -o "${output}" -w "%{http_code}" -X "${method}")

  if [[ -n "${ACCESS_TOKEN}" ]]; then
    args+=(-H "Authorization: Bearer ${ACCESS_TOKEN}")
  fi
  if [[ -n "${body}" ]]; then
    args+=(-H "Content-Type: application/json" -d "${body}")
  fi

  status="$(curl "${args[@]}" "${BASE_URL}${path}")" || fail "请求失败: ${method} ${path}"
  if [[ ! "${status}" =~ ^2[0-9][0-9]$ ]]; then
    echo "HTTP ${status}: ${method} ${path}" >&2
    cat "${output}" >&2
    exit 1
  fi

  cat "${output}"
}

assert_code_ok() {
  local name="$1"
  local response="$2"

  if [[ ! "${response}" =~ \"code\"[[:space:]]*:[[:space:]]*\"ok\" ]]; then
    echo "${name} 响应未通过统一契约校验:" >&2
    echo "${response}" >&2
    exit 1
  fi

  echo "OK: ${name}"
}

next_step "网关健康检查: GET /health"
GATEWAY_STATUS="$(curl -sS --max-time "${CURL_MAX_TIME}" -o /dev/null -w "%{http_code}" "${BASE_URL}/health" || true)"
if [[ ! "${GATEWAY_STATUS}" =~ ^2[0-9][0-9]$ ]]; then
  fail "网关未就绪，HTTP ${GATEWAY_STATUS}，请确认 ${BASE_URL} 已启动"
fi
echo "OK: 网关已响应 ${BASE_URL}/health"

next_step "用户目录分类: GET /api/user/catalog/categories"
CATEGORY_RES="$(request GET "/api/user/catalog/categories")"
assert_code_ok "用户目录分类" "${CATEGORY_RES}"

next_step "用户商品分页: GET /api/user/catalog/products"
PRODUCT_PAGE_RES="$(request GET "/api/user/catalog/products?page=1&pageSize=5&sortBy=updatedAt&sortOrder=desc")"
assert_code_ok "用户商品分页" "${PRODUCT_PAGE_RES}"
PRODUCT_ID="$(echo "${PRODUCT_PAGE_RES}" | sed -n 's/.*"items"[[:space:]]*:[[:space:]]*\[{"id"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
if [[ -z "${PRODUCT_ID}" ]]; then
  echo "${PRODUCT_PAGE_RES}" >&2
  fail "未能从商品分页中解析商品 ID"
fi
echo "OK: 商品 ID = ${PRODUCT_ID}"

next_step "用户商品详情: GET /api/user/catalog/products/{id}"
PRODUCT_DETAIL_RES="$(request GET "/api/user/catalog/products/${PRODUCT_ID}")"
assert_code_ok "用户商品详情" "${PRODUCT_DETAIL_RES}"

next_step "用户注册: POST /api/user/auth/register"
REGISTER_RES="$(request POST "/api/user/auth/register" "{\"openId\":\"${OPEN_ID}\",\"name\":\"Smoke User\",\"phone\":\"13900000000\",\"password\":\"${PASSWORD}\"}")"
assert_code_ok "用户注册" "${REGISTER_RES}"

next_step "用户登录: POST /api/user/auth/login"
LOGIN_RES="$(request POST "/api/user/auth/login" "{\"openId\":\"${OPEN_ID}\",\"password\":\"${PASSWORD}\"}")"
assert_code_ok "用户登录" "${LOGIN_RES}"
ACCESS_TOKEN="$(echo "${LOGIN_RES}" | sed -n 's/.*"token"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
if [[ -z "${ACCESS_TOKEN}" ]]; then
  echo "${LOGIN_RES}" >&2
  fail "未能从登录响应中解析 token"
fi
echo "OK: 已取得用户访问令牌"

next_step "当前用户会话: GET /api/user/auth/me"
ME_RES="$(request GET "/api/user/auth/me")"
assert_code_ok "当前用户会话" "${ME_RES}"

next_step "更新用户资料: PUT /api/user/auth/me"
UPDATE_PROFILE_RES="$(request PUT "/api/user/auth/me" "{\"name\":\"Smoke Updated\",\"phone\":\"13900000001\"}")"
assert_code_ok "更新用户资料" "${UPDATE_PROFILE_RES}"
if ! printf '%s' "${UPDATE_PROFILE_RES}" | grep -q '"phone":"13900000001"'; then
  echo "${UPDATE_PROFILE_RES}" >&2
  fail "用户资料更新后手机号未生效"
fi
echo "OK: 用户资料已更新"

next_step "创建自提联系人: POST /api/user/pickup-contacts"
CREATE_CONTACT_RES="$(request POST "/api/user/pickup-contacts" "{\"consignee\":\"Smoke Updated\",\"phone\":\"13900000001\",\"label\":\"公司\",\"detail\":\"滨江社区前置仓 A 区\",\"defaultContact\":true}")"
assert_code_ok "创建自提联系人" "${CREATE_CONTACT_RES}"
CONTACT_ID="$(echo "${CREATE_CONTACT_RES}" | sed -n 's/.*"id"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
if [[ -z "${CONTACT_ID}" ]]; then
  echo "${CREATE_CONTACT_RES}" >&2
  fail "未能从自提联系人响应中解析联系人 ID"
fi
echo "OK: 自提联系人 ID = ${CONTACT_ID}"

next_step "自提联系人列表: GET /api/user/pickup-contacts"
CONTACT_LIST_RES="$(request GET "/api/user/pickup-contacts")"
assert_code_ok "自提联系人列表" "${CONTACT_LIST_RES}"
if ! printf '%s' "${CONTACT_LIST_RES}" | grep -q "\"id\":\"${CONTACT_ID}\""; then
  echo "${CONTACT_LIST_RES}" >&2
  fail "自提联系人列表中未找到刚创建的联系人"
fi
echo "OK: 自提联系人列表包含 ${CONTACT_ID}"

next_step "更新自提联系人: PUT /api/user/pickup-contacts/{id}"
UPDATE_CONTACT_RES="$(request PUT "/api/user/pickup-contacts/${CONTACT_ID}" "{\"consignee\":\"Smoke Updated\",\"phone\":\"13900000001\",\"label\":\"家\",\"detail\":\"晚间自提\",\"defaultContact\":true}")"
assert_code_ok "更新自提联系人" "${UPDATE_CONTACT_RES}"
if ! printf '%s' "${UPDATE_CONTACT_RES}" | grep -q '"label":"家"'; then
  echo "${UPDATE_CONTACT_RES}" >&2
  fail "自提联系人更新后标签未生效"
fi
echo "OK: 自提联系人已更新"

next_step "加入购物车: POST /api/user/cart/items"
ADD_CART_RES="$(request POST "/api/user/cart/items" "{\"productId\":\"${PRODUCT_ID}\",\"quantity\":2}")"
assert_code_ok "加入购物车" "${ADD_CART_RES}"

next_step "购物车列表: GET /api/user/cart/items"
CART_LIST_RES="$(request GET "/api/user/cart/items")"
assert_code_ok "购物车列表" "${CART_LIST_RES}"
CART_ITEM_ID="$(echo "${CART_LIST_RES}" | sed -n 's/.*"data"[[:space:]]*:[[:space:]]*\[{"id"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
if [[ -z "${CART_ITEM_ID}" ]]; then
  echo "${CART_LIST_RES}" >&2
  fail "未能从购物车列表中解析购物车项 ID"
fi
echo "OK: 购物车项 ID = ${CART_ITEM_ID}"

next_step "更新购物车数量: PATCH /api/user/cart/items/{id}"
UPDATE_CART_RES="$(request PATCH "/api/user/cart/items/${CART_ITEM_ID}" "{\"quantity\":3}")"
assert_code_ok "更新购物车数量" "${UPDATE_CART_RES}"

next_step "更新后购物车列表: GET /api/user/cart/items"
UPDATED_CART_LIST_RES="$(request GET "/api/user/cart/items")"
assert_code_ok "更新后购物车列表" "${UPDATED_CART_LIST_RES}"
if ! printf '%s' "${UPDATED_CART_LIST_RES}" | grep -q '"quantity":3'; then
  echo "${UPDATED_CART_LIST_RES}" >&2
  fail "购物车数量更新后不是 3"
fi
echo "OK: 购物车数量已更新为 3"

next_step "删除购物车项: DELETE /api/user/cart/items/{id}"
REMOVE_CART_RES="$(request DELETE "/api/user/cart/items/${CART_ITEM_ID}")"
assert_code_ok "删除购物车项" "${REMOVE_CART_RES}"

next_step "再次加入购物车: POST /api/user/cart/items"
ADD_CART_AGAIN_RES="$(request POST "/api/user/cart/items" "{\"productId\":\"${PRODUCT_ID}\",\"quantity\":1}")"
assert_code_ok "再次加入购物车" "${ADD_CART_AGAIN_RES}"

next_step "提交订单: POST /api/user/orders"
SUBMIT_ORDER_RES="$(request POST "/api/user/orders" "{\"remark\":\"smoke order\",\"pickupContactId\":\"${CONTACT_ID}\"}")"
assert_code_ok "提交订单" "${SUBMIT_ORDER_RES}"
ORDER_ID="$(echo "${SUBMIT_ORDER_RES}" | sed -n 's/.*"id"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
if [[ -z "${ORDER_ID}" ]]; then
  echo "${SUBMIT_ORDER_RES}" >&2
  fail "未能从下单响应中解析订单 ID"
fi
echo "OK: 订单 ID = ${ORDER_ID}"

next_step "订单列表: GET /api/user/orders"
ORDER_PAGE_RES="$(request GET "/api/user/orders?page=1&pageSize=5&sortBy=orderTime&sortOrder=desc")"
assert_code_ok "订单列表" "${ORDER_PAGE_RES}"
if ! printf '%s' "${ORDER_PAGE_RES}" | grep -q "\"id\":\"${ORDER_ID}\""; then
  echo "${ORDER_PAGE_RES}" >&2
  fail "订单列表中未找到刚提交的订单"
fi
echo "OK: 订单列表包含新订单 ${ORDER_ID}"

next_step "订单详情: GET /api/user/orders/{id}"
ORDER_DETAIL_RES="$(request GET "/api/user/orders/${ORDER_ID}")"
assert_code_ok "订单详情" "${ORDER_DETAIL_RES}"
if ! printf '%s' "${ORDER_DETAIL_RES}" | grep -q "\"id\":\"${ORDER_ID}\""; then
  echo "${ORDER_DETAIL_RES}" >&2
  fail "订单详情中未找到刚提交的订单"
fi
if ! printf '%s' "${ORDER_DETAIL_RES}" | grep -q '"phone":"13900000001"'; then
  echo "${ORDER_DETAIL_RES}" >&2
  fail "订单详情未使用指定自提联系人手机号"
fi
echo "OK: 订单详情可查询 ${ORDER_ID}"

next_step "取消订单: DELETE /api/user/orders/{id}"
CANCEL_REASON="smoke 用户主动取消"
CANCEL_ORDER_RES="$(request DELETE "/api/user/orders/${ORDER_ID}" "{\"cancelReason\":\"${CANCEL_REASON}\"}")"
assert_code_ok "取消订单" "${CANCEL_ORDER_RES}"

next_step "取消后订单详情: GET /api/user/orders/{id}"
CANCELLED_ORDER_DETAIL_RES="$(request GET "/api/user/orders/${ORDER_ID}")"
assert_code_ok "取消后订单详情" "${CANCELLED_ORDER_DETAIL_RES}"
if ! printf '%s' "${CANCELLED_ORDER_DETAIL_RES}" | grep -q '"status":"cancelled"'; then
  echo "${CANCELLED_ORDER_DETAIL_RES}" >&2
  fail "订单取消后状态不是 cancelled"
fi
if ! printf '%s' "${CANCELLED_ORDER_DETAIL_RES}" | grep -q "\"cancelReason\":\"${CANCEL_REASON}\""; then
  echo "${CANCELLED_ORDER_DETAIL_RES}" >&2
  fail "订单取消原因未按请求落库"
fi
echo "OK: 订单 ${ORDER_ID} 已取消"

next_step "再次加入购物车用于支付流转: POST /api/user/cart/items"
ADD_CART_FOR_PAYMENT_RES="$(request POST "/api/user/cart/items" "{\"productId\":\"${PRODUCT_ID}\",\"quantity\":1}")"
assert_code_ok "再次加入购物车用于支付流转" "${ADD_CART_FOR_PAYMENT_RES}"

next_step "再次提交订单: POST /api/user/orders"
SUBMIT_PAYABLE_ORDER_RES="$(request POST "/api/user/orders" "{\"remark\":\"smoke pay order\",\"pickupContactId\":\"${CONTACT_ID}\"}")"
assert_code_ok "再次提交订单" "${SUBMIT_PAYABLE_ORDER_RES}"
PAYABLE_ORDER_ID="$(echo "${SUBMIT_PAYABLE_ORDER_RES}" | sed -n 's/.*"id"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
if [[ -z "${PAYABLE_ORDER_ID}" ]]; then
  echo "${SUBMIT_PAYABLE_ORDER_RES}" >&2
  fail "未能从第二次下单响应中解析订单 ID"
fi
echo "OK: 可支付订单 ID = ${PAYABLE_ORDER_ID}"

next_step "支付订单: POST /api/user/orders/{id}/pay"
PAY_ORDER_RES="$(request POST "/api/user/orders/${PAYABLE_ORDER_ID}/pay")"
assert_code_ok "支付订单" "${PAY_ORDER_RES}"
if ! printf '%s' "${PAY_ORDER_RES}" | grep -q '"status":"to_prepare"'; then
  echo "${PAY_ORDER_RES}" >&2
  fail "支付后订单状态不是 to_prepare"
fi
if ! printf '%s' "${PAY_ORDER_RES}" | grep -q '"payStatus":"paid"'; then
  echo "${PAY_ORDER_RES}" >&2
  fail "支付后订单支付状态不是 paid"
fi
echo "OK: 订单 ${PAYABLE_ORDER_ID} 支付成功"

printf '\n用户链路 smoke 通过: %s\n' "${BASE_URL}"
