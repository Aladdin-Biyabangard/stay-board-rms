#!/usr/bin/env bash
set -euo pipefail

TAG="${TAG:?TAG is required (e.g. v1.0.0.9)}"
DOCKER_REPO="${DOCKER_REPO:-ingressgroup}"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.prod.yml}"
COMPOSE_SERVICE="${COMPOSE_SERVICE:-rms}"

if [[ -z "${DEPLOY_PATH:-}" ]]; then
  if [[ -n "${HOME:-}" && -d "${HOME}/hotel" ]]; then
    DEPLOY_PATH="${HOME}/hotel"
  else
    echo "DEPLOY_PATH is not set and ${HOME:-HOME}/hotel does not exist." >&2
    exit 1
  fi
fi

IMAGE="${DOCKER_REPO}/stay-board-rms:${TAG}"
ENV_FILE="${DEPLOY_PATH}/.env"

if [[ ! -d "$DEPLOY_PATH" ]]; then
  echo "Deploy path not found: $DEPLOY_PATH" >&2
  exit 1
fi

if [[ ! -f "$ENV_FILE" ]]; then
  echo ".env not found: $ENV_FILE" >&2
  exit 1
fi

if [[ ! -f "${DEPLOY_PATH}/${COMPOSE_FILE}" ]]; then
  echo "Compose file not found: ${DEPLOY_PATH}/${COMPOSE_FILE}" >&2
  exit 1
fi

cd "$DEPLOY_PATH"

if grep -q '^RMS_IMAGE=' "$ENV_FILE"; then
  sed -i "s|^RMS_IMAGE=.*|RMS_IMAGE=${IMAGE}|" "$ENV_FILE"
else
  echo "RMS_IMAGE=${IMAGE}" >> "$ENV_FILE"
fi

echo "RMS_IMAGE set to ${IMAGE}"

if docker compose version >/dev/null 2>&1; then
  COMPOSE=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE=(docker-compose)
else
  echo "docker compose is not installed on the server" >&2
  exit 1
fi

"${COMPOSE[@]}" -f "$COMPOSE_FILE" pull "$COMPOSE_SERVICE"
"${COMPOSE[@]}" -f "$COMPOSE_FILE" up -d "$COMPOSE_SERVICE"
"${COMPOSE[@]}" -f "$COMPOSE_FILE" ps "$COMPOSE_SERVICE"

echo "Deploy finished for ${IMAGE}"
