#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "${ROOT_DIR}/scripts/shelfflow-env.sh"

BASE_URL="${1:-${BASE_URL:-${SHELFFLOW_GATEWAY_BASE_URL}}}"
ADMIN_USERNAME="${ADMIN_USERNAME:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-123456}"
CURL_MAX_TIME="${CURL_MAX_TIME:-10}"

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT

step_no=0
ACCESS_TOKEN=""
PRODUCT_NAME="sf-p-$(date +%s)"
UPDATED_PRODUCT_NAME="${PRODUCT_NAME}-u"
BATCH_CODE="BATCH-$(date +%s)"
PRODUCT_ID=""
BATCH_ID=""
PRODUCT_CATEGORY_ID=""
PRICING_RULE_NAME="sf-rule-$(date +%s)"
UPDATED_PRICING_RULE_NAME="${PRICING_RULE_NAME}-u"
PRICING_RULE_ID=""
SMOKE_PRICING_MIN_DAYS="${SMOKE_PRICING_MIN_DAYS:-1}"
SMOKE_PRICING_MAX_DAYS="${SMOKE_PRICING_MAX_DAYS:-3}"
SMOKE_PRICING_UPDATED_MAX_DAYS="${SMOKE_PRICING_UPDATED_MAX_DAYS:-5}"
SMOKE_PRICING_DISCOUNT_RATE="${SMOKE_PRICING_DISCOUNT_RATE:-0.85}"
SMOKE_PRICING_UPDATED_DISCOUNT_RATE="${SMOKE_PRICING_UPDATED_DISCOUNT_RATE:-0.80}"
SMOKE_PRICING_PRIORITY="${SMOKE_PRICING_PRIORITY:-90}"
SMOKE_PRICING_UPDATED_PRIORITY="${SMOKE_PRICING_UPDATED_PRIORITY:-95}"

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
    -Nse "select id from category where type = 1 order by id asc limit 1" \
    "${DB_NAME}" 2>/dev/null | tr -d '[:space:]'
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

next_step "管理端登录: POST /api/admin/auth/login"
LOGIN_RES="$(request POST "/api/admin/auth/login" "{\"username\":\"${ADMIN_USERNAME}\",\"password\":\"${ADMIN_PASSWORD}\"}")"
assert_code_ok "管理端登录" "${LOGIN_RES}"
ACCESS_TOKEN="$(echo "${LOGIN_RES}" | sed -n 's/.*"token"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
if [[ -z "${ACCESS_TOKEN}" ]]; then
  echo "${LOGIN_RES}" >&2
  fail "未能从登录响应中解析 token"
fi
echo "OK: 已取得访问令牌"

next_step "当前登录会话: GET /api/admin/auth/me"
ME_RES="$(request GET "/api/admin/auth/me")"
assert_code_ok "当前登录会话" "${ME_RES}"

next_step "商品分页: GET /api/admin/products"
PRODUCT_RES="$(request GET "/api/admin/products?page=1&pageSize=5&sortBy=updatedAt&sortOrder=desc")"
assert_code_ok "商品分页" "${PRODUCT_RES}"
PRODUCT_CATEGORY_ID="$(resolve_product_category_id || true)"
if [[ -z "${PRODUCT_CATEGORY_ID}" ]]; then
  echo "${PRODUCT_RES}" >&2
  fail "未能从数据库解析可用分类 ID"
fi

next_step "批次分页: GET /api/admin/inventory-batches"
BATCH_RES="$(request GET "/api/admin/inventory-batches?page=1&pageSize=5&sortBy=updatedAt&sortOrder=desc")"
assert_code_ok "批次分页" "${BATCH_RES}"

next_step "定价规则分页: GET /api/admin/pricing-rules"
PRICING_RULE_RES="$(request GET "/api/admin/pricing-rules?page=1&pageSize=5&sortBy=updatedAt&sortOrder=desc")"
assert_code_ok "定价规则分页" "${PRICING_RULE_RES}"

