#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
USER_USERNAME="${USER_USERNAME:-user}"
PASSWORD="${PASSWORD:-Admin_123456}"
TOP_K="${TOP_K:-5}"
MIN_SIMILARITY="${MIN_SIMILARITY:-0.2}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
EVALUATION_SET_PATH="${EVALUATION_SET_PATH:-$REPO_ROOT/docs/evaluation/rag-evaluation-set.json}"
RESULTS_PATH="${RESULTS_PATH:-${TMPDIR:-/tmp}/phase23-rag-evaluation-results.json}"
TMP_DIR="${TMPDIR:-/tmp}"
BODY_FILE="$TMP_DIR/phase23-rag-evaluation-response.json"
PAYLOAD_FILE="$TMP_DIR/phase23-rag-evaluation-payload.json"

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
  node -e 'const fs=require("fs"); fs.writeFileSync(process.argv[1], JSON.stringify({username: process.argv[2], password: process.argv[3]}));' "$PAYLOAD_FILE" "$USER_USERNAME" "$PASSWORD"
  local code
  code=$(request POST /api/auth/login "" "$PAYLOAD_FILE")
  if [[ "$code" != "200" ]]; then
    printf 'user login failed with status %s\n' "$code" >&2
    cat "$BODY_FILE" >&2
    exit 1
  fi
  json_get "$BODY_FILE" data.accessToken
}

USER_TOKEN=$(login)
printf 'phase23RagEvaluation start token:redacted dataset=%s\n' "$EVALUATION_SET_PATH"

export BASE_URL
export USER_TOKEN
export EVALUATION_SET_PATH
export RESULTS_PATH
export TOP_K
export MIN_SIMILARITY

node --input-type=commonjs <<'NODE'
const fs = require('fs');
const path = require('path');

const baseUrl = process.env.BASE_URL.replace(/\/$/, '');
const token = process.env.USER_TOKEN;
const datasetPath = process.env.EVALUATION_SET_PATH;
const resultsPath = process.env.RESULTS_PATH;
const topK = Number(process.env.TOP_K || '5');
const minSimilarity = Number(process.env.MIN_SIMILARITY || '0.2');
const cases = JSON.parse(fs.readFileSync(datasetPath, 'utf8'));

if (!Array.isArray(cases)) {
  throw new Error('evaluation set must be an array');
}

function normalize(value) {
  return String(value || '').toLowerCase().replace(/\s+/g, '');
}

function includesNormalized(haystack, needle) {
  return normalize(haystack).includes(normalize(needle));
}

function citationEvidence(answer) {
  return (answer.citations || [])
    .map((citation) => `${citation.sourceTitle || ''}\n${citation.snippet || ''}`)
    .join('\n');
}

function retrievalHit(answer, expectedSourceHint) {
  const evidence = citationEvidence(answer);
  if (includesNormalized(evidence, expectedSourceHint)) {
    return true;
  }
  return (answer.citations || []).some((citation) => {
    const title = citation.sourceTitle || '';
    return includesNormalized(expectedSourceHint, title)
      || includesNormalized(title, expectedSourceHint)
      || normalize(expectedSourceHint).split('或').some((part) => part && includesNormalized(title, part));
  });
}

function usefulAnswer(answer, expectedKeywords, shouldTransfer) {
  const content = answer.answer || '';
  const matchedKeywords = expectedKeywords.filter((keyword) => includesNormalized(content, keyword));
  if (shouldTransfer) {
    return Boolean(answer.transferSuggested || !answer.canAnswer || matchedKeywords.length > 0);
  }
  return matchedKeywords.length >= Math.max(1, Math.ceil(expectedKeywords.length / 2));
}

async function ask(caseItem) {
  const response = await fetch(`${baseUrl}/api/ai/chat/ask`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      question: caseItem.question,
      topK,
      minSimilarity,
    }),
  });
  const text = await response.text();
  let body;
  try {
    body = JSON.parse(text);
  } catch {
    body = { rawBody: text };
  }
  return { status: response.status, body };
}

