#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "${ROOT_DIR}/scripts/shelfflow-env.sh"

export PORT="${SHELFFLOW_USER_WEB_PORT}"
export HOSTNAME="${SHELFFLOW_USER_WEB_HOST}"
export SHELFFLOW_GATEWAY_BASE_URL

echo "ShelfFlow 用户端: ${SHELFFLOW_USER_WEB_BASE_URL}"
echo "ShelfFlow Gateway: ${SHELFFLOW_GATEWAY_BASE_URL}"

cd "${ROOT_DIR}/apps/shelfflow-user-web"
npm run dev -- --hostname "${SHELFFLOW_USER_WEB_HOST}" --port "${SHELFFLOW_USER_WEB_PORT}"
