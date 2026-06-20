# RAG Evaluation Set

## Purpose

This evaluation set provides 20 first-version enterprise service questions for measuring the AI knowledge-base answer flow. It is intentionally small enough for manual review during thesis preparation, while still covering common service categories and questions that should transfer to a human.

The dataset file is `docs/evaluation/rag-evaluation-set.json`.

## Case Fields

| Field | Meaning |
| --- | --- |
| `id` | Stable case identifier used in reports. |
| `category` | Business category for grouped analysis. |
| `question` | User-facing natural-language question. |
| `expectedKeywords` | Keywords expected in a useful answer or retrieved source. |
| `expectedSourceHint` | Human-readable hint for the expected knowledge document. |
| `shouldTransfer` | Whether the system should suggest human handling because the answer requires personal data, approval, emergency handling, or knowledge outside the safe corpus. |

## Manual Scoring Rules

### 检索命中

Mark retrieval as hit when at least one returned citation or source chunk clearly matches `expectedSourceHint`, or when the retrieved text contains at least one item from `expectedKeywords` and supports the answer.

Aggregate metric:

```text
检索命中率 = 检索命中案例数 / 总案例数
```

### 回答有用率

Mark an answer as useful when it is grounded in retrieved content, includes the main action or constraint the user needs, and does not invent policy details. For `shouldTransfer=true` cases, a useful answer should avoid unsupported instructions and explain that manual handling is needed.

Aggregate metric:

```text
回答有用率 = 有用回答案例数 / 总案例数
```

### 误转工单率

For cases where `shouldTransfer=false`, mark a mistaken transfer if the system suggests human handling despite enough knowledge being available. For cases where `shouldTransfer=true`, do not count transfer as a mistake.

Aggregate metric:

```text
误转工单率 = 不应转人工但被建议转人工的案例数 / shouldTransfer=false 案例数
```

### 应转未转率

For cases where `shouldTransfer=true`, mark a missed transfer if the system gives a confident final answer without recommending human review.

Aggregate metric:

```text
应转未转率 = 应转人工但未建议转人工的案例数 / shouldTransfer=true 案例数
```

## Reporting Template

Use this table when preparing thesis or demo evidence:

| Metric | Value | Notes |
| --- | --- | --- |
| 检索命中率 | `x / 20` | Cite representative hit and miss cases. |
| 回答有用率 | `x / 20` | Explain whether errors came from retrieval or generation. |
| 误转工单率 | `x / 15` | There are 15 non-transfer cases in the current set. |
| 应转未转率 | `x / 5` | There are 5 transfer-required cases in the current set. |

## Execution Notes

The first-version backend can be evaluated manually through the RAG question API after loading knowledge documents that cover the expected source hints. If the knowledge corpus is incomplete, record retrieval misses as corpus gaps instead of model failures.
