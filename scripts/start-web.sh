#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

ADMIN_PID=""
USER_PID=""

cleanup() {
  if [[ -n "${ADMIN_PID}" ]]; then
    kill "${ADMIN_PID}" >/dev/null 2>&1 || true
  fi
  if [[ -n "${USER_PID}" ]]; then
    kill "${USER_PID}" >/dev/null 2>&1 || true
  fi
}

trap cleanup INT TERM EXIT

echo "启动 ShelfFlow Web"
echo "管理端: http://127.0.0.1:3000"
echo "用户端: http://127.0.0.1:3001"
echo "网关:   ${SHELFFLOW_GATEWAY_BASE_URL:-http://127.0.0.1:4010}"
echo

bash "${ROOT_DIR}/scripts/start-admin-web.sh" &
ADMIN_PID="$!"

bash "${ROOT_DIR}/scripts/start-user-web.sh" &
USER_PID="$!"

wait "${ADMIN_PID}"
wait "${USER_PID}"
