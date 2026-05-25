#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "${ROOT_DIR}/scripts/shelfflow-env.sh"

MYSQL_BASE_DIR="${MYSQL_BASE_DIR:-/opt/homebrew/opt/mysql}"
MYSQLADMIN_BIN="${MYSQLADMIN_BIN:-${MYSQL_BASE_DIR}/bin/mysqladmin}"
MYSQL_RUN_DIR="${MYSQL_RUN_DIR:-${ROOT_DIR}/.local/mysql/run}"
MYSQL_SOCKET="${MYSQL_SOCKET:-${MYSQL_RUN_DIR}/mysql.sock}"

if [[ ! -x "${MYSQLADMIN_BIN}" ]]; then
  echo "[fail] mysqladmin 不存在或不可执行: ${MYSQLADMIN_BIN}" >&2
  exit 1
fi

if "${MYSQLADMIN_BIN}" --protocol=TCP -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${DB_PASSWORD}" ping >/dev/null 2>&1; then
  "${MYSQLADMIN_BIN}" --protocol=TCP -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${DB_PASSWORD}" shutdown
  echo "[stop] project MySQL stopped: ${DB_HOST}:${DB_PORT}"
  exit 0
fi

if [[ -S "${MYSQL_SOCKET}" ]] && "${MYSQLADMIN_BIN}" --protocol=SOCKET --socket="${MYSQL_SOCKET}" -uroot ping >/dev/null 2>&1; then
  "${MYSQLADMIN_BIN}" --protocol=SOCKET --socket="${MYSQL_SOCKET}" -uroot shutdown
  echo "[stop] project MySQL stopped via socket"
  exit 0
fi

echo "[skip] project MySQL 未运行"
