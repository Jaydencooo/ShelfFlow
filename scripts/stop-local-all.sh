#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="${ROOT_DIR}/logs"

if [[ ! -d "${LOG_DIR}" ]]; then
  echo "日志目录不存在，无需停止。"
  exit 0
fi

stop_pid_file() {
  local file="$1"
  local pid

  if [[ ! -f "${file}" ]]; then
    return
  fi

  pid="$(cat "${file}")"
  if [[ -n "${pid}" ]] && kill -0 "${pid}" >/dev/null 2>&1; then
    kill "${pid}" >/dev/null 2>&1 || true
    wait "${pid}" >/dev/null 2>&1 || true
    echo "已停止 ${file##*/}: ${pid}"
  else
    echo "进程已退出 ${file##*/}: ${pid:-unknown}"
  fi

  rm -f "${file}"
}

PID_FILES=(
  "${LOG_DIR}/admin-web.pid"
  "${LOG_DIR}/user-web.pid"
  "${LOG_DIR}/gateway.pid"
  "${LOG_DIR}/user-service.pid"
  "${LOG_DIR}/admin-service.pid"
  "${LOG_DIR}/auth-service.pid"
  "${LOG_DIR}/legacy-backend.pid"
)

for file in "${PID_FILES[@]}"; do
  stop_pid_file "${file}"
done

echo "本地服务已停止。"
