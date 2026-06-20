import { beforeEach, describe, expect, it, vi } from 'vitest'
import { http } from './http'
import { askQuestion, listSessionMessages, listSessions } from './ragChat'

vi.mock('./http', async () => {
  const actual = await vi.importActual<typeof import('./http')>('./http')

  return {
    ...actual,
    http: {
      get: vi.fn(),
      post: vi.fn()
    }
  }
})

const getMock = vi.mocked(http.get)
const postMock = vi.mocked(http.post)

describe('ragChat api', () => {
  beforeEach(() => {
    getMock.mockReset()
    postMock.mockReset()
  })

  it('asks a question through the RAG endpoint and unwraps the response', async () => {
    postMock.mockResolvedValueOnce({
      data: {
        success: true,
        data: {
          sessionId: 7,
          userMessageId: 11,
          assistantMessageId: 12,
          answer: '可以在账号安全页面重置密码。',
          canAnswer: true,
          confidence: 0.86,
          transferSuggested: false,
          transferReason: null,
          citations: [{ citationIndex: 1, sourceTitle: '账号手册', snippet: '重置密码步骤' }]
        },
        message: 'ok'
      }
    })

    await expect(askQuestion({ question: '如何重置密码？', topK: 4 })).resolves.toMatchObject({
      sessionId: 7,
      answer: '可以在账号安全页面重置密码。'
    })
    expect(postMock).toHaveBeenCalledWith('/ai/chat/ask', { question: '如何重置密码？', topK: 4 })
  })

  it('loads sessions and messages through history endpoints', async () => {
    getMock
      .mockResolvedValueOnce({
        data: {
          success: true,
          data: [{ id: 12, title: '登录问题', lastQuestion: '无法登录' }],
          message: 'ok'
        }
      })
      .mockResolvedValueOnce({
        data: {
          success: true,
          data: [{ id: 21, sessionId: 12, role: 'USER', content: '无法登录' }],
          message: 'ok'
        }
      })

    await expect(listSessions()).resolves.toHaveLength(1)
    await expect(listSessionMessages(12)).resolves.toHaveLength(1)
    expect(getMock).toHaveBeenNthCalledWith(1, '/ai/chat/sessions')
    expect(getMock).toHaveBeenNthCalledWith(2, '/ai/chat/sessions/12/messages')
  })
})
