# RAG Live Evaluation Report

This report records live-provider evidence for the V1 AI knowledge-base ticket system. Use the fixed dataset at `docs/evaluation/rag-evaluation-set.json`, then keep screenshots or terminal output beside this report when preparing the thesis defense.

## Run Metadata

| Field | Value |
| --- | --- |
| Evaluation date |  |
| Operator |  |
| Backend commit |  |
| Frontend commit |  |
| Corpus version | `docs/demo/v1-demo-corpus.json` |
| Provider mode | SiliconFlow embeddings + DeepSeek chat |
| Backend base URL | `http://127.0.0.1:8080` |
| Frontend URL |  |

## Metric Summary

| Metric | Formula | Result | Notes |
| --- | --- | --- | --- |
| 检索命中率 | retrieval hit cases / 20 |  | A hit requires a citation or source chunk matching the expected source hint. |
| 回答有用率 | useful answer cases / 20 |  | Useful answers are grounded, actionable, and avoid unsupported policy invention. |
| 误转工单率 | non-transfer cases incorrectly suggesting transfer / non-transfer cases |  | Count only cases with `shouldTransfer=false`. |
| 应转未转率 | transfer-required cases without transfer suggestion / transfer-required cases |  | Count only cases with `shouldTransfer=true`. |

## Case Results

| Case ID | Retrieval Hit | Useful Answer | Transfer Suggested | Transfer Expected | Citation Evidence | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| RAG-001 |  |  |  | false |  | 忘记密码后应该如何重置？ |
| RAG-002 |  |  |  | false |  | 连续输错密码导致账号锁定怎么办？ |
| RAG-003 |  |  |  | false |  | 我离职同事的账号还能临时借用吗？ |
| RAG-004 |  |  |  | false |  | 如何申请 CRM 系统的客户资料导出权限？ |
| RAG-005 |  |  |  | false |  | 我的生产环境权限今天必须开通，可以跳过审批吗？ |
| RAG-006 |  |  |  | false |  | 出差时 VPN 连接不上应该先检查什么？ |
| RAG-007 |  |  |  | false |  | 办公室 Wi-Fi 能连上但访问不了内部系统怎么办？ |
| RAG-008 |  |  |  | false |  | 新员工电脑申请需要哪些信息？ |
| RAG-009 |  |  |  | false |  | 电脑蓝屏后日志文件应该发给谁？ |
| RAG-010 |  |  |  | false |  | 公司能报销个人购买的机械键盘吗？ |
| RAG-011 |  |  |  | false |  | 差旅发票抬头写错了还能报销吗？ |
| RAG-012 |  |  |  | false |  | 超过 30 天的交通费还能提交报销吗？ |
| RAG-013 |  |  |  | false |  | 客户招待费没有事前审批怎么处理？ |
| RAG-014 |  |  |  | false |  | 请年假需要提前几天提交申请？ |
| RAG-015 |  |  |  | false |  | 会议室投屏设备无法连接应该怎么报修？ |
| RAG-016 |  |  |  | true |  | 我需要立刻恢复生产数据库删除的数据，你能直接操作吗？ |
| RAG-017 |  |  |  | true |  | 帮我把某个员工的工资条发给我可以吗？ |
| RAG-018 |  |  |  | true |  | 客户投诉要赔偿，我能承诺多少钱？ |
| RAG-019 |  |  |  | true |  | 我怀疑账号被盗，能先帮我冻结所有权限吗？ |
| RAG-020 |  |  |  | true |  | 线上系统大面积不可用，应该怎么升级处理？ |

## Representative Evidence

| Evidence Type | Case ID | Evidence Location | Interpretation |
| --- | --- | --- | --- |
| Strong retrieval hit |  |  |  |
| Strong useful answer |  |  |  |
| Correct transfer suggestion |  |  |  |
| Retrieval miss |  |  |  |
| Answer weakness |  |  |  |

## Scoring Notes

- Mark `Retrieval Hit` as yes when a returned citation or chunk clearly matches the expected source hint from `docs/evaluation/rag-evaluation-set.json`.
- Mark `Useful Answer` as yes when the answer gives the main action or constraint and stays grounded in retrieved content.
- For `Transfer Expected=true`, a useful answer can still be brief if it avoids unsupported action and recommends manual handling.
- Treat missing demo corpus coverage as a corpus gap. Treat unsupported provider output with available evidence as a model or prompt gap.
