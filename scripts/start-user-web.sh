#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ROOT_DIR}/.env.local"

# shellcheck disable=SC1091
source "${ROOT_DIR}/scripts/lib/web-env.sh"

load_shelfflow_env_file "${ENV_FILE}"
ensure_shelfflow_node_dependencies "${ROOT_DIR}"

USER_WEB_HOST="${SHELFFLOW_USER_WEB_HOST:-127.0.0.1}"
USER_WEB_PORT="${SHELFFLOW_USER_WEB_PORT:-3001}"
GATEWAY_BASE_URL="${SHELFFLOW_GATEWAY_BASE_URL:-http://127.0.0.1:4010}"

export SHELFFLOW_GATEWAY_BASE_URL="${GATEWAY_BASE_URL}"
export NEXT_PUBLIC_SHELFFLOW_GATEWAY_BASE_URL="${GATEWAY_BASE_URL}"

free_port() {
  local port="$1"
  local pids

  pids="$(lsof -ti tcp:"${port}" || true)"
  if [[ -n "${pids}" ]]; then
    echo "释放端口 ${port}: ${pids}"
    kill ${pids} >/dev/null 2>&1 || true
    sleep 1
  fi
}

echo "启动 ShelfFlow 用户端"
echo "用户端地址: http://${USER_WEB_HOST}:${USER_WEB_PORT}"
echo "网关地址: ${GATEWAY_BASE_URL}"

free_port "${USER_WEB_PORT}"

cd "${ROOT_DIR}/apps/shelfflow-user-web"
npm run dev -- --hostname "${USER_WEB_HOST}" --port "${USER_WEB_PORT}"
