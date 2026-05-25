#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "${ROOT_DIR}/scripts/shelfflow-env.sh"

echo "ShelfFlow legacy backend: ${SHELFFLOW_LEGACY_BACKEND_BASE_URL}"
mkdir -p "${SHELFFLOW_MAVEN_REPO_LOCAL}"

cd "${ROOT_DIR}/shelfflow-backend"
mvn -Dmaven.repo.local="${SHELFFLOW_MAVEN_REPO_LOCAL}" -DskipTests clean package

cd "${ROOT_DIR}"
env \
  SHELFFLOW_BACKEND_PORT="${SHELFFLOW_LEGACY_BACKEND_PORT}" \
  SHELFFLOW_MYSQL_HOST="${DB_HOST}" \
  SHELFFLOW_MYSQL_PORT="${DB_PORT}" \
  DB_NAME="${DB_NAME}" \
  DB_USER="${DB_USER}" \
  DB_PASSWORD="${DB_PASSWORD}" \
  JWT_SECRET="${JWT_SECRET}" \
  SHELFFLOW_REDIS_HOST="${REDIS_HOST}" \
  SHELFFLOW_REDIS_PORT="${REDIS_PORT}" \
  REDIS_PASSWORD="${REDIS_PASSWORD}" \
  REDIS_DATABASE="${REDIS_DATABASE}" \
  java -jar "${ROOT_DIR}/shelfflow-backend/shelfflow-server/target/shelfflow-server-1.0-SNAPSHOT.jar"
