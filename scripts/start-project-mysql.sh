#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "${ROOT_DIR}/scripts/shelfflow-env.sh"

MYSQL_BASE_DIR="${MYSQL_BASE_DIR:-/opt/homebrew/opt/mysql}"
MYSQLD_BIN="${MYSQLD_BIN:-${MYSQL_BASE_DIR}/bin/mysqld}"
MYSQL_BIN="${MYSQL_BIN:-${MYSQL_BASE_DIR}/bin/mysql}"
MYSQLADMIN_BIN="${MYSQLADMIN_BIN:-${MYSQL_BASE_DIR}/bin/mysqladmin}"
MYSQL_DATA_DIR="${MYSQL_DATA_DIR:-/opt/homebrew/var/mysql}"
MYSQL_RUN_DIR="${MYSQL_RUN_DIR:-${ROOT_DIR}/.local/mysql/run}"
MYSQL_SOCKET="${MYSQL_SOCKET:-${MYSQL_RUN_DIR}/mysql.sock}"
MYSQL_PID_FILE="${MYSQL_PID_FILE:-${MYSQL_RUN_DIR}/mysql.pid}"
MYSQL_ERROR_LOG="${MYSQL_ERROR_LOG:-${ROOT_DIR}/logs/project-mysql.err}"
MYSQL_READY_TIMEOUT_SECONDS="${MYSQL_READY_TIMEOUT_SECONDS:-45}"
PASSWORD_MARKER_FILE="${MYSQL_DATA_DIR}/.shelfflow-password-set"

require_file() {
  local file_path="$1"
  local description="$2"

  if [[ ! -x "${file_path}" ]]; then
    echo "[fail] ${description} 不存在或不可执行: ${file_path}" >&2
    exit 1
  fi
}

sql_escape() {
  local value="$1"
  printf "%s" "${value//\'/\'\'}"
}

is_mysql_running() {
  "${MYSQLADMIN_BIN}" \
    --protocol=TCP \
    -h "${DB_HOST}" \
    -P "${DB_PORT}" \
    -u "${DB_USER}" \
    -p"${DB_PASSWORD}" \
    ping >/dev/null 2>&1
}

wait_for_mysql() {
  local attempt

  for attempt in $(seq 1 "${MYSQL_READY_TIMEOUT_SECONDS}"); do
    if [[ -S "${MYSQL_SOCKET}" ]]; then
      if "${MYSQLADMIN_BIN}" --protocol=SOCKET --socket="${MYSQL_SOCKET}" -uroot ping >/dev/null 2>&1; then
        return 0
      fi
      if "${MYSQLADMIN_BIN}" --protocol=SOCKET --socket="${MYSQL_SOCKET}" -uroot -p"${DB_PASSWORD}" ping >/dev/null 2>&1; then
        return 0
      fi
    fi
    sleep 1
  done

  echo "[fail] project MySQL 未在 ${MYSQL_READY_TIMEOUT_SECONDS}s 内就绪" >&2
  echo "[hint] 查看日志: ${MYSQL_ERROR_LOG}" >&2
  exit 1
}

initialize_data_dir() {
  if [[ -d "${MYSQL_DATA_DIR}/mysql" ]]; then
    return 0
  fi

  echo "[mysql] initialize data dir: ${MYSQL_DATA_DIR}"
  mkdir -p "${MYSQL_DATA_DIR}"
  "${MYSQLD_BIN}" \
    --initialize-insecure \
    --basedir="${MYSQL_BASE_DIR}" \
    --datadir="${MYSQL_DATA_DIR}" \
    --log-error="${MYSQL_ERROR_LOG}"
}

start_mysql_daemon() {
  if is_mysql_running; then
    echo "[ready] project MySQL 已在 ${DB_HOST}:${DB_PORT} 运行"
    return 0
  fi

  if lsof -ti tcp:"${DB_PORT}" >/dev/null 2>&1; then
    echo "[fail] DB_PORT=${DB_PORT} 已被其他进程占用，请修改 .env.local 或停止占用进程" >&2
    exit 1
  fi

  mkdir -p "${MYSQL_RUN_DIR}" "$(dirname "${MYSQL_ERROR_LOG}")"
  echo "[mysql] start: ${DB_HOST}:${DB_PORT}"
  "${MYSQLD_BIN}" \
    --basedir="${MYSQL_BASE_DIR}" \
    --datadir="${MYSQL_DATA_DIR}" \
    --port="${DB_PORT}" \
    --bind-address="${DB_HOST}" \
    --socket="${MYSQL_SOCKET}" \
    --pid-file="${MYSQL_PID_FILE}" \
    --log-error="${MYSQL_ERROR_LOG}" \
    --skip-mysqlx \
    --character-set-server=utf8mb4 \
    --collation-server=utf8mb4_unicode_ci \
    --daemonize
  wait_for_mysql
}

configure_root_password() {
  if is_mysql_running; then
    touch "${PASSWORD_MARKER_FILE}" 2>/dev/null || true
    return 0
  fi

  if [[ -f "${PASSWORD_MARKER_FILE}" ]]; then
    return 0
  fi

  local escaped_password
  escaped_password="$(sql_escape "${DB_PASSWORD}")"

  echo "[mysql] configure root password"
  "${MYSQL_BIN}" --protocol=SOCKET --socket="${MYSQL_SOCKET}" -uroot <<SQL
ALTER USER 'root'@'localhost' IDENTIFIED BY '${escaped_password}';
CREATE USER IF NOT EXISTS 'root'@'127.0.0.1' IDENTIFIED BY '${escaped_password}';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'127.0.0.1' WITH GRANT OPTION;
FLUSH PRIVILEGES;
SQL
  touch "${PASSWORD_MARKER_FILE}"
}

verify_connection() {
  "${MYSQL_BIN}" \
    --protocol=TCP \
    -h "${DB_HOST}" \
    -P "${DB_PORT}" \
    -u "${DB_USER}" \
    -p"${DB_PASSWORD}" \
    -e "SELECT VERSION();" >/dev/null
  echo "[ready] project MySQL: ${DB_HOST}:${DB_PORT}"
}

require_file "${MYSQLD_BIN}" "mysqld"
require_file "${MYSQL_BIN}" "mysql"
require_file "${MYSQLADMIN_BIN}" "mysqladmin"

if [[ -z "${DB_PASSWORD}" ]]; then
  echo "[fail] DB_PASSWORD 不能为空，请在 .env.local 配置" >&2
  exit 1
fi

initialize_data_dir
start_mysql_daemon
configure_root_password
verify_connection

echo "Navicat 连接信息:"
echo "  Host: ${DB_HOST}"
echo "  Port: ${DB_PORT}"
echo "  User: ${DB_USER}"
echo "  Password: ${DB_PASSWORD}"
