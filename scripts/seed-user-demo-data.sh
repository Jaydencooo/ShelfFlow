#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "${ROOT_DIR}/scripts/shelfflow-env.sh"

BASE_URL="${1:-${BASE_URL:-${SHELFFLOW_GATEWAY_BASE_URL}}}"
CURL_MAX_TIME="${CURL_MAX_TIME:-10}"

DEMO_USER_1_OPEN_ID="${DEMO_USER_1_OPEN_ID:-sf-user-demo-01}"
DEMO_USER_1_NAME="${DEMO_USER_1_NAME:-陈晓}"
DEMO_USER_1_PHONE="${DEMO_USER_1_PHONE:-13810000001}"

DEMO_USER_2_OPEN_ID="${DEMO_USER_2_OPEN_ID:-sf-user-demo-02}"
DEMO_USER_2_NAME="${DEMO_USER_2_NAME:-林悦}"
DEMO_USER_2_PHONE="${DEMO_USER_2_PHONE:-13810000002}"

DEMO_USERS=(
  "${DEMO_USER_1_OPEN_ID}|${DEMO_USER_1_NAME}|${DEMO_USER_1_PHONE}|${SHELFFLOW_USER_SMOKE_PASSWORD}"
  "${DEMO_USER_2_OPEN_ID}|${DEMO_USER_2_NAME}|${DEMO_USER_2_PHONE}|${SHELFFLOW_USER_SMOKE_PASSWORD}"
  "${SHELFFLOW_USER_SMOKE_OPEN_ID}|${SHELFFLOW_USER_SMOKE_NAME}|${SHELFFLOW_USER_SMOKE_PHONE}|${SHELFFLOW_USER_SMOKE_PASSWORD}"
)

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT

request() {
  local method="$1"
  local path="$2"
  local body="${3:-}"
  local output="${TMP_DIR}/response.json"
  local status
  local args=(-sS --max-time "${CURL_MAX_TIME}" -o "${output}" -w "%{http_code}" -X "${method}")

  if [[ -n "${body}" ]]; then
    args+=(-H "Content-Type: application/json" -d "${body}")
  fi

  status="$(curl "${args[@]}" "${BASE_URL}${path}")"
  cat "${output}"
  printf '\n%s' "${status}"
}

seed_user() {
  local open_id="$1"
  local name="$2"
  local phone="$3"
  local password="$4"
  local response
  local status
  local body

  response="$(request POST "/api/user/auth/register" "{\"openId\":\"${open_id}\",\"name\":\"${name}\",\"phone\":\"${phone}\",\"password\":\"${password}\"}")"
  body="$(printf '%s' "${response}" | sed '$d')"
  status="$(printf '%s' "${response}" | tail -n 1)"

  if [[ "${status}" =~ ^2[0-9][0-9]$ ]]; then
    echo "[seed] 已创建用户 ${open_id} (${name})"
    return 0
  fi

  if printf '%s' "${body}" | grep -q '"code":"conflict"'; then
    response="$(request POST "/api/user/auth/password/reset" "{\"openId\":\"${open_id}\",\"phone\":\"${phone}\",\"newPassword\":\"${password}\"}")"
    body="$(printf '%s' "${response}" | sed '$d')"
    status="$(printf '%s' "${response}" | tail -n 1)"
    if [[ "${status}" =~ ^2[0-9][0-9]$ ]]; then
      echo "[seed] 已重置已有用户 ${open_id} 的密码"
      return 0
    fi
  fi

  echo "${body}" >&2
  echo "[seed] 用户数据写入失败: ${open_id}" >&2
  exit 1
}

for demo_user in "${DEMO_USERS[@]}"; do
  IFS='|' read -r open_id name phone password <<< "${demo_user}"
  seed_user "${open_id}" "${name}" "${phone}" "${password}"
done

echo "[seed] 用户端演示账号已准备完成"
