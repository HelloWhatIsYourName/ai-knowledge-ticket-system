#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
ADMIN_USERNAME="${ADMIN_USERNAME:-admin}"
PASSWORD="${PASSWORD:-Admin_123456}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
CORPUS_PATH="${CORPUS_PATH:-$REPO_ROOT/docs/demo/v1-demo-corpus.json}"
TMP_DIR="${TMPDIR:-/tmp}"
BODY_FILE="$TMP_DIR/phase23-load-demo-corpus-response.json"
PAYLOAD_FILE="$TMP_DIR/phase23-load-demo-corpus-payload.json"
DOCUMENTS_FILE="$TMP_DIR/phase23-load-demo-corpus-documents.json"

json_get() {
  node -e 'const fs=require("fs"); const path=process.argv[1]; const expr=process.argv[2]; const j=JSON.parse(fs.readFileSync(path,"utf8")); const v=expr.split(".").reduce((o,k)=>o && o[k], j); if (v === undefined || v === null) process.exit(2); if (typeof v === "object") console.log(JSON.stringify(v)); else console.log(v);' "$1" "$2"
}

request() {
  local method="$1"
  local path="$2"
  local token="${3:-}"
  local body_file="${4:-}"
  local code
  if [[ -n "$token" && -n "$body_file" ]]; then
    code=$(curl -sS -o "$BODY_FILE" -w '%{http_code}' -X "$method" "$BASE_URL$path" -H "Authorization: Bearer $token" -H 'Content-Type: application/json' --data-binary "@$body_file")
  elif [[ -n "$token" ]]; then
    code=$(curl -sS -o "$BODY_FILE" -w '%{http_code}' -X "$method" "$BASE_URL$path" -H "Authorization: Bearer $token")
  elif [[ -n "$body_file" ]]; then
    code=$(curl -sS -o "$BODY_FILE" -w '%{http_code}' -X "$method" "$BASE_URL$path" -H 'Content-Type: application/json' --data-binary "@$body_file")
  else
    code=$(curl -sS -o "$BODY_FILE" -w '%{http_code}' -X "$method" "$BASE_URL$path")
  fi
  printf '%s' "$code"
}

login() {
  node -e 'const fs=require("fs"); fs.writeFileSync(process.argv[1], JSON.stringify({username: process.argv[2], password: process.argv[3]}));' "$PAYLOAD_FILE" "$ADMIN_USERNAME" "$PASSWORD"
  local code
  code=$(request POST /api/auth/login "" "$PAYLOAD_FILE")
  if [[ "$code" != "200" ]]; then
    printf 'admin login failed with status %s\n' "$code" >&2
    cat "$BODY_FILE" >&2
    exit 1
  fi
  json_get "$BODY_FILE" data.accessToken
}

corpus_count() {
  node -e 'const fs=require("fs"); const docs=JSON.parse(fs.readFileSync(process.argv[1],"utf8")); if (!Array.isArray(docs)) process.exit(2); console.log(docs.length);' "$CORPUS_PATH"
}

corpus_field() {
  node -e 'const fs=require("fs"); const docs=JSON.parse(fs.readFileSync(process.argv[1],"utf8")); const doc=docs[Number(process.argv[2])]; const value=doc[process.argv[3]]; if (value === undefined || value === null) process.exit(2); console.log(value);' "$CORPUS_PATH" "$1" "$2"
}

write_document_payload() {
  node -e 'const fs=require("fs"); const docs=JSON.parse(fs.readFileSync(process.argv[1],"utf8")); const doc=docs[Number(process.argv[2])]; const payload={title: `${doc.id} ${doc.title}`, categoryId: doc.categoryId ?? 1, content: doc.content}; fs.writeFileSync(process.argv[3], JSON.stringify(payload));' "$CORPUS_PATH" "$1" "$PAYLOAD_FILE"
}

document_exists() {
  node -e 'const fs=require("fs"); const response=JSON.parse(fs.readFileSync(process.argv[1],"utf8")); const docs=Array.isArray(response.data) ? response.data : []; const title=process.argv[2]; console.log(docs.some((doc)=>doc.title === title) ? "yes" : "no");' "$DOCUMENTS_FILE" "$1"
}

ADMIN_TOKEN=$(login)
printf 'phase23LoadDemoCorpus start token:redacted corpus=%s\n' "$CORPUS_PATH"

code=$(request GET /api/kb/documents "$ADMIN_TOKEN")
cp "$BODY_FILE" "$DOCUMENTS_FILE"
if [[ "$code" != "200" ]]; then
  printf 'document list failed with status %s\n' "$code" >&2
  cat "$BODY_FILE" >&2
  exit 1
fi

total=$(corpus_count)
loaded=0
skipped=0

for ((index = 0; index < total; index++)); do
  id=$(corpus_field "$index" id)
  title="$(corpus_field "$index" title)"
  api_title="$id $title"

  if [[ "$(document_exists "$api_title")" == "yes" ]]; then
    skipped=$((skipped + 1))
    printf 'corpusDocument %s skipped existing title="%s"\n' "$id" "$api_title"
    continue
  fi

  write_document_payload "$index"
  code=$(request POST /api/kb/documents/text "$ADMIN_TOKEN" "$PAYLOAD_FILE")
  if [[ "$code" != "200" ]]; then
    printf 'corpusDocument %s failed status=%s\n' "$id" "$code" >&2
    cat "$BODY_FILE" >&2
    exit 1
  fi
  document_id=$(json_get "$BODY_FILE" data.id)
  parse_status=$(json_get "$BODY_FILE" data.parseStatus)
  loaded=$((loaded + 1))
  printf 'corpusDocument %s loaded status=%s documentId=%s parseStatus=%s\n' "$id" "$code" "$document_id" "$parse_status"
done

printf 'phase23LoadDemoCorpus complete token:redacted total=%s loaded=%s skipped=%s\n' "$total" "$loaded" "$skipped"
