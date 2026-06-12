#!/usr/bin/env bash
set -euo pipefail

COMMAND=""
TAG=""
PROPERTIES_FILE="gradle.properties"

usage() {
  echo "Usage: $0 -Command stay-board-rms-build-push -Tag <tag> [-PropertiesFile gradle.properties]" >&2
  exit 1
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    -Command|-command)
      COMMAND="${2:-}"; shift 2 ;;
    -Tag|-tag)
      TAG="${2:-}"; shift 2 ;;
    -PropertiesFile|-properties-file)
      PROPERTIES_FILE="${2:-}"; shift 2 ;;
    -h|--help)
      usage ;;
    *)
      echo "Unknown argument: $1" >&2
      usage ;;
  esac
done

if [[ "$COMMAND" != "stay-board-rms-build-push" ]]; then
  echo "Invalid -Command: ${COMMAND:-<missing>} (expected stay-board-rms-build-push)" >&2
  exit 1
fi

if [[ -z "$TAG" ]]; then
  echo "-Tag is required" >&2
  usage
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
CURRENT_DIR="$(pwd)"

if [[ "$(pwd -P)" != "$PROJECT_ROOT" && "$(pwd -P)" != "$PROJECT_ROOT"/* ]]; then
  echo "This script can only run inside this project: $PROJECT_ROOT" >&2
  exit 1
fi

if [[ ! -f "$PROPERTIES_FILE" ]]; then
  echo "Properties file not found: $PROPERTIES_FILE" >&2
  exit 1
fi

get_property() {
  local key="$1"
  local file="$2"
  grep -E "^[[:space:]]*${key}[[:space:]]*=" "$file" | tail -n1 | sed -E "s/^[[:space:]]*${key}[[:space:]]*=[[:space:]]*(.*)[[:space:]]*$/\1/" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//'
}

dockerHubUsername="$(get_property dockerHubUsername "$PROPERTIES_FILE")"
dockerHubPassword="$(get_property dockerHubPassword "$PROPERTIES_FILE")"
dockerRepoUrl="$(get_property dockerRepoUrl "$PROPERTIES_FILE")"

if [[ -z "$dockerHubUsername" ]]; then
  echo "dockerHubUsername not found in $PROPERTIES_FILE" >&2
  exit 1
fi
if [[ -z "$dockerHubPassword" ]]; then
  echo "dockerHubPassword not found in $PROPERTIES_FILE" >&2
  exit 1
fi
if [[ -z "$dockerRepoUrl" ]]; then
  dockerRepoUrl="$dockerHubUsername"
fi

backendImage="${dockerRepoUrl}/stay-board-rms:${TAG}"

echo "Docker login check..."
if ! docker info >/dev/null 2>&1; then
  echo "Docker daemon is not available. Please start Docker Desktop and try again." >&2
  exit 1
fi

echo "Docker Hub login..."
if ! echo "$dockerHubPassword" | docker login --username "$dockerHubUsername" --password-stdin; then
  echo "Docker Hub login failed." >&2
  exit 1
fi

echo "Building backend image: $backendImage"
if ! docker build -t "$backendImage" -f Dockerfile .; then
  echo "Backend docker build failed. Image push skipped." >&2
  exit 1
fi

echo "Pushing backend image..."
if ! docker push "$backendImage"; then
  echo "Backend docker push failed." >&2
  exit 1
fi

echo ""
echo "Done."
echo "Backend : $backendImage"
