#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "${ROOT_DIR}/scripts/shelfflow-env.sh"

BASE_URL="${1:-${BASE_URL:-${SHELFFLOW_GATEWAY_BASE_URL}}}"
CURL_MAX_TIME="${CURL_MAX_TIME:-10}"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT

ADMIN_USERNAME="${ADMIN_USERNAME:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-123456}"
SMOKE_PRODUCT_PRICE="${SMOKE_PRODUCT_PRICE:-16.8}"
SMOKE_BATCH_BASE_PRICE="${SMOKE_BATCH_BASE_PRICE:-9.9}"
SMOKE_BATCH_STOCK_QUANTITY="${SMOKE_BATCH_STOCK_QUANTITY:-8}"

step_no=0
USER_ACCESS_TOKEN=""
ADMIN_ACCESS_TOKEN=""
OPEN_ID="sf-order-flow-$(date +%s)"
USER_PASSWORD="${SHELFFLOW_USER_SMOKE_PASSWORD:-ShelfFlow#2026}"
PRODUCT_NAME="sf-order-p-$(date +%s)"
BATCH_CODE="FLOW-BATCH-$(date +%s)"
PRODUCT_ID=""
PRODUCT_CATEGORY_ID=""
ORDER_PRODUCT_ID=""
PAID_ORDER_ID=""

next_step() {
  step_no=$((step_no + 1))
  printf '\n[%s] %s\n' "${step_no}" "$1"
}

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

resolve_product_category_id() {
  if ! command -v mysql >/dev/null 2>&1; then
    return 1
  fi

  MYSQL_PWD="${DB_PASSWORD:-}" mysql \
    --protocol=TCP \
    -h "${DB_HOST}" \
    -P "${DB_PORT}" \
    -u "${DB_USER}" \
    -Nse "select id from category where type = 1 and status = 1 order by id asc limit 1" \
    "${DB_NAME}" 2>/dev/null | tr -d '[:space:]'
}

format_datetime_offset() {
  local offset_days="$1"

  if command -v python3 >/dev/null 2>&1; then
    python3 -c "from datetime import datetime, timedelta; print((datetime.now() + timedelta(days=${offset_days})).strftime('%Y-%m-%dT00:00:00'))"
    return 0
  fi

  if date -v+"${offset_days}"d '+%Y-%m-%dT00:00:00' >/dev/null 2>&1; then
    date -v+"${offset_days}"d '+%Y-%m-%dT00:00:00'
    return 0
  fi

  if date -d "+${offset_days} day" '+%Y-%m-%dT00:00:00' >/dev/null 2>&1; then
    date -d "+${offset_days} day" '+%Y-%m-%dT00:00:00'
    return 0
  fi

  fail "当前环境不支持日期偏移计算"
}

