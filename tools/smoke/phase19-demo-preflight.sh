#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
ADMIN_USERNAME="${ADMIN_USERNAME:-admin}"
USER_USERNAME="${USER_USERNAME:-user}"
PASSWORD="${PASSWORD:-Admin_123456}"
TMP_DIR="${TMPDIR:-/tmp}"
BODY_FILE="$TMP_DIR/phase19-preflight-response.json"

json_get() {
  node -e 'const fs=require("fs"); const path=process.argv[1]; const expr=process.argv[2]; const j=JSON.parse(fs.readFileSync(path,"utf8")); const v=expr.split(".").reduce((o,k)=>o && o[k], j); if (v === undefined || v === null) process.exit(2); if (typeof v === "object") console.log(JSON.stringify(v)); else console.log(v);' "$1" "$2"
}

shape() {
  node -e 'const fs=require("fs"); const s=fs.readFileSync(process.argv[1],"utf8"); let j; try { j=JSON.parse(s); } catch { console.log("non-json"); process.exit(0); } const d=j.data; if (Array.isArray(d)) console.log(`array(${d.length})`); else if (d && typeof d === "object") console.log(Object.keys(d).slice(0,10).join(",")); else console.log(typeof d);' "$1"
}

request() {
  local method="$1"
  local path="$2"
  local token="${3:-}"
  local body="${4:-}"
  local code
  if [[ -n "$token" && -n "$body" ]]; then
    code=$(curl -sS -o "$BODY_FILE" -w '%{http_code}' -X "$method" "$BASE_URL$path" -H "Authorization: Bearer $token" -H 'Content-Type: application/json' -d "$body")
  elif [[ -n "$token" ]]; then
    code=$(curl -sS -o "$BODY_FILE" -w '%{http_code}' -X "$method" "$BASE_URL$path" -H "Authorization: Bearer $token")
  elif [[ -n "$body" ]]; then
    code=$(curl -sS -o "$BODY_FILE" -w '%{http_code}' -X "$method" "$BASE_URL$path" -H 'Content-Type: application/json' -d "$body")
  else
    code=$(curl -sS -o "$BODY_FILE" -w '%{http_code}' -X "$method" "$BASE_URL$path")
  fi
  printf '%s' "$code"
}

request_stream() {
  local token="$1"
  local question="$2"
  local code
  set +e
  code=$(curl -sS --max-time 20 -o "$BODY_FILE" -w '%{http_code}' -G "$BASE_URL/api/ai/chat/stream" -H "Authorization: Bearer $token" --data-urlencode "question=$question")
  local exit_code=$?
  set -e
  if [[ "$exit_code" != "0" ]]; then
    printf 'stream curl exited with %s and status %s\n' "$exit_code" "$code" >&2
    cat "$BODY_FILE" >&2 || true
    exit 1
  fi
  printf '%s' "$code"
}

login() {
  local username="$1"
  local code
  code=$(request POST /api/auth/login "" "{\"username\":\"$username\",\"password\":\"$PASSWORD\"}")
  if [[ "$code" != "200" ]]; then
    printf 'login failed for %s with status %s\n' "$username" "$code" >&2
    cat "$BODY_FILE" >&2
    exit 1
  fi
  json_get "$BODY_FILE" data.accessToken
}

check() {
  local name="$1"
  local expected="$2"
  local method="$3"
  local path="$4"
  local token="${5:-}"
  local body="${6:-}"
  local code
  code=$(request "$method" "$path" "$token" "$body")
  local response_shape
  response_shape=$(shape "$BODY_FILE")
  printf '%s %s %s\n' "$name" "$code" "$response_shape"
  if [[ "$code" != "$expected" ]]; then
    printf 'expected %s for %s, got %s\n' "$expected" "$name" "$code" >&2
    cat "$BODY_FILE" >&2
    exit 1
  fi
}

check_stream() {
  local token="$1"
  local body="$2"
  local code
  code=$(request_stream "$token" "$body")
  printf 'ragStream %s sse\n' "$code"
  if [[ "$code" != "200" ]]; then
    printf 'expected 200 for ragStream, got %s\n' "$code" >&2
    cat "$BODY_FILE" >&2
    exit 1
  fi
}

ADMIN_TOKEN=$(login "$ADMIN_USERNAME")
USER_TOKEN=$(login "$USER_USERNAME")

printf 'adminLogin 200 token:redacted\n'
printf 'userLogin 200 token:redacted\n'

check authMe 200 GET /api/auth/me "$ADMIN_TOKEN"
check knowledgeSearch 200 POST /api/kb/search "$ADMIN_TOKEN" '{"query":"忘记密码后应该如何重置？","topK":3}'
check ragAsk 200 POST /api/ai/chat/ask "$USER_TOKEN" '{"question":"忘记密码后应该如何重置？"}'
check_stream "$USER_TOKEN" '忘记密码后应该如何重置？'
check adminOverview 200 GET /api/admin/statistics/overview "$ADMIN_TOKEN"

printf 'phase19Preflight complete token:redacted\n'