next_step "创建定价规则: POST /api/admin/pricing-rules"
CREATE_PRICING_RULE_RES="$(request POST "/api/admin/pricing-rules" "{\"name\":\"${PRICING_RULE_NAME}\",\"minDaysToExpire\":${SMOKE_PRICING_MIN_DAYS},\"maxDaysToExpire\":${SMOKE_PRICING_MAX_DAYS},\"discountRate\":${SMOKE_PRICING_DISCOUNT_RATE},\"priority\":${SMOKE_PRICING_PRIORITY},\"status\":\"enabled\"}")"
assert_code_ok "创建定价规则" "${CREATE_PRICING_RULE_RES}"
PRICING_RULE_ID="$(echo "${CREATE_PRICING_RULE_RES}" | sed -n 's/.*"id"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -n 1)"
if [[ -z "${PRICING_RULE_ID}" ]]; then
  echo "${CREATE_PRICING_RULE_RES}" >&2
  fail "未能从定价规则创建响应中解析 ID"
fi
echo "OK: 新建定价规则 ID = ${PRICING_RULE_ID}"

next_step "更新定价规则: PUT /api/admin/pricing-rules/{id}"
UPDATE_PRICING_RULE_RES="$(request PUT "/api/admin/pricing-rules/${PRICING_RULE_ID}" "{\"name\":\"${UPDATED_PRICING_RULE_NAME}\",\"minDaysToExpire\":${SMOKE_PRICING_MIN_DAYS},\"maxDaysToExpire\":${SMOKE_PRICING_UPDATED_MAX_DAYS},\"discountRate\":${SMOKE_PRICING_UPDATED_DISCOUNT_RATE},\"priority\":${SMOKE_PRICING_UPDATED_PRIORITY},\"status\":\"enabled\"}")"
assert_code_ok "更新定价规则" "${UPDATE_PRICING_RULE_RES}"

next_step "定价规则状态流转: POST /api/admin/pricing-rules/{id}/status"
UPDATE_PRICING_RULE_STATUS_RES="$(request POST "/api/admin/pricing-rules/${PRICING_RULE_ID}/status" "{\"status\":\"disabled\"}")"
assert_code_ok "定价规则状态流转" "${UPDATE_PRICING_RULE_STATUS_RES}"

next_step "AI 定价建议: GET /api/admin/pricing-rules/suggestions"
PRICING_SUGGESTIONS_RES="$(request GET "/api/admin/pricing-rules/suggestions")"
assert_code_ok "AI 定价建议" "${PRICING_SUGGESTIONS_RES}"

next_step "损耗统计总览: GET /api/admin/loss-stats/overview"
LOSS_STATS_RES="$(request GET "/api/admin/loss-stats/overview")"
assert_code_ok "损耗统计总览" "${LOSS_STATS_RES}"

next_step "AI 运营建议: GET /api/admin/ai-ops/suggestions"
AI_SUGGESTIONS_RES="$(request GET "/api/admin/ai-ops/suggestions")"
assert_code_ok "AI 运营建议" "${AI_SUGGESTIONS_RES}"

next_step "AI 知识库分页: GET /api/admin/ai-ops/knowledge"
AI_KNOWLEDGE_RES="$(request GET "/api/admin/ai-ops/knowledge?page=1&pageSize=5&sortBy=updatedAt&sortOrder=desc")"
assert_code_ok "AI 知识库分页" "${AI_KNOWLEDGE_RES}"

next_step "AI 运营问答: POST /api/admin/ai-ops/chat"
AI_CHAT_RES="$(request POST "/api/admin/ai-ops/chat" "{\"message\":\"哪些批次需要紧急处理\"}")"
assert_code_ok "AI 运营问答" "${AI_CHAT_RES}"

next_step "创建商品: POST /api/admin/products"
CREATE_PRODUCT_RES="$(request POST "/api/admin/products" "{\"name\":\"${PRODUCT_NAME}\",\"categoryId\":\"${PRODUCT_CATEGORY_ID}\",\"price\":19.9,\"description\":\"smoke\",\"image\":\"\",\"status\":\"active\",\"shelfLifeDays\":7}")"
assert_code_ok "创建商品" "${CREATE_PRODUCT_RES}"

