#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "${ROOT_DIR}/scripts/shelfflow-env.sh"

LOG_DIR="${ROOT_DIR}/logs"
mkdir -p "${LOG_DIR}"
mkdir -p "${SHELFFLOW_MAVEN_REPO_LOCAL}"

START_USER_WEB="${START_USER_WEB:-false}"
SEED_USER_DEMO_DATA="${SEED_USER_DEMO_DATA:-true}"
WAIT_TIMEOUT_SECONDS="${WAIT_TIMEOUT_SECONDS:-60}"
START_INFRA="${START_INFRA:-false}"

PIDS=()
PID_FILES=()

cleanup_on_error() {
  local status=$?
  if [[ "${status}" -eq 0 ]]; then
    return
  fi

  for pid in "${PIDS[@]:-}"; do
    if kill -0 "${pid}" >/dev/null 2>&1; then
      kill "${pid}" >/dev/null 2>&1 || true
      wait "${pid}" >/dev/null 2>&1 || true
    fi
  done

  rm -f "${PID_FILES[@]:-}" >/dev/null 2>&1 || true
  exit "${status}"
}

trap cleanup_on_error EXIT INT TERM

wait_for_http() {
  local name="$1"
  local url="$2"
  local expected="${3:-200}"
  local attempt http_code

  for attempt in $(seq 1 "${WAIT_TIMEOUT_SECONDS}"); do
    http_code="$(curl -s -o /dev/null -w '%{http_code}' "${url}" || true)"
    if [[ "${http_code}" == "${expected}" ]]; then
      echo "[ready] ${name}: ${url}"
      return 0
    fi
    sleep 1
  done

  echo "[fail] ${name} 未在预期时间内就绪: ${url}" >&2
  return 1
}

free_port() {
  local port="$1"
  local pid

  pid="$(lsof -ti tcp:${port} || true)"
  if [[ -n "${pid}" ]]; then
    kill "${pid}" >/dev/null 2>&1 || true
    sleep 1
  fi
}

register_pid() {
  local name="$1"
  local pid="$2"
  local file="${LOG_DIR}/${name}.pid"

  echo "${pid}" > "${file}"
  PID_FILES+=("${file}")
  PIDS+=("${pid}")
}

echo "启动顺序：legacy backend -> auth-service -> admin-service -> user-service -> gateway -> admin web"
if [[ "${START_USER_WEB}" == "true" ]]; then
  echo "附加启动：user web"
fi
echo "日志目录: ${LOG_DIR}"
echo "Maven repo: ${SHELFFLOW_MAVEN_REPO_LOCAL}"
echo "启动超时: ${WAIT_TIMEOUT_SECONDS}s"

if [[ "${START_INFRA}" == "true" ]]; then
  echo "[infra] start"
  bash "${ROOT_DIR}/scripts/start-infra.sh"
fi

echo "[init] database"
bash "${ROOT_DIR}/scripts/init-db.sh"

echo "[build] java services"
(
  cd "${ROOT_DIR}/services"
  mvn -Dmaven.repo.local="${SHELFFLOW_MAVEN_REPO_LOCAL}" -DskipTests clean package \
    > "${LOG_DIR}/services-bootstrap.log" 2>&1
)

echo "[build] legacy backend"
(
  cd "${ROOT_DIR}/shelfflow-backend"
  mvn -Dmaven.repo.local="${SHELFFLOW_MAVEN_REPO_LOCAL}" -DskipTests clean package \
    > "${LOG_DIR}/legacy-bootstrap.log" 2>&1
)

for port in \
  "${SHELFFLOW_LEGACY_BACKEND_PORT}" \
  "${SHELFFLOW_AUTH_SERVICE_PORT}" \
  "${SHELFFLOW_ADMIN_SERVICE_PORT}" \
  "${SHELFFLOW_USER_SERVICE_PORT}" \
  "${SHELFFLOW_GATEWAY_PORT}" \
  "${SHELFFLOW_ADMIN_PORT}"; do
  free_port "${port}"
done

if [[ "${START_USER_WEB}" == "true" ]]; then
  free_port "${SHELFFLOW_USER_WEB_PORT}"
fi

