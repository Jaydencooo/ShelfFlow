#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "${ROOT_DIR}/scripts/shelfflow-env.sh"

echo "Gateway: ${SHELFFLOW_GATEWAY_BASE_URL}"
echo "Auth Service: ${SHELFFLOW_AUTH_SERVICE_BASE_URL}"
echo "Admin Service: ${SHELFFLOW_ADMIN_SERVICE_BASE_URL}"
echo "User Service: ${SHELFFLOW_USER_SERVICE_BASE_URL}"
echo "Legacy Backend: ${SHELFFLOW_LEGACY_BACKEND_BASE_URL}"
echo "Maven repo: ${SHELFFLOW_MAVEN_REPO_LOCAL}"

echo "请分别在独立终端执行："
echo "  (cd services && mvn -Dmaven.repo.local=${SHELFFLOW_MAVEN_REPO_LOCAL} -DskipTests clean package)"
echo "  env APP_NAME=shelfflow-auth-service APP_PORT=${SHELFFLOW_AUTH_SERVICE_PORT} APP_ENV=${APP_ENV} LOG_LEVEL=${LOG_LEVEL} API_PREFIX=${API_PREFIX} JWT_SECRET=${JWT_SECRET} JWT_EXPIRES_IN=${JWT_EXPIRES_IN} CORS_ALLOWED_ORIGINS=${CORS_ALLOWED_ORIGINS} SHELFFLOW_LEGACY_BACKEND_BASE_URL=${SHELFFLOW_LEGACY_BACKEND_BASE_URL} java -jar /Users/coconut/Desktop/ShelfFlow/services/shelfflow-auth-service/target/shelfflow-auth-service-1.0.0-SNAPSHOT.jar"
echo "  env APP_NAME=shelfflow-admin-service APP_PORT=${SHELFFLOW_ADMIN_SERVICE_PORT} APP_ENV=${APP_ENV} LOG_LEVEL=${LOG_LEVEL} API_PREFIX=${API_PREFIX} JWT_SECRET=${JWT_SECRET} JWT_EXPIRES_IN=${JWT_EXPIRES_IN} CORS_ALLOWED_ORIGINS=${CORS_ALLOWED_ORIGINS} SHELFFLOW_LEGACY_BACKEND_BASE_URL=${SHELFFLOW_LEGACY_BACKEND_BASE_URL} java -jar /Users/coconut/Desktop/ShelfFlow/services/shelfflow-admin-service/target/shelfflow-admin-service-1.0.0-SNAPSHOT.jar"
echo "  env APP_NAME=shelfflow-user-service APP_PORT=${SHELFFLOW_USER_SERVICE_PORT} APP_ENV=${APP_ENV} LOG_LEVEL=${LOG_LEVEL} API_PREFIX=${API_PREFIX} JWT_SECRET=${JWT_SECRET} JWT_EXPIRES_IN=${JWT_EXPIRES_IN} CORS_ALLOWED_ORIGINS=${CORS_ALLOWED_ORIGINS} java -jar /Users/coconut/Desktop/ShelfFlow/services/shelfflow-user-service/target/shelfflow-user-service-1.0.0-SNAPSHOT.jar"
echo "  env APP_NAME=shelfflow-gateway APP_PORT=${SHELFFLOW_GATEWAY_PORT} APP_ENV=${APP_ENV} LOG_LEVEL=${LOG_LEVEL} API_PREFIX=${API_PREFIX} JWT_SECRET=${JWT_SECRET} JWT_EXPIRES_IN=${JWT_EXPIRES_IN} CORS_ALLOWED_ORIGINS=${CORS_ALLOWED_ORIGINS} AUTH_SERVICE_BASE_URL=${AUTH_SERVICE_BASE_URL} ADMIN_SERVICE_BASE_URL=${ADMIN_SERVICE_BASE_URL} USER_SERVICE_BASE_URL=${USER_SERVICE_BASE_URL} java -jar /Users/coconut/Desktop/ShelfFlow/services/shelfflow-gateway/target/shelfflow-gateway-1.0.0-SNAPSHOT.jar"