async function main() {
const results = [];

for (const caseItem of cases) {
  const response = await ask(caseItem);
  if (response.status !== 200) {
    results.push({
      id: caseItem.id,
      question: caseItem.question,
      status: response.status,
      expectedSourceHint: caseItem.expectedSourceHint,
      shouldTransfer: caseItem.shouldTransfer,
      error: response.body,
      scores: {
        retrievalHit: false,
        usefulAnswer: false,
        transferSuggested: false,
        transferExpected: caseItem.shouldTransfer,
      },
    });
    console.log(`ragCase ${caseItem.id} status=${response.status} retrievalHit=false usefulAnswer=false transferSuggested=false`);
    continue;
  }

  const answer = response.body.data || {};
  const hit = retrievalHit(answer, caseItem.expectedSourceHint);
  const useful = usefulAnswer(answer, caseItem.expectedKeywords || [], caseItem.shouldTransfer);
  const transferSuggested = Boolean(answer.transferSuggested);
  results.push({
    id: caseItem.id,
    category: caseItem.category,
    question: caseItem.question,
    status: response.status,
    expectedKeywords: caseItem.expectedKeywords,
    expectedSourceHint: caseItem.expectedSourceHint,
    shouldTransfer: caseItem.shouldTransfer,
    answer: answer.answer || '',
    canAnswer: Boolean(answer.canAnswer),
    confidence: Number(answer.confidence || 0),
    transferSuggested,
    transferReason: answer.transferReason || null,
    citations: (answer.citations || []).map((citation) => ({
      citationIndex: citation.citationIndex,
      sourceTitle: citation.sourceTitle,
      similarity: citation.similarity,
      snippet: citation.snippet,
    })),
    scores: {
      retrievalHit: hit,
      usefulAnswer: useful,
      transferSuggested,
      transferExpected: caseItem.shouldTransfer,
    },
  });
  console.log(`ragCase ${caseItem.id} status=${response.status} retrievalHit=${hit} usefulAnswer=${useful} transferSuggested=${transferSuggested}`);
}

const total = results.length;
const nonTransferCases = results.filter((result) => !result.shouldTransfer);
const transferCases = results.filter((result) => result.shouldTransfer);
const retrievalHits = results.filter((result) => result.scores.retrievalHit).length;
const usefulAnswers = results.filter((result) => result.scores.usefulAnswer).length;
const wrongTransfers = nonTransferCases.filter((result) => result.transferSuggested).length;
const missedTransfers = transferCases.filter((result) => !result.transferSuggested).length;

function rate(count, denominator) {
  if (denominator === 0) {
    return 0;
  }
  return Number((count / denominator).toFixed(4));
}

const output = {
  metadata: {
    evaluatedAt: new Date().toISOString(),
    baseUrl,
    datasetPath,
    topK,
    minSimilarity,
    token: 'redacted',
  },
  summary: {
    totalCases: total,
    retrievalHits,
    usefulAnswers,
    wrongTransfers,
    missedTransfers,
    retrievalHitRate: rate(retrievalHits, total),
    answerUsefulRate: rate(usefulAnswers, total),
    wrongTransferRate: rate(wrongTransfers, nonTransferCases.length),
    missedTransferRate: rate(missedTransfers, transferCases.length),
  },
  cases: results,
};

fs.mkdirSync(path.dirname(resultsPath), { recursive: true });
fs.writeFileSync(resultsPath, `${JSON.stringify(output, null, 2)}\n`);
console.log(`ragEvaluationSummary retrievalHitRate=${output.summary.retrievalHitRate} answerUsefulRate=${output.summary.answerUsefulRate} wrongTransferRate=${output.summary.wrongTransferRate} missedTransferRate=${output.summary.missedTransferRate}`);
console.log(`ragEvaluationResults ${resultsPath}`);
}

main().catch((error) => {
  console.error(error.stack || error.message);
  process.exit(1);
});
NODE

printf 'phase23RagEvaluation complete token:redacted results=%s\n' "$RESULTS_PATH"
