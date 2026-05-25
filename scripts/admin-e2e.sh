#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "${ROOT_DIR}/scripts/shelfflow-env.sh"

LOG_DIR="${ROOT_DIR}/logs/admin-e2e"
mkdir -p "${LOG_DIR}"
mkdir -p "${SHELFFLOW_MAVEN_REPO_LOCAL}"

KEEP_SERVICES_RUNNING="${KEEP_SERVICES_RUNNING:-false}"
START_USER_WEB="${START_USER_WEB:-false}"
RUN_USER_WEB_UI_SMOKE="${RUN_USER_WEB_UI_SMOKE:-false}"
RUN_ADMIN_WEB_UI_SMOKE="${RUN_ADMIN_WEB_UI_SMOKE:-false}"
SEED_USER_DEMO_DATA="${SEED_USER_DEMO_DATA:-true}"
START_INFRA="${START_INFRA:-false}"

if [[ "${START_INFRA}" == "true" ]]; then
  echo "[infra] start"
  bash "${ROOT_DIR}/scripts/start-infra.sh"
fi

echo "[init] database"
bash "${ROOT_DIR}/scripts/init-db.sh"

PIDS=()
PID_FILES=()

cleanup() {
  local status=$?
  if [[ "${status}" -eq 0 && "${KEEP_SERVICES_RUNNING}" == "true" ]]; then
    echo "[keep] 验收通过，服务保持运行。"
    echo "[keep] Gateway: ${SHELFFLOW_GATEWAY_BASE_URL}"
    echo "[keep] 管理端: ${SHELFFLOW_ADMIN_BASE_URL}"
    if [[ "${START_USER_WEB}" == "true" ]]; then
      echo "[keep] 用户端: ${SHELFFLOW_USER_WEB_BASE_URL}"
    fi
    echo "[keep] PID 文件目录: ${LOG_DIR}"
    exit 0
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

trap cleanup EXIT INT TERM

wait_for_http() {
  local name="$1"
  local url="$2"
  local expected="${3:-200}"
  local attempt http_code

  for attempt in $(seq 1 60); do
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

echo "[build] legacy backend"
(
  cd "${ROOT_DIR}/shelfflow-backend"
  mvn -Dmaven.repo.local="${SHELFFLOW_MAVEN_REPO_LOCAL}" -DskipTests clean package
)

echo "[build] java services"
(
  cd "${ROOT_DIR}/services"
  mvn -Dmaven.repo.local="${SHELFFLOW_MAVEN_REPO_LOCAL}" -DskipTests clean package
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

echo "[config] KEEP_SERVICES_RUNNING=${KEEP_SERVICES_RUNNING}"
echo "[config] START_USER_WEB=${START_USER_WEB}"
echo "[config] RUN_ADMIN_WEB_UI_SMOKE=${RUN_ADMIN_WEB_UI_SMOKE}"
echo "[config] RUN_USER_WEB_UI_SMOKE=${RUN_USER_WEB_UI_SMOKE}"

echo "[start] legacy backend"
bash -lc "exec java -jar '${ROOT_DIR}/shelfflow-backend/shelfflow-server/target/shelfflow-server-1.0-SNAPSHOT.jar' \
  --server.port=${SHELFFLOW_LEGACY_BACKEND_PORT} \
  \"--spring.datasource.druid.url=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true\" \
  --spring.datasource.druid.username=${DB_USER} \
  --spring.datasource.druid.password=${DB_PASSWORD} \
  --spring.redis.host=${REDIS_HOST} \
  --spring.redis.port=${REDIS_PORT} \
  --spring.redis.password=${REDIS_PASSWORD} \
  --spring.redis.database=${REDIS_DATABASE} \
  --JWT_SECRET=${JWT_SECRET}" > "${LOG_DIR}/legacy-backend.log" 2>&1 &
register_pid "legacy-backend" "$!"

echo "[start] auth service"
env \
  APP_NAME="shelfflow-auth-service" \
  APP_PORT="${SHELFFLOW_AUTH_SERVICE_PORT}" \
  APP_ENV="${APP_ENV}" \
  LOG_LEVEL="${LOG_LEVEL}" \
  API_PREFIX="${API_PREFIX}" \
  JWT_SECRET="${JWT_SECRET}" \
  JWT_EXPIRES_IN="${JWT_EXPIRES_IN}" \
  CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS}" \
  SHELFFLOW_LEGACY_BACKEND_BASE_URL="${SHELFFLOW_LEGACY_BACKEND_BASE_URL}" \
  java -jar "${ROOT_DIR}/services/shelfflow-auth-service/target/shelfflow-auth-service-1.0.0-SNAPSHOT.jar" \
  > "${LOG_DIR}/auth-service.log" 2>&1 &
register_pid "auth-service" "$!"

echo "[start] admin service"
env \
  APP_NAME="shelfflow-admin-service" \
  APP_PORT="${SHELFFLOW_ADMIN_SERVICE_PORT}" \
  APP_ENV="${APP_ENV}" \
  LOG_LEVEL="${LOG_LEVEL}" \
  API_PREFIX="${API_PREFIX}" \
  JWT_SECRET="${JWT_SECRET}" \
  JWT_EXPIRES_IN="${JWT_EXPIRES_IN}" \
  CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS}" \
  SHELFFLOW_LEGACY_BACKEND_BASE_URL="${SHELFFLOW_LEGACY_BACKEND_BASE_URL}" \
  java -jar "${ROOT_DIR}/services/shelfflow-admin-service/target/shelfflow-admin-service-1.0.0-SNAPSHOT.jar" \
  > "${LOG_DIR}/admin-service.log" 2>&1 &
register_pid "admin-service" "$!"

echo "[start] gateway"
env \
  APP_NAME="shelfflow-gateway" \
  APP_PORT="${SHELFFLOW_GATEWAY_PORT}" \
  APP_ENV="${APP_ENV}" \
  LOG_LEVEL="${LOG_LEVEL}" \
  API_PREFIX="${API_PREFIX}" \
  JWT_SECRET="${JWT_SECRET}" \
  JWT_EXPIRES_IN="${JWT_EXPIRES_IN}" \
  CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS}" \
  SHELFFLOW_AUTH_SERVICE_BASE_URL="${SHELFFLOW_AUTH_SERVICE_BASE_URL}" \
  SHELFFLOW_ADMIN_SERVICE_BASE_URL="${SHELFFLOW_ADMIN_SERVICE_BASE_URL}" \
  SHELFFLOW_USER_SERVICE_BASE_URL="${SHELFFLOW_USER_SERVICE_BASE_URL}" \
  java -jar "${ROOT_DIR}/services/shelfflow-gateway/target/shelfflow-gateway-1.0.0-SNAPSHOT.jar" \
  > "${LOG_DIR}/gateway.log" 2>&1 &