echo "[start] legacy backend"
(
  cd "${ROOT_DIR}"
  nohup env \
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
    java -jar "${ROOT_DIR}/shelfflow-backend/shelfflow-server/target/shelfflow-server-1.0-SNAPSHOT.jar" \
    > "${LOG_DIR}/legacy-backend.log" 2>&1 &
  register_pid "legacy-backend" "$!"
)

echo "[start] auth service"
(
  cd "${ROOT_DIR}"
  nohup env \
    APP_NAME="shelfflow-auth-service" \
    APP_PORT="${SHELFFLOW_AUTH_SERVICE_PORT}" \
    APP_ENV="${APP_ENV}" \
    LOG_LEVEL="${LOG_LEVEL}" \
    API_PREFIX="${API_PREFIX}" \
    JWT_SECRET="${JWT_SECRET}" \
    JWT_EXPIRES_IN="${JWT_EXPIRES_IN}" \
    CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS}" \
    SHELFFLOW_AUTH_SERVICE_PORT="${SHELFFLOW_AUTH_SERVICE_PORT}" \
    SHELFFLOW_LEGACY_BACKEND_BASE_URL="${SHELFFLOW_LEGACY_BACKEND_BASE_URL}" \
    java -jar "${ROOT_DIR}/services/shelfflow-auth-service/target/shelfflow-auth-service-1.0.0-SNAPSHOT.jar" \
    > "${LOG_DIR}/auth-service.log" 2>&1 &
  register_pid "auth-service" "$!"
)

echo "[start] admin service"
(
  cd "${ROOT_DIR}"
  nohup env \
    APP_NAME="shelfflow-admin-service" \
    APP_PORT="${SHELFFLOW_ADMIN_SERVICE_PORT}" \
    APP_ENV="${APP_ENV}" \
    LOG_LEVEL="${LOG_LEVEL}" \
    API_PREFIX="${API_PREFIX}" \
    JWT_SECRET="${JWT_SECRET}" \
    JWT_EXPIRES_IN="${JWT_EXPIRES_IN}" \
    CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS}" \
    SHELFFLOW_ADMIN_SERVICE_PORT="${SHELFFLOW_ADMIN_SERVICE_PORT}" \
    SHELFFLOW_LEGACY_BACKEND_BASE_URL="${SHELFFLOW_LEGACY_BACKEND_BASE_URL}" \
    java -jar "${ROOT_DIR}/services/shelfflow-admin-service/target/shelfflow-admin-service-1.0.0-SNAPSHOT.jar" \
    > "${LOG_DIR}/admin-service.log" 2>&1 &
  register_pid "admin-service" "$!"
)

echo "[start] user service"
(
  cd "${ROOT_DIR}"
  nohup env \
    APP_NAME="shelfflow-user-service" \
    APP_PORT="${SHELFFLOW_USER_SERVICE_PORT}" \
    APP_ENV="${APP_ENV}" \
    LOG_LEVEL="${LOG_LEVEL}" \
    API_PREFIX="${API_PREFIX}" \
    JWT_SECRET="${JWT_SECRET}" \
    JWT_EXPIRES_IN="${JWT_EXPIRES_IN}" \
    CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS}" \
    SHELFFLOW_USER_SERVICE_PORT="${SHELFFLOW_USER_SERVICE_PORT}" \
    java -jar "${ROOT_DIR}/services/shelfflow-user-service/target/shelfflow-user-service-1.0.0-SNAPSHOT.jar" \
    > "${LOG_DIR}/user-service.log" 2>&1 &
  register_pid "user-service" "$!"
)

