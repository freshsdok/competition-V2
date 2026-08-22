#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"
ENV_FILE="${REVIEW_UAT_ENV_FILE:-${SCRIPT_DIR}/review_uat.env}"

if [[ -f "${ENV_FILE}" ]]; then
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
fi

LOG_DIR="${LOG_DIR:-${ROOT_DIR}/logs/review-uat}"
SERVICES=(gateway auth system competition admin)

stop_one() {
  local service="$1"
  local pid_file="${LOG_DIR}/${service}.pid"
  if [[ ! -f "${pid_file}" ]]; then
    echo "[SKIP] ${service}: no pid file"
    rm -f "${LOG_DIR}/${service}.started"
    return
  fi

  local pid
  pid="$(cat "${pid_file}" 2>/dev/null || true)"
  if [[ -z "${pid}" ]]; then
    echo "[WARN] ${service}: empty pid file"
    rm -f "${pid_file}"
    rm -f "${LOG_DIR}/${service}.started"
    return
  fi

  if ! kill -0 "${pid}" >/dev/null 2>&1; then
    echo "[OK] ${service}: pid ${pid} is not running"
    rm -f "${pid_file}"
    rm -f "${LOG_DIR}/${service}.started"
    return
  fi

  echo "[STOP] ${service}: sending TERM to pid ${pid}"
  kill "${pid}" >/dev/null 2>&1 || true

  for _ in {1..20}; do
    if ! kill -0 "${pid}" >/dev/null 2>&1; then
      echo "[OK] ${service}: stopped"
      rm -f "${pid_file}"
      rm -f "${LOG_DIR}/${service}.started"
      return
    fi
    sleep 1
  done

  echo "[WARN] ${service}: still running after TERM, sending KILL to pid ${pid}"
  kill -9 "${pid}" >/dev/null 2>&1 || true
  rm -f "${pid_file}"
  rm -f "${LOG_DIR}/${service}.started"
}

echo "Review UAT stop"
echo "Log dir: ${LOG_DIR}"
for service in "${SERVICES[@]}"; do
  stop_one "${service}"
done