register_pid "gateway" "$!"

echo "[start] user service"
env \
  APP_NAME="shelfflow-user-service" \
  APP_PORT="${SHELFFLOW_USER_SERVICE_PORT}" \
  APP_ENV="${APP_ENV}" \
  LOG_LEVEL="${LOG_LEVEL}" \
  API_PREFIX="${API_PREFIX}" \
  JWT_SECRET="${JWT_SECRET}" \
  JWT_EXPIRES_IN="${JWT_EXPIRES_IN}" \
  CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS}" \
  java -jar "${ROOT_DIR}/services/shelfflow-user-service/target/shelfflow-user-service-1.0.0-SNAPSHOT.jar" \
  > "${LOG_DIR}/user-service.log" 2>&1 &
register_pid "user-service" "$!"

echo "[start] admin web"
(
  cd "${ROOT_DIR}/apps/shelfflow-admin-web"
  env \
    PORT="${SHELFFLOW_ADMIN_PORT}" \
    HOSTNAME="${SHELFFLOW_ADMIN_HOST}" \
    SHELFFLOW_GATEWAY_BASE_URL="${SHELFFLOW_GATEWAY_BASE_URL}" \
    npm run dev -- --hostname "${SHELFFLOW_ADMIN_HOST}" --port "${SHELFFLOW_ADMIN_PORT}"
) > "${LOG_DIR}/admin-web.log" 2>&1 &
register_pid "admin-web" "$!"

if [[ "${START_USER_WEB}" == "true" ]]; then
  echo "[start] user web"
  (
    cd "${ROOT_DIR}/apps/shelfflow-user-web"
    env \
      PORT="${SHELFFLOW_USER_WEB_PORT}" \
      HOSTNAME="${SHELFFLOW_USER_WEB_HOST}" \
      SHELFFLOW_GATEWAY_BASE_URL="${SHELFFLOW_GATEWAY_BASE_URL}" \
      npm run dev -- --hostname "${SHELFFLOW_USER_WEB_HOST}" --port "${SHELFFLOW_USER_WEB_PORT}"
  ) > "${LOG_DIR}/user-web.log" 2>&1 &
  register_pid "user-web" "$!"
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

echo "[smoke] backend-smoke.sh"
bash "${ROOT_DIR}/scripts/backend-smoke.sh"

echo "[smoke] order-lifecycle-smoke.sh"
bash "${ROOT_DIR}/scripts/order-lifecycle-smoke.sh"

if [[ "${RUN_ADMIN_WEB_UI_SMOKE}" == "true" ]]; then
  echo "[smoke] admin-web-ui-smoke.sh"
  bash "${ROOT_DIR}/scripts/admin-web-ui-smoke.sh"
fi

if [[ "${RUN_USER_WEB_UI_SMOKE}" == "true" ]]; then
  if [[ "${START_USER_WEB}" != "true" ]]; then
    echo "[fail] RUN_USER_WEB_UI_SMOKE=true 需要同时设置 START_USER_WEB=true" >&2
    exit 1
  fi
  echo "[smoke] user-web-ui-smoke.sh"
  bash "${ROOT_DIR}/scripts/user-web-ui-smoke.sh"
fi

echo "[done] admin e2e passed"
