#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"
ENV_FILE="${REVIEW_UAT_ENV_FILE:-${SCRIPT_DIR}/review_uat.env}"

if [[ -f "${ENV_FILE}" ]]; then
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
fi

GATEWAY_PORT="${GATEWAY_PORT:-9889}"
AUTH_PORT="${AUTH_PORT:-9224}"
SYSTEM_PORT="${SYSTEM_PORT:-9211}"
COMPETITION_PORT="${COMPETITION_PORT:-9205}"
ADMIN_PORT="${ADMIN_PORT:-8081}"
CHECK_HOST="${CHECK_HOST:-127.0.0.1}"
ADMIN_HOST="${ADMIN_HOST:-127.0.0.1}"
ADMIN_BIND_HOST="${ADMIN_BIND_HOST:-0.0.0.0}"
SPRING_PROFILE="${SPRING_PROFILE:-test}"
NACOS_HOST="${NACOS_HOST:-127.0.0.1}"
NACOS_PORT="${NACOS_PORT:-8848}"
LOG_DIR="${LOG_DIR:-${ROOT_DIR}/logs/review-uat}"
STARTUP_WAIT_SECONDS="${STARTUP_WAIT_SECONDS:-120}"
PYTHON_BIN="${PYTHON_BIN:-python3}"
MAVEN_PREBUILD="${MAVEN_PREBUILD:-1}"

START_GATEWAY="${START_GATEWAY:-1}"
START_AUTH="${START_AUTH:-1}"
START_SYSTEM="${START_SYSTEM:-1}"
START_COMPETITION="${START_COMPETITION:-1}"
START_ADMIN="${START_ADMIN:-1}"

mkdir -p "${LOG_DIR}"

print_kv() {
  printf '%-18s %s\n' "$1" "$2"
}

port_lsof() {
  local port="$1"
  lsof -nP -iTCP:"${port}" -sTCP:LISTEN 2>/dev/null || true
}

listener_pid() {
  local port="$1"
  lsof -nP -iTCP:"${port}" -sTCP:LISTEN -t 2>/dev/null | head -1 || true
}

port_is_open() {
  local host="$1"
  local port="$2"
  "${PYTHON_BIN}" - "$host" "$port" <<'PY' >/dev/null 2>&1
import socket
import sys
host = sys.argv[1]
port = int(sys.argv[2])
try:
    with socket.create_connection((host, port), timeout=1.5):
        pass
    sys.exit(0)
except Exception:
    sys.exit(1)
PY
}

pid_is_running() {
  local pid_file="$1"
  [[ -f "${pid_file}" ]] || return 1
  local pid
  pid="$(cat "${pid_file}" 2>/dev/null || true)"
  [[ -n "${pid}" ]] || return 1
  kill -0 "${pid}" >/dev/null 2>&1
}

show_port_owner() {
  local port="$1"
  local lsof_output
  lsof_output="$(port_lsof "${port}")"
  if [[ -z "${lsof_output}" ]]; then
    echo "no listener"
    return
  fi
  echo "${lsof_output}"
  echo "${lsof_output}" | awk 'NR>1 {print $2}' | sort -u | while read -r pid; do
    [[ -n "${pid}" ]] || continue
    ps -p "${pid}" -o pid=,command= 2>/dev/null || true
  done
}

ensure_port_available_or_owned() {
  local service="$1"
  local host="$2"
  local port="$3"
  local pid_file="${LOG_DIR}/${service}.pid"
  if pid_is_running "${pid_file}"; then
    echo "[INFO] ${service} already started by this script, pid $(cat "${pid_file}")"
    return 1
  fi
  if port_is_open "${host}" "${port}"; then
    echo "[WARN] ${service} port ${host}:${port} is already listening. Not starting a duplicate process."
    show_port_owner "${port}"
    return 1
  fi
  return 0
}

spring_run_arguments() {
  local port="$1"
  printf -- '--server.port=%s --spring.cloud.nacos.discovery.server-addr=%s:%s --spring.cloud.nacos.config.server-addr=%s:%s' \
    "${port}" "${NACOS_HOST}" "${NACOS_PORT}" "${NACOS_HOST}" "${NACOS_PORT}"
}

start_java_service() {
  local service="$1"
  local module="$2"
  local port="$3"
  local enabled="$4"
  local pid_file="${LOG_DIR}/${service}.pid"
  local log_file="${LOG_DIR}/${service}.log"

  if [[ "${enabled}" != "1" ]]; then
    echo "[SKIP] ${service} disabled by environment"
    return
  fi
  if ! ensure_port_available_or_owned "${service}" "${CHECK_HOST}" "${port}"; then
    return
  fi

  echo "[START] ${service} (${module}) on port ${port}"
  (
    cd "${ROOT_DIR}/old-code"
    nohup bash -c '
      set -e
      module="$1"
      profile="$2"
      run_args="$3"
      prebuild="$4"
      if [[ "${prebuild}" == "1" ]]; then
        echo "[BUILD] mvn -pl ${module} -am -Dmaven.test.skip=true install"
        mvn -pl "${module}" -am -Dmaven.test.skip=true install
      fi
      echo "[RUN] mvn -pl ${module} spring-boot:run"
      exec mvn -pl "${module}" spring-boot:run -Dmaven.test.skip=true \
        "-Dspring-boot.run.profiles=${profile}" \
        "-Dspring-boot.run.arguments=${run_args}"
    ' bash "${module}" "${SPRING_PROFILE}" "$(spring_run_arguments "${port}")" "${MAVEN_PREBUILD}" \
      > "${log_file}" 2>&1 &
    echo $! > "${pid_file}"
  )
  echo "[INFO] ${service} pid $(cat "${pid_file}"), log ${log_file}"
  touch "${LOG_DIR}/${service}.started"
}

