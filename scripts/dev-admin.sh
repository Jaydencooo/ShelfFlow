#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "${ROOT_DIR}/scripts/shelfflow-env.sh"

export PORT="${SHELFFLOW_ADMIN_PORT}"
export HOSTNAME="${SHELFFLOW_ADMIN_HOST}"
export SHELFFLOW_GATEWAY_BASE_URL

echo "ShelfFlow 管理端: ${SHELFFLOW_ADMIN_BASE_URL}"
echo "ShelfFlow Gateway: ${SHELFFLOW_GATEWAY_BASE_URL}"

cd "${ROOT_DIR}/apps/shelfflow-admin-web"
npm run dev -- --hostname "${SHELFFLOW_ADMIN_HOST}" --port "${SHELFFLOW_ADMIN_PORT}"
