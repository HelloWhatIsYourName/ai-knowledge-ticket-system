#!/usr/bin/env bash
set -euo pipefail

BACKEND_URL="${BACKEND_URL:-http://127.0.0.1:8080}"
FRONTEND_URL="${FRONTEND_URL:-http://127.0.0.1:5174}"
SECRET_DIR="${SECRET_DIR:-/private/tmp/ai-ticket-secrets}"
BODY_FILE="${TMPDIR:-/tmp}/phase21-rehearsal-audit-response.txt"

blocked=0

status_line() {
  printf '%s %s %s\n' "$1" "$2" "$3"
}

check_http() {
  local name="$1"
  local url="$2"
  local code
  set +e
  code=$(curl -sS -o "$BODY_FILE" -w '%{http_code}' "$url")
  local exit_code=$?
  set -e
  if [[ "$exit_code" == "0" ]]; then
    status_line "$name" "reachable" "$code"
    return 0
  fi
  status_line "$name" "BLOCKED" "curl-exit:$exit_code"
  blocked=1
  return 1
}

check_env_present() {
  local name="$1"
  local hint_file="${2:-}"
  if [[ -n "${!name:-}" ]]; then
    status_line "$name" "present" "value:redacted"
    return 0
  fi
  if [[ -n "$hint_file" && -f "$hint_file" ]] && rg -q "^$name=" "$hint_file"; then
    status_line "$name" "PARTIAL" "local-secret-file-present"
    blocked=1
    return 1
  fi
  status_line "$name" "BLOCKED" "missing"
  blocked=1
  return 1
}

printf 'phase21RehearsalAudit start token:redacted\n'

if command -v docker >/dev/null 2>&1; then
  docker compose ps
else
  status_line "Docker services" "BLOCKED" "docker-not-found"
  blocked=1
fi

check_http "Backend /api/auth/me" "$BACKEND_URL/api/auth/me" || true
check_http "Frontend" "$FRONTEND_URL/" || true
check_env_present "AI_EMBEDDING_API_KEY" "$SECRET_DIR/siliconflow.env" || true
check_env_present "AI_CHAT_API_KEY" "$SECRET_DIR/deepseek.env" || true

if [[ "$blocked" != "0" ]]; then
  status_line "Phase 19 preflight" "BLOCKED" "missing-prerequisites"
  printf 'phase21RehearsalAudit complete token:redacted\n'
  exit 2
fi

tools/smoke/phase19-demo-preflight.sh
printf 'phase21RehearsalAudit complete token:redacted\n'
