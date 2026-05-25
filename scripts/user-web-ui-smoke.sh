#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/shelfflow-env.sh"

if ! command -v npx >/dev/null 2>&1; then
  echo "FAIL: npx 不可用，无法执行用户端 UI smoke" >&2
  exit 1
fi

USER_WEB_HEALTH_URL="${SHELFFLOW_USER_WEB_BASE_URL}/products"

echo "[ui-smoke] user-web"
echo "检查用户端页面入口: ${USER_WEB_HEALTH_URL}"

if ! curl -fsS "${USER_WEB_HEALTH_URL}" >/dev/null; then
  echo "FAIL: 用户端页面不可访问。请先运行 START_USER_WEB=true bash scripts/start-local-all.sh" >&2
  exit 1
fi

echo "[ui-smoke] seed demo users"
bash "${SCRIPT_DIR}/seed-user-demo-data.sh" "${SHELFFLOW_GATEWAY_BASE_URL}"

export SHELFFLOW_USER_WEB_BASE_URL
export SHELFFLOW_USER_SMOKE_OPEN_ID
export SHELFFLOW_USER_SMOKE_NAME
export SHELFFLOW_USER_SMOKE_PHONE
export SHELFFLOW_USER_SMOKE_PASSWORD

npx --yes --package playwright node "${SCRIPT_DIR}/user-web-ui-smoke.mjs"