start_admin_service() {
  local service="admin"
  local pid_file="${LOG_DIR}/${service}.pid"
  local log_file="${LOG_DIR}/${service}.log"

  if [[ "${START_ADMIN}" != "1" ]]; then
    echo "[SKIP] admin disabled by environment"
    return
  fi
  if ! ensure_port_available_or_owned "${service}" "${ADMIN_HOST}" "${ADMIN_PORT}"; then
    return
  fi

  echo "[START] admin (old-code-admin) on port ${ADMIN_PORT}"
  (
    cd "${ROOT_DIR}/old-code-admin"
    nohup npm run dev -- --host "${ADMIN_BIND_HOST}" --port "${ADMIN_PORT}" > "${log_file}" 2>&1 &
    echo $! > "${pid_file}"
  )
  echo "[INFO] admin pid $(cat "${pid_file}"), log ${log_file}"
  touch "${LOG_DIR}/${service}.started"
}

refresh_pid_file() {
  local service="$1"
  local port="$2"
  local marker="${LOG_DIR}/${service}.started"
  local pid_file="${LOG_DIR}/${service}.pid"
  [[ -f "${marker}" ]] || return
  local actual_pid
  actual_pid="$(listener_pid "${port}")"
  if [[ -n "${actual_pid}" ]]; then
    echo "${actual_pid}" > "${pid_file}"
    echo "[INFO] ${service} listener pid is ${actual_pid}"
  else
    echo "[WARN] ${service} has no listener pid on port ${port}"
  fi
}

refresh_started_pid_files() {
  [[ "${START_GATEWAY}" != "1" ]] || refresh_pid_file "gateway" "${GATEWAY_PORT}"
  [[ "${START_AUTH}" != "1" ]] || refresh_pid_file "auth" "${AUTH_PORT}"
  [[ "${START_SYSTEM}" != "1" ]] || refresh_pid_file "system" "${SYSTEM_PORT}"
  [[ "${START_COMPETITION}" != "1" ]] || refresh_pid_file "competition" "${COMPETITION_PORT}"
  [[ "${START_ADMIN}" != "1" ]] || refresh_pid_file "admin" "${ADMIN_PORT}"
}

wait_for_ports() {
  local deadline=$((SECONDS + STARTUP_WAIT_SECONDS))
  local pending
  while (( SECONDS < deadline )); do
    pending=0
    [[ "${START_GATEWAY}" != "1" ]] || port_is_open "${CHECK_HOST}" "${GATEWAY_PORT}" || pending=1
    [[ "${START_AUTH}" != "1" ]] || port_is_open "${CHECK_HOST}" "${AUTH_PORT}" || pending=1
    [[ "${START_SYSTEM}" != "1" ]] || port_is_open "${CHECK_HOST}" "${SYSTEM_PORT}" || pending=1
    [[ "${START_COMPETITION}" != "1" ]] || port_is_open "${CHECK_HOST}" "${COMPETITION_PORT}" || pending=1
    [[ "${START_ADMIN}" != "1" ]] || port_is_open "${ADMIN_HOST}" "${ADMIN_PORT}" || pending=1
    if [[ "${pending}" == "0" ]]; then
      echo "[INFO] Enabled service ports are listening."
      return
    fi
    sleep 3
  done
  echo "[WARN] Startup wait timed out after ${STARTUP_WAIT_SECONDS}s. Health check will show details."
}

echo "Review UAT startup"
print_kv "Root" "${ROOT_DIR}"
print_kv "Log dir" "${LOG_DIR}"
print_kv "Spring profile" "${SPRING_PROFILE}"
print_kv "Nacos" "${NACOS_HOST}:${NACOS_PORT}"
print_kv "Gateway" "${GATEWAY_PORT}"
print_kv "Auth" "${AUTH_PORT}"
print_kv "System" "${SYSTEM_PORT}"
print_kv "Competition" "${COMPETITION_PORT}"
print_kv "Admin" "${ADMIN_PORT}"

start_java_service "gateway" "teaching-gateway" "${GATEWAY_PORT}" "${START_GATEWAY}"
start_java_service "auth" "teaching-auth" "${AUTH_PORT}" "${START_AUTH}"
start_java_service "system" "teaching-modules/teaching-system" "${SYSTEM_PORT}" "${START_SYSTEM}"
start_java_service "competition" "teaching-modules/teaching-competition" "${COMPETITION_PORT}" "${START_COMPETITION}"
start_admin_service

wait_for_ports
refresh_started_pid_files

echo "[CHECK] Running health check"
"${PYTHON_BIN}" "${SCRIPT_DIR}/check_review_uat.py" || true

echo
echo "Review UAT URLs:"
echo "  Admin:   http://${ADMIN_HOST}:${ADMIN_PORT}"
echo "  Gateway: http://${CHECK_HOST}:${GATEWAY_PORT}"
echo "  Logs:    ${LOG_DIR}"
