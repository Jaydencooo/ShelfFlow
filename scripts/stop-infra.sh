#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "${ROOT_DIR}/scripts/shelfflow-env.sh"

SHELFFLOW_DOCKER_PROJECT="${SHELFFLOW_DOCKER_PROJECT:-shelfflow}"
SHELFFLOW_INFRA_REMOVE_VOLUMES="${SHELFFLOW_INFRA_REMOVE_VOLUMES:-false}"

if ! command -v docker >/dev/null 2>&1; then
  echo "[fail] 未找到 docker，无法停止 Docker 基础设施" >&2
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "[fail] 当前 Docker 不支持 'docker compose' 命令" >&2
  exit 1
fi

COMPOSE_ARGS=(
  -f "${ROOT_DIR}/docker-compose.yml"
  -p "${SHELFFLOW_DOCKER_PROJECT}"
  down
)

if [[ "${SHELFFLOW_INFRA_REMOVE_VOLUMES}" == "true" ]]; then
  COMPOSE_ARGS+=(--volumes)
fi

docker compose "${COMPOSE_ARGS[@]}"

if [[ "${SHELFFLOW_INFRA_REMOVE_VOLUMES}" == "true" ]]; then
  echo "[infra] 基础设施已停止，并已删除数据卷"
else
  echo "[infra] 基础设施已停止，数据卷已保留"
fi
