#!/usr/bin/env bash
set -euo pipefail

export PATH="/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:${PATH:-}"

NACOS_CONTAINER="${NACOS_CONTAINER:-nacos-standalone}"
DOCKER_WAIT_SECONDS="${DOCKER_WAIT_SECONDS:-90}"

log() {
  printf '[%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$*"
}

need_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    log "Missing command: $1"
    exit 1
  fi
}

wait_for_docker() {
  local elapsed=0
  until docker info >/dev/null 2>&1; do
    if (( elapsed >= DOCKER_WAIT_SECONDS )); then
      log "Docker is still not ready after ${DOCKER_WAIT_SECONDS}s."
      exit 1
    fi
    sleep 2
    elapsed=$((elapsed + 2))
  done
}

need_cmd brew
need_cmd colima
need_cmd docker

log "Starting Redis via Homebrew services..."
brew services start redis >/dev/null
log "Redis is ready or already running."

if colima status >/dev/null 2>&1; then
  log "Colima is already running."
else
  log "Starting Colima..."
  colima start
fi

log "Waiting for Docker..."
wait_for_docker
log "Docker is ready."

if ! docker inspect "$NACOS_CONTAINER" >/dev/null 2>&1; then
  log "Docker container '$NACOS_CONTAINER' does not exist."
  log "Check the container name with: docker ps -a --format '{{.Names}}'"
  exit 1
fi

if docker ps --format '{{.Names}}' | grep -Fxq "$NACOS_CONTAINER"; then
  log "Nacos container '$NACOS_CONTAINER' is already running."
else
  log "Starting Nacos container '$NACOS_CONTAINER'..."
  docker start "$NACOS_CONTAINER" >/dev/null
  log "Nacos is running."
fi

log "Development environment is ready."