next_step "查询刚创建商品: GET /api/admin/products"
PRODUCT_LOOKUP_RES="$(request GET "/api/admin/products?page=1&pageSize=10&keyword=${PRODUCT_NAME}")"
assert_code_ok "查询刚创建商品" "${PRODUCT_LOOKUP_RES}"
PRODUCT_ID="$(echo "${PRODUCT_LOOKUP_RES}" | sed -n 's/.*"id"[[:space:]]*:[[:space:]]*"\([^"]*\)".*"name"[[:space:]]*:[[:space:]]*"'"${PRODUCT_NAME}"'".*/\1/p' | head -n 1)"
if [[ -z "${PRODUCT_ID}" ]]; then
  echo "${PRODUCT_LOOKUP_RES}" >&2
  fail "未能从商品分页中解析新建商品 ID"
fi
echo "OK: 新建商品 ID = ${PRODUCT_ID}"

next_step "更新商品: PUT /api/admin/products/{id}"
UPDATE_PRODUCT_RES="$(request PUT "/api/admin/products/${PRODUCT_ID}" "{\"name\":\"${UPDATED_PRODUCT_NAME}\",\"categoryId\":\"${PRODUCT_CATEGORY_ID}\",\"price\":21.5,\"description\":\"smoke updated\",\"image\":\"\",\"status\":\"active\",\"shelfLifeDays\":9}")"
assert_code_ok "更新商品" "${UPDATE_PRODUCT_RES}"

next_step "创建批次: POST /api/admin/inventory-batches"
CREATE_BATCH_RES="$(request POST "/api/admin/inventory-batches" "{\"productId\":\"${PRODUCT_ID}\",\"batchCode\":\"${BATCH_CODE}\",\"productionDate\":\"2026-05-12T00:00:00\",\"expiryDate\":\"2026-05-19T00:00:00\",\"stockQuantity\":10,\"basePrice\":9.9,\"batchStatus\":\"active\",\"pricingStatus\":\"active\"}")"
assert_code_ok "创建批次" "${CREATE_BATCH_RES}"

next_step "查询刚创建批次: GET /api/admin/inventory-batches"
BATCH_LOOKUP_RES="$(request GET "/api/admin/inventory-batches?page=1&pageSize=10&keyword=${BATCH_CODE}")"
assert_code_ok "查询刚创建批次" "${BATCH_LOOKUP_RES}"
BATCH_ID="$(echo "${BATCH_LOOKUP_RES}" | sed -n 's/.*"id"[[:space:]]*:[[:space:]]*"\([^"]*\)".*"batchCode"[[:space:]]*:[[:space:]]*"'"${BATCH_CODE}"'".*/\1/p' | head -n 1)"
if [[ -z "${BATCH_ID}" ]]; then
  echo "${BATCH_LOOKUP_RES}" >&2
  fail "未能从批次分页中解析新建批次 ID"
fi
echo "OK: 新建批次 ID = ${BATCH_ID}"

next_step "更新批次: PUT /api/admin/inventory-batches/{id}"
UPDATE_BATCH_RES="$(request PUT "/api/admin/inventory-batches/${BATCH_ID}" "{\"productId\":\"${PRODUCT_ID}\",\"batchCode\":\"${BATCH_CODE}\",\"productionDate\":\"2026-05-12T00:00:00\",\"expiryDate\":\"2026-05-21T00:00:00\",\"stockQuantity\":12,\"basePrice\":10.5,\"batchStatus\":\"active\",\"pricingStatus\":\"active\"}")"
assert_code_ok "更新批次" "${UPDATE_BATCH_RES}"

next_step "批次状态流转: POST /api/admin/inventory-batches/{id}/status"
UPDATE_BATCH_STATUS_RES="$(request POST "/api/admin/inventory-batches/${BATCH_ID}/status" "{\"batchStatus\":\"paused\"}")"
assert_code_ok "批次状态流转" "${UPDATE_BATCH_STATUS_RES}"

printf '\n微服务 smoke 通过: %s\n' "${BASE_URL}"