echo "[start] gateway"
(
  cd "${ROOT_DIR}"
  nohup env \
    APP_NAME="shelfflow-gateway" \
    APP_PORT="${SHELFFLOW_GATEWAY_PORT}" \
    APP_ENV="${APP_ENV}" \
    LOG_LEVEL="${LOG_LEVEL}" \
    API_PREFIX="${API_PREFIX}" \
    JWT_SECRET="${JWT_SECRET}" \
    JWT_EXPIRES_IN="${JWT_EXPIRES_IN}" \
    CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS}" \
    AUTH_SERVICE_BASE_URL="${AUTH_SERVICE_BASE_URL}" \
    ADMIN_SERVICE_BASE_URL="${ADMIN_SERVICE_BASE_URL}" \
    USER_SERVICE_BASE_URL="${USER_SERVICE_BASE_URL}" \
    SHELFFLOW_GATEWAY_PORT="${SHELFFLOW_GATEWAY_PORT}" \
    SHELFFLOW_AUTH_SERVICE_BASE_URL="${SHELFFLOW_AUTH_SERVICE_BASE_URL}" \
    SHELFFLOW_ADMIN_SERVICE_BASE_URL="${SHELFFLOW_ADMIN_SERVICE_BASE_URL}" \
    SHELFFLOW_USER_SERVICE_BASE_URL="${SHELFFLOW_USER_SERVICE_BASE_URL}" \
    java -jar "${ROOT_DIR}/services/shelfflow-gateway/target/shelfflow-gateway-1.0.0-SNAPSHOT.jar" \
    > "${LOG_DIR}/gateway.log" 2>&1 &
  register_pid "gateway" "$!"
)

echo "[start] admin web"
(
  cd "${ROOT_DIR}/apps/shelfflow-admin-web"
  nohup env \
    PORT="${SHELFFLOW_ADMIN_PORT}" \
    HOSTNAME="${SHELFFLOW_ADMIN_HOST}" \
    SHELFFLOW_GATEWAY_BASE_URL="${SHELFFLOW_GATEWAY_BASE_URL}" \
    npm run dev -- --hostname "${SHELFFLOW_ADMIN_HOST}" --port "${SHELFFLOW_ADMIN_PORT}" \
    > "${LOG_DIR}/admin-web.log" 2>&1 &
  register_pid "admin-web" "$!"
)

if [[ "${START_USER_WEB}" == "true" ]]; then
  echo "[start] user web"
  (
    cd "${ROOT_DIR}/apps/shelfflow-user-web"
    nohup env \
      PORT="${SHELFFLOW_USER_WEB_PORT}" \
      HOSTNAME="${SHELFFLOW_USER_WEB_HOST}" \
      SHELFFLOW_GATEWAY_BASE_URL="${SHELFFLOW_GATEWAY_BASE_URL}" \
      npm run dev -- --hostname "${SHELFFLOW_USER_WEB_HOST}" --port "${SHELFFLOW_USER_WEB_PORT}" \
      > "${LOG_DIR}/user-web.log" 2>&1 &
    register_pid "user-web" "$!"
  )
fi

wait_for_http "legacy backend" "${SHELFFLOW_LEGACY_BACKEND_BASE_URL}/doc.html" "200"
wait_for_http "auth service" "${SHELFFLOW_AUTH_SERVICE_BASE_URL}/health" "200"
wait_for_http "admin service" "${SHELFFLOW_ADMIN_SERVICE_BASE_URL}/health" "200"
wait_for_http "user service" "${SHELFFLOW_USER_SERVICE_BASE_URL}/health" "200"
wait_for_http "gateway" "${SHELFFLOW_GATEWAY_BASE_URL}/health" "200"
wait_for_http "admin web" "${SHELFFLOW_ADMIN_BASE_URL}/login" "200"

if [[ "${START_USER_WEB}" == "true" ]]; then
  wait_for_http "user web" "${SHELFFLOW_USER_WEB_BASE_URL}/products" "200"
fi

if [[ "${SEED_USER_DEMO_DATA}" == "true" ]]; then
  echo "[seed] user demo data"
  bash "${ROOT_DIR}/scripts/seed-user-demo-data.sh" "${SHELFFLOW_GATEWAY_BASE_URL}"
fi

trap - EXIT INT TERM

echo "启动完成。"
echo "管理端: ${SHELFFLOW_ADMIN_BASE_URL}"
echo "Gateway: ${SHELFFLOW_GATEWAY_BASE_URL}"
echo "Legacy backend: ${SHELFFLOW_LEGACY_BACKEND_BASE_URL}"
if [[ "${START_USER_WEB}" == "true" ]]; then
  echo "用户端: ${SHELFFLOW_USER_WEB_BASE_URL}"
fi
echo "停止时请执行: bash ${ROOT_DIR}/scripts/stop-local-all.sh"