request() {
  local method="$1"
  local path="$2"
  local body="${3:-}"
  local token="${4:-}"
  local output="${TMP_DIR}/response-${step_no}.json"
  local status
  local args=(-sS --max-time "${CURL_MAX_TIME}" -o "${output}" -w "%{http_code}" -X "${method}")

  if [[ -n "${token}" ]]; then
    args+=(-H "Authorization: Bearer ${token}")
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

next_step "管理端登录: POST /api/admin/auth/login"
ADMIN_LOGIN_RES="$(request POST "/api/admin/auth/login" "{\"username\":\"${ADMIN_USERNAME}\",\"password\":\"${ADMIN_PASSWORD}\"}")"
assert_code_ok "管理端登录" "${ADMIN_LOGIN_RES}"
ADMIN_ACCESS_TOKEN="$(echo "${ADMIN_LOGIN_RES}" | sed -n 's/.*"token"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
if [[ -z "${ADMIN_ACCESS_TOKEN}" ]]; then
  echo "${ADMIN_LOGIN_RES}" >&2
  fail "未能从管理端登录响应中解析 token"
fi
echo "OK: 已取得管理端访问令牌"

PRODUCT_CATEGORY_ID="$(resolve_product_category_id || true)"
if [[ -z "${PRODUCT_CATEGORY_ID}" ]]; then
  fail "未能从数据库解析用户侧可用商品分类 ID"
fi
echo "OK: 商品分类 ID = ${PRODUCT_CATEGORY_ID}"

next_step "创建可售商品: POST /api/admin/products"
CREATE_PRODUCT_RES="$(request POST "/api/admin/products" "{\"name\":\"${PRODUCT_NAME}\",\"categoryId\":\"${PRODUCT_CATEGORY_ID}\",\"price\":${SMOKE_PRODUCT_PRICE},\"description\":\"order lifecycle smoke\",\"image\":\"\",\"status\":\"active\",\"shelfLifeDays\":7}" "${ADMIN_ACCESS_TOKEN}")"
assert_code_ok "创建可售商品" "${CREATE_PRODUCT_RES}"

next_step "查询刚创建商品: GET /api/admin/products"
PRODUCT_LOOKUP_RES="$(request GET "/api/admin/products?page=1&pageSize=10&keyword=${PRODUCT_NAME}" "" "${ADMIN_ACCESS_TOKEN}")"
assert_code_ok "查询刚创建商品" "${PRODUCT_LOOKUP_RES}"
PRODUCT_ID="$(echo "${PRODUCT_LOOKUP_RES}" | sed -n 's/.*"id"[[:space:]]*:[[:space:]]*"\([^"]*\)".*"name"[[:space:]]*:[[:space:]]*"'"${PRODUCT_NAME}"'".*/\1/p' | head -n 1)"
if [[ -z "${PRODUCT_ID}" ]]; then
  echo "${PRODUCT_LOOKUP_RES}" >&2
  fail "未能从管理端商品列表中解析商品 ID"
fi
echo "OK: 新建商品 ID = ${PRODUCT_ID}"

next_step "创建可售批次: POST /api/admin/inventory-batches"
CREATE_BATCH_RES="$(request POST "/api/admin/inventory-batches" "{\"productId\":\"${PRODUCT_ID}\",\"batchCode\":\"${BATCH_CODE}\",\"productionDate\":\"$(format_datetime_offset -1)\",\"expiryDate\":\"$(format_datetime_offset 10)\",\"stockQuantity\":${SMOKE_BATCH_STOCK_QUANTITY},\"basePrice\":${SMOKE_BATCH_BASE_PRICE},\"batchStatus\":\"active\",\"pricingStatus\":\"active\"}" "${ADMIN_ACCESS_TOKEN}")"
assert_code_ok "创建可售批次" "${CREATE_BATCH_RES}"

next_step "查询用户商品列表: GET /api/user/catalog/products"
PRODUCT_PAGE_RES="$(request GET "/api/user/catalog/products?page=1&pageSize=5&keyword=${PRODUCT_NAME}&sortBy=updatedAt&sortOrder=desc")"
assert_code_ok "用户商品列表" "${PRODUCT_PAGE_RES}"
ORDER_PRODUCT_ID="$(echo "${PRODUCT_PAGE_RES}" | sed -n 's/.*"items"[[:space:]]*:[[:space:]]*\[{"id"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
if [[ -z "${ORDER_PRODUCT_ID}" ]]; then
  echo "${PRODUCT_PAGE_RES}" >&2
  fail "未能从商品列表中解析商品 ID"
fi
echo "OK: 商品 ID = ${ORDER_PRODUCT_ID}"

next_step "用户注册: POST /api/user/auth/register"
USER_REGISTER_RES="$(request POST "/api/user/auth/register" "{\"openId\":\"${OPEN_ID}\",\"name\":\"Flow User\",\"phone\":\"13900000001\",\"password\":\"${USER_PASSWORD}\"}")"
assert_code_ok "用户注册" "${USER_REGISTER_RES}"

next_step "用户登录: POST /api/user/auth/login"
USER_LOGIN_RES="$(request POST "/api/user/auth/login" "{\"openId\":\"${OPEN_ID}\",\"password\":\"${USER_PASSWORD}\"}")"
assert_code_ok "用户登录" "${USER_LOGIN_RES}"
USER_ACCESS_TOKEN="$(echo "${USER_LOGIN_RES}" | sed -n 's/.*"token"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
if [[ -z "${USER_ACCESS_TOKEN}" ]]; then
  echo "${USER_LOGIN_RES}" >&2
  fail "未能从用户登录响应中解析 token"
fi
echo "OK: 已取得用户访问令牌"

next_step "加入购物车: POST /api/user/cart/items"
ADD_CART_RES="$(request POST "/api/user/cart/items" "{\"productId\":\"${ORDER_PRODUCT_ID}\",\"quantity\":1}" "${USER_ACCESS_TOKEN}")"
assert_code_ok "加入购物车" "${ADD_CART_RES}"

next_step "提交订单: POST /api/user/orders"
SUBMIT_ORDER_RES="$(request POST "/api/user/orders" "{\"remark\":\"order lifecycle smoke\"}" "${USER_ACCESS_TOKEN}")"
assert_code_ok "提交订单" "${SUBMIT_ORDER_RES}"
PAID_ORDER_ID="$(echo "${SUBMIT_ORDER_RES}" | sed -n 's/.*"id"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
if [[ -z "${PAID_ORDER_ID}" ]]; then
  echo "${SUBMIT_ORDER_RES}" >&2
  fail "未能从下单响应中解析订单 ID"
fi
echo "OK: 订单 ID = ${PAID_ORDER_ID}"

next_step "支付订单: POST /api/user/orders/{id}/pay"
PAY_ORDER_RES="$(request POST "/api/user/orders/${PAID_ORDER_ID}/pay" "" "${USER_ACCESS_TOKEN}")"
assert_code_ok "支付订单" "${PAY_ORDER_RES}"
if ! printf '%s' "${PAY_ORDER_RES}" | grep -q '"status":"to_prepare"'; then
  echo "${PAY_ORDER_RES}" >&2
  fail "支付后订单状态不是 to_prepare"
fi
echo "OK: 订单 ${PAID_ORDER_ID} 已支付"

next_step "管理端订单列表: GET /api/admin/orders"
ADMIN_ORDER_PAGE_RES="$(request GET "/api/admin/orders?page=1&pageSize=10&keyword=${PAID_ORDER_ID}&sortBy=orderTime&sortOrder=desc" "" "${ADMIN_ACCESS_TOKEN}")"
assert_code_ok "管理端订单列表" "${ADMIN_ORDER_PAGE_RES}"
if ! printf '%s' "${ADMIN_ORDER_PAGE_RES}" | grep -q "\"id\":\"${PAID_ORDER_ID}\""; then
  echo "${ADMIN_ORDER_PAGE_RES}" >&2
  fail "管理端订单列表中未找到目标订单"
fi
echo "OK: 管理端可查询订单 ${PAID_ORDER_ID}"

for target_status in preparing ready_for_pickup completed; do
  next_step "管理端流转订单: POST /api/admin/orders/{id}/status -> ${target_status}"
  UPDATE_ORDER_RES="$(request POST "/api/admin/orders/${PAID_ORDER_ID}/status" "{\"orderStatus\":\"${target_status}\"}" "${ADMIN_ACCESS_TOKEN}")"
  assert_code_ok "管理端流转订单 ${target_status}" "${UPDATE_ORDER_RES}"
  if ! printf '%s' "${UPDATE_ORDER_RES}" | grep -q "\"status\":\"${target_status}\""; then
    echo "${UPDATE_ORDER_RES}" >&2
    fail "订单状态未更新为 ${target_status}"
  fi
  echo "OK: 订单 ${PAID_ORDER_ID} 已流转为 ${target_status}"
done

next_step "用户侧确认订单完成: GET /api/user/orders/{id}"
FINAL_DETAIL_RES="$(request GET "/api/user/orders/${PAID_ORDER_ID}" "" "${USER_ACCESS_TOKEN}")"
assert_code_ok "用户侧确认订单完成" "${FINAL_DETAIL_RES}"
if ! printf '%s' "${FINAL_DETAIL_RES}" | grep -q '"status":"completed"'; then
  echo "${FINAL_DETAIL_RES}" >&2
  fail "用户侧订单最终状态不是 completed"
fi
echo "OK: 用户侧订单最终状态为 completed"

printf '\n订单生命周期 smoke 通过: %s\n' "${BASE_URL}"
