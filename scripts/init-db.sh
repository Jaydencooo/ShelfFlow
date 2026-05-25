#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "${ROOT_DIR}/scripts/shelfflow-env.sh"

if [[ "$(uname -s)" == "Darwin" && "${LC_ALL:-}" == "C.UTF-8" ]]; then
  export LC_ALL="en_US.UTF-8"
fi
if [[ "$(uname -s)" == "Darwin" && "${LANG:-}" == "C.UTF-8" ]]; then
  export LANG="en_US.UTF-8"
fi

MYSQL_ARGS=(
  -h"${DB_HOST}"
  -P"${DB_PORT}"
  -u"${DB_USER}"
)

if [[ -n "${DB_PASSWORD}" ]]; then
  MYSQL_ARGS+=(-p"${DB_PASSWORD}")
fi

FORCE_RESET="${1:-}"
MIGRATION_TABLE="schema_migration"
BASE_SCHEMA_IMPORTED="false"

require_mysql() {
  if ! command -v mysql >/dev/null 2>&1; then
    echo "[fail] 未找到 mysql 客户端" >&2
    exit 1
  fi
}

require_checksum_tool() {
  if command -v shasum >/dev/null 2>&1; then
    return 0
  fi
  echo "[fail] 未找到 shasum，无法记录迁移校验值" >&2
  exit 1
}

table_exists() {
  local table_name="$1"
  mysql "${MYSQL_ARGS[@]}" -N -B -e \
    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${DB_NAME}' AND table_name='${table_name}';"
}

check_database_connection() {
  if ! mysql "${MYSQL_ARGS[@]}" -e "SELECT 1;" >/dev/null 2>&1; then
    echo "[fail] 无法连接 MySQL: ${DB_HOST}:${DB_PORT}" >&2
    echo "[hint] 请确认 MySQL 已启动，并检查 .env.local 中的 DB_HOST/DB_PORT/DB_USER/DB_PASSWORD" >&2
    exit 1
  fi
}

require_mysql
require_checksum_tool
check_database_connection

apply_sql_file() {
  local sql_file="$1"
  echo "[db] apply ${sql_file}"
  mysql "${MYSQL_ARGS[@]}" "${DB_NAME}" < "${sql_file}"
}

ensure_migration_table() {
  mysql "${MYSQL_ARGS[@]}" "${DB_NAME}" -e "
    CREATE TABLE IF NOT EXISTS \`${MIGRATION_TABLE}\` (
      id BIGINT NOT NULL AUTO_INCREMENT,
      filename VARCHAR(255) NOT NULL,
      checksum VARCHAR(64) NOT NULL,
      applied_at DATETIME NOT NULL,
      PRIMARY KEY (id),
      UNIQUE KEY uq_schema_migration_filename (filename)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;"
}

migration_applied() {
  local filename="$1"
  mysql "${MYSQL_ARGS[@]}" "${DB_NAME}" -N -B -e \
    "SELECT COUNT(*) FROM \`${MIGRATION_TABLE}\` WHERE filename='${filename}';"
}

migration_checksum() {
  local filename="$1"
  mysql "${MYSQL_ARGS[@]}" "${DB_NAME}" -N -B -e \
    "SELECT checksum FROM \`${MIGRATION_TABLE}\` WHERE filename='${filename}' LIMIT 1;"
}

record_migration() {
  local filename="$1"
  local checksum="$2"
  mysql "${MYSQL_ARGS[@]}" "${DB_NAME}" -e \
    "INSERT INTO \`${MIGRATION_TABLE}\` (filename, checksum, applied_at) VALUES ('${filename}', '${checksum}', NOW());"
}

apply_migration_once() {
  local migration_file="$1"
  local filename checksum applied recorded_checksum

  filename="$(basename "${migration_file}")"
  checksum="$(shasum -a 256 "${migration_file}" | awk '{print $1}')"
  applied="$(migration_applied "${filename}")"

  if [[ "${applied}" == "1" ]]; then
    recorded_checksum="$(migration_checksum "${filename}")"
    if [[ "${recorded_checksum}" != "${checksum}" ]]; then
      echo "[fail] migration checksum changed: ${filename}" >&2
      echo "[fail] recorded=${recorded_checksum} current=${checksum}" >&2
      exit 1
    fi
    echo "[db] skip migration ${filename}"
    return 0
  fi

  apply_sql_file "${migration_file}"
  record_migration "${filename}" "${checksum}"
}

echo "[db] ensure database ${DB_NAME}"
mysql "${MYSQL_ARGS[@]}" -e "CREATE DATABASE IF NOT EXISTS \`${DB_NAME}\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

staff_exists="$(table_exists "staff")"
product_exists="$(table_exists "product")"
batch_exists="$(table_exists "inventory_batch")"

if [[ "${FORCE_RESET}" != "--force" && "${staff_exists}" == "1" && "${product_exists}" == "1" && "${batch_exists}" == "1" ]]; then
  echo "[db] schema already initialized, skip base import"
else
  apply_sql_file "${ROOT_DIR}/shelfflow-backend/mysql.sql"
  BASE_SCHEMA_IMPORTED="true"
fi

if [[ "${BASE_SCHEMA_IMPORTED}" == "true" ]]; then
  mysql "${MYSQL_ARGS[@]}" "${DB_NAME}" -e "DROP TABLE IF EXISTS \`${MIGRATION_TABLE}\`;"
fi
ensure_migration_table

MIGRATIONS_DIR="${ROOT_DIR}/shelfflow-backend/docs/migrations"
if [[ -d "${MIGRATIONS_DIR}" ]]; then
  while IFS= read -r migration_file; do
    apply_migration_once "${migration_file}"
  done < <(find "${MIGRATIONS_DIR}" -maxdepth 1 -type f -name '*.sql' | sort)
fi

echo "[db] schema ready"
