# V1 Demo Knowledge Corpus

This corpus is the recommended knowledge base for the defense demo. The machine-readable file is:

```text
docs/demo/v1-demo-corpus.json
```

Each JSON item can be entered through `/app/knowledge`:

- `title` -> 标题
- `categoryId` -> 分类 ID
- `content` -> 正文

`sourceHints` are not sent to the backend. They are used by tests and evaluators to confirm that the corpus covers `docs/evaluation/rag-evaluation-set.json`.

## Loading Order

Load the account, permission, network, device, reimbursement, and office-process documents first. These cover the non-transfer questions `RAG-001` to `RAG-015`.

Then load the boundary documents:

- 敏感个人信息处理边界
- 合同审批和销售授权制度
- 生产事故应急预案
- 未公开人事信息处理边界

These documents help the demo explain why some questions should transfer to a human instead of receiving a confident final answer.

## Suggested Demo Question

Use this stable first question after loading the corpus:

```text
忘记密码后应该如何重置？
```

Expected retrieval source:

```text
账号登录 FAQ
```

Expected answer content should mention:

- 忘记密码
- 身份验证
- 重置

## Evaluation Use

After loading the corpus, run the 20 questions in `docs/evaluation/rag-evaluation-set.json` through `/app/ai/chat` or the backend RAG API.

Record:

- 检索命中率
- 回答有用率
- 误转工单率
- 应转未转率

Use `docs/evaluation/rag-evaluation-set.md` for scoring rules.
