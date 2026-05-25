#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/shelfflow-env.sh"

if ! command -v npx >/dev/null 2>&1; then
  echo "FAIL: npx 不可用，无法执行管理端 UI smoke" >&2
  exit 1
fi

ADMIN_WEB_HEALTH_URL="${SHELFFLOW_ADMIN_BASE_URL}/login"
GATEWAY_HEALTH_URL="${SHELFFLOW_GATEWAY_BASE_URL}/health"

echo "[ui-smoke] admin-web"
echo "检查管理端页面入口: ${ADMIN_WEB_HEALTH_URL}"
echo "检查 Gateway: ${GATEWAY_HEALTH_URL}"

if ! curl -fsS "${ADMIN_WEB_HEALTH_URL}" >/dev/null; then
  echo "FAIL: 管理端页面不可访问。请先运行 START_ADMIN_WEB=true bash scripts/start-local-all.sh" >&2
  exit 1
fi

if ! curl -fsS "${GATEWAY_HEALTH_URL}" >/dev/null; then
  echo "FAIL: Gateway 不可访问。请先启动 Java 后端和 gateway" >&2
  exit 1
fi

export SHELFFLOW_ADMIN_BASE_URL
export SHELFFLOW_GATEWAY_BASE_URL
export ADMIN_USERNAME="${ADMIN_USERNAME:-admin}"
export ADMIN_PASSWORD="${ADMIN_PASSWORD:-123456}"
export SHELFFLOW_ADMIN_UI_CATEGORY_ID="${SHELFFLOW_ADMIN_UI_CATEGORY_ID:-11}"

npx --yes --package playwright node "${SCRIPT_DIR}/admin-web-ui-smoke.mjs"
