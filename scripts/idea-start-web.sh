#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "${ROOT_DIR}/scripts/shelfflow-env.sh"

LOG_DIR="${ROOT_DIR}/logs"
WEB_READY_TIMEOUT_SECONDS="${WEB_READY_TIMEOUT_SECONDS:-45}"
START_ADMIN_WEB="${START_ADMIN_WEB:-true}"
START_USER_WEB="${START_USER_WEB:-true}"

PIDS=()

mkdir -p "${LOG_DIR}"

require_command() {
  local command_name="$1"

  if ! command -v "${command_name}" >/dev/null 2>&1; then
    echo "[fail] 未找到命令: ${command_name}" >&2
    exit 1
  fi
}

ensure_port_available() {
  local service_name="$1"
  local port="$2"

  if lsof -ti tcp:"${port}" >/dev/null 2>&1; then
    echo "[fail] ${service_name} 端口 ${port} 已被占用" >&2
    echo "[hint] 请先停止旧进程，或在 .env.local 中修改端口" >&2
    exit 1
  fi
}

wait_for_http() {
  local service_name="$1"
  local url="$2"
  local attempt http_code

  for attempt in $(seq 1 "${WEB_READY_TIMEOUT_SECONDS}"); do
    http_code="$(curl -s -o /dev/null -w '%{http_code}' "${url}" || true)"
    if [[ "${http_code}" == "200" ]]; then
      echo "[ready] ${service_name}: ${url}"
      return 0
    fi
    sleep 1
  done

  echo "[fail] ${service_name} 未在 ${WEB_READY_TIMEOUT_SECONDS}s 内就绪: ${url}" >&2
  exit 1
}

cleanup() {
  local status=$?

  for pid in "${PIDS[@]:-}"; do
    if kill -0 "${pid}" >/dev/null 2>&1; then
      kill "${pid}" >/dev/null 2>&1 || true
      wait "${pid}" >/dev/null 2>&1 || true
    fi
  done

  exit "${status}"
}

start_admin_web() {
  echo "[start] admin web -> ${SHELFFLOW_ADMIN_BASE_URL}"
  (
    cd "${ROOT_DIR}/apps/shelfflow-admin-web"
    env \
      PORT="${SHELFFLOW_ADMIN_PORT}" \
      HOSTNAME="${SHELFFLOW_ADMIN_HOST}" \
      SHELFFLOW_GATEWAY_BASE_URL="${SHELFFLOW_GATEWAY_BASE_URL}" \
      npm run dev -- --hostname "${SHELFFLOW_ADMIN_HOST}" --port "${SHELFFLOW_ADMIN_PORT}"
  ) > "${LOG_DIR}/idea-admin-web.log" 2>&1 &
  PIDS+=("$!")
}

start_user_web() {
  echo "[start] user web -> ${SHELFFLOW_USER_WEB_BASE_URL}"
  (
    cd "${ROOT_DIR}/apps/shelfflow-user-web"
    env \
      PORT="${SHELFFLOW_USER_WEB_PORT}" \
      HOSTNAME="${SHELFFLOW_USER_WEB_HOST}" \
      SHELFFLOW_GATEWAY_BASE_URL="${SHELFFLOW_GATEWAY_BASE_URL}" \
      npm run dev -- --hostname "${SHELFFLOW_USER_WEB_HOST}" --port "${SHELFFLOW_USER_WEB_PORT}"
  ) > "${LOG_DIR}/idea-user-web.log" 2>&1 &
  PIDS+=("$!")
}

require_command "npm"
require_command "curl"
require_command "lsof"

trap cleanup EXIT INT TERM

echo "ShelfFlow IDEA Web 启动脚本"
echo "管理端: ${SHELFFLOW_ADMIN_BASE_URL}"
echo "用户端: ${SHELFFLOW_USER_WEB_BASE_URL}"
echo "Gateway: ${SHELFFLOW_GATEWAY_BASE_URL}"
echo "日志目录: ${LOG_DIR}"

if [[ "${START_ADMIN_WEB}" == "true" ]]; then
  ensure_port_available "管理端" "${SHELFFLOW_ADMIN_PORT}"
  start_admin_web
fi

if [[ "${START_USER_WEB}" == "true" ]]; then
  ensure_port_available "用户端" "${SHELFFLOW_USER_WEB_PORT}"
  start_user_web
fi

if [[ "${START_ADMIN_WEB}" == "true" ]]; then
  wait_for_http "admin web" "${SHELFFLOW_ADMIN_BASE_URL}/login"
fi

if [[ "${START_USER_WEB}" == "true" ]]; then
  wait_for_http "user web" "${SHELFFLOW_USER_WEB_BASE_URL}/products"
fi

echo "启动完成。按 Ctrl+C 或在 IDEA 中 Stop 可停止管理端和用户端。"
wait
