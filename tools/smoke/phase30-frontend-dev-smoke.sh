#!/usr/bin/env bash
set -euo pipefail

FRONTEND_BASE_URL="${FRONTEND_BASE_URL:-http://127.0.0.1:5174}"
ADMIN_USERNAME="${ADMIN_USERNAME:-admin}"
PASSWORD="${PASSWORD:-Admin_123456}"
TMP_DIR="${TMPDIR:-/tmp}"
BODY_FILE="$TMP_DIR/phase30-frontend-smoke-response.json"
HEADER_FILE="$TMP_DIR/phase30-frontend-smoke-headers.txt"

json_get() {
  node -e 'const fs=require("fs"); const path=process.argv[1]; const expr=process.argv[2]; const j=JSON.parse(fs.readFileSync(path,"utf8")); const v=expr.split(".").reduce((o,k)=>o && o[k], j); if (v === undefined || v === null) process.exit(2); if (typeof v === "object") console.log(JSON.stringify(v)); else console.log(v);' "$1" "$2"
}

json_expect() {
  node -e 'const fs=require("fs"); const path=process.argv[1]; const expr=process.argv[2]; const expected=process.argv[3]; const j=JSON.parse(fs.readFileSync(path,"utf8")); const v=expr.split(".").reduce((o,k)=>o && o[k], j); if (String(v) !== expected) { console.error(`expected ${expr}=${expected}, got ${String(v)}`); process.exit(1); }' "$1" "$2" "$3"
}

request() {
  local method="$1"
  local path="$2"
  local token="${3:-}"
  local body="${4:-}"
  local code
  if [[ -n "$token" && -n "$body" ]]; then
    code=$(curl -sS -D "$HEADER_FILE" -o "$BODY_FILE" -w '%{http_code}' -X "$method" "$FRONTEND_BASE_URL$path" -H "Authorization: Bearer $token" -H 'Content-Type: application/json' -d "$body")
  elif [[ -n "$token" ]]; then
    code=$(curl -sS -D "$HEADER_FILE" -o "$BODY_FILE" -w '%{http_code}' -X "$method" "$FRONTEND_BASE_URL$path" -H "Authorization: Bearer $token")
  elif [[ -n "$body" ]]; then
    code=$(curl -sS -D "$HEADER_FILE" -o "$BODY_FILE" -w '%{http_code}' -X "$method" "$FRONTEND_BASE_URL$path" -H 'Content-Type: application/json' -d "$body")
  else
    code=$(curl -sS -D "$HEADER_FILE" -o "$BODY_FILE" -w '%{http_code}' -X "$method" "$FRONTEND_BASE_URL$path")
  fi
  printf '%s' "$code"
}

check_html_route() {
  local path="$1"
  local code
  code=$(request GET "$path")
  if [[ "$code" != "200" ]]; then
    printf 'expected 200 for route %s, got %s\n' "$path" "$code" >&2
    cat "$BODY_FILE" >&2
    exit 1
  fi
  if ! grep -qi '^content-type:.*text/html' "$HEADER_FILE"; then
    printf 'expected text/html for route %s\n' "$path" >&2
    cat "$HEADER_FILE" >&2
    exit 1
  fi
  if ! grep -q 'id="app"' "$BODY_FILE"; then
    printf 'expected Vite app shell for route %s\n' "$path" >&2
    exit 1
  fi
  printf 'route %s 200 html\n' "$path"
}

check_api() {
  local name="$1"
  local expected="$2"
  local method="$3"
  local path="$4"
  local token="${5:-}"
  local body="${6:-}"
  local code
  code=$(request "$method" "$path" "$token" "$body")
  printf '%s %s json\n' "$name" "$code"
  if [[ "$code" != "$expected" ]]; then
    printf 'expected %s for %s, got %s\n' "$expected" "$name" "$code" >&2
    cat "$BODY_FILE" >&2
    exit 1
  fi
  json_expect "$BODY_FILE" success true
}

check_html_route /
check_html_route /login
check_html_route /app
check_html_route /app/ai/chat
check_html_route /app/tickets/my
check_html_route /app/admin/dashboard

check_api adminLogin 200 POST /api/auth/login "" "{\"username\":\"$ADMIN_USERNAME\",\"password\":\"$PASSWORD\"}"
ADMIN_TOKEN=$(json_get "$BODY_FILE" data.accessToken)
printf 'adminLoginToken 200 token:redacted\n'

check_api authMe 200 GET /api/auth/me "$ADMIN_TOKEN"
json_expect "$BODY_FILE" data.user.username "$ADMIN_USERNAME"

printf 'phase30FrontendDevSmoke ok\n'
