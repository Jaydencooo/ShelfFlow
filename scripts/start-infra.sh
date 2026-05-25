#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "${ROOT_DIR}/scripts/shelfflow-env.sh"

SHELFFLOW_DOCKER_PROJECT="${SHELFFLOW_DOCKER_PROJECT:-shelfflow}"
SHELFFLOW_INFRA_SERVICES="${SHELFFLOW_INFRA_SERVICES:-mysql redis rabbitmq}"
SHELFFLOW_INFRA_WAIT_TIMEOUT_SECONDS="${SHELFFLOW_INFRA_WAIT_TIMEOUT_SECONDS:-90}"

require_docker_compose() {
  if ! command -v docker >/dev/null 2>&1; then
    echo "[fail] 未找到 docker，请先安装并启动 Docker Desktop" >&2
    exit 1
  fi

  if ! docker compose version >/dev/null 2>&1; then
    echo "[fail] 当前 Docker 不支持 'docker compose' 命令" >&2
    exit 1
  fi
}

wait_for_container_health() {
  local service="$1"
  local container_id status attempt

  container_id="$(docker compose -f "${ROOT_DIR}/docker-compose.yml" -p "${SHELFFLOW_DOCKER_PROJECT}" ps -q "${service}")"
  if [[ -z "${container_id}" ]]; then
    echo "[fail] 未找到基础设施容器: ${service}" >&2
    return 1
  fi

  for attempt in $(seq 1 "${SHELFFLOW_INFRA_WAIT_TIMEOUT_SECONDS}"); do
    status="$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}running{{end}}' "${container_id}" 2>/dev/null || true)"
    if [[ "${status}" == "healthy" || "${status}" == "running" ]]; then
      echo "[ready] ${service}"
      return 0
    fi
    sleep 1
  done

  echo "[fail] ${service} 未在预期时间内就绪，当前状态: ${status:-unknown}" >&2
  return 1
}

if [[ -z "${DB_PASSWORD}" ]]; then
  echo "[fail] DB_PASSWORD 不能为空；请在 .env.local 中配置数据库密码" >&2
  exit 1
fi

require_docker_compose

if [[ "$#" -gt 0 ]]; then
  SERVICES=("$@")
else
  read -r -a SERVICES <<< "${SHELFFLOW_INFRA_SERVICES}"
fi

echo "[infra] project=${SHELFFLOW_DOCKER_PROJECT}"
echo "[infra] services=${SERVICES[*]}"
echo "[infra] mysql=${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo "[infra] redis=${REDIS_HOST}:${REDIS_PORT}"

docker compose -f "${ROOT_DIR}/docker-compose.yml" -p "${SHELFFLOW_DOCKER_PROJECT}" up -d "${SERVICES[@]}"

for service in "${SERVICES[@]}"; do
  wait_for_container_health "${service}"
done

echo "[infra] 基础设施已启动"
