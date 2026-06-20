import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import RagChatView from './RagChatView.vue'
import { askQuestion, askQuestionStream, listSessionMessages, listSessions, type RagAnswerResponse } from '../../api/ragChat'
import { createTicketFromAiSession, listTicketCategories } from '../../api/tickets'

vi.mock('../../api/ragChat', () => ({
  askQuestion: vi.fn(),
  askQuestionStream: vi.fn(),
  listSessions: vi.fn(),
  listSessionMessages: vi.fn()
}))

vi.mock('../../api/tickets', () => ({
  createTicketFromAiSession: vi.fn(),
  listTicketCategories: vi.fn()
}))

const askQuestionMock = vi.mocked(askQuestion)
const askQuestionStreamMock = vi.mocked(askQuestionStream)
const listSessionsMock = vi.mocked(listSessions)
const listSessionMessagesMock = vi.mocked(listSessionMessages)
const listTicketCategoriesMock = vi.mocked(listTicketCategories)
const createTicketFromAiSessionMock = vi.mocked(createTicketFromAiSession)

describe('RagChatView', () => {
  beforeEach(() => {
    askQuestionMock.mockReset()
    askQuestionStreamMock.mockReset()
    listSessionsMock.mockReset()
    listSessionMessagesMock.mockReset()
    listTicketCategoriesMock.mockReset()
    createTicketFromAiSessionMock.mockReset()
  })

  it('renders the RAG workspace and streams a question with citations', async () => {
    listSessionsMock.mockResolvedValue([{ id: 3, title: '历史会话', lastQuestion: '怎么登录？' }])
    listSessionMessagesMock.mockResolvedValue([])
    listTicketCategoriesMock.mockResolvedValue([{ id: 1, name: '账号问题', enabled: true }])
    createTicketFromAiSessionMock.mockResolvedValue({
      id: 8,
      ticketNo: 'TK-20260620-0001',
      title: '无法登录',
      status: 'PENDING'
    })
    const streamResult: RagAnswerResponse = {
      sessionId: 7,
      userMessageId: 11,
      assistantMessageId: 12,
      answer: '请先在账号安全页面重置密码。',
      canAnswer: true,
      confidence: 0.82,
      transferSuggested: true,
      transferReason: '用户可能仍需人工确认账号状态',
      citations: [
        {
          citationIndex: 1,
          sourceTitle: '账号手册',
          snippet: '账号安全页面提供密码重置入口。',
          similarity: 0.91
        }
      ]
    }
    let resolveStream: (value: RagAnswerResponse) => void = () => undefined
    askQuestionStreamMock.mockImplementation((_request, handlers) => {
      handlers.onToken?.('请先在账号安全')
      return new Promise((resolve) => {
        resolveStream = resolve
      })
    })

    const wrapper = mount(RagChatView)
    await flushPromises()

    expect(wrapper.text()).toContain('AI 问答工作台')
    expect(wrapper.text()).toContain('输入用户问题')
    expect(wrapper.text()).toContain('引用来源')
    expect(wrapper.text()).toContain('转为工单')

    await wrapper.find('[data-testid="question-input"]').setValue('如何重置密码？')
    await wrapper.find('[data-testid="ask-button"]').trigger('click')
    await flushPromises()

    expect(askQuestionStreamMock).toHaveBeenCalledWith(
      {
        question: '如何重置密码？',
        sessionId: undefined,
        topK: 4
      },
      expect.objectContaining({ onToken: expect.any(Function) })
    )
    expect(wrapper.text()).toContain('请先在账号安全')

    resolveStream(streamResult)
    await flushPromises()

    expect(askQuestionMock).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('请先在账号安全页面重置密码。')
    expect(wrapper.text()).toContain('账号手册')
    expect(wrapper.text()).toContain('账号安全页面提供密码重置入口。')
  })

  it('falls back to normal HTTP ask when streaming fails', async () => {
    listSessionsMock.mockResolvedValue([])
    listSessionMessagesMock.mockResolvedValue([])
    listTicketCategoriesMock.mockResolvedValue([])
    askQuestionStreamMock.mockRejectedValue(new Error('stream unavailable'))
    askQuestionMock.mockResolvedValue({
      sessionId: 9,
      userMessageId: 31,
      assistantMessageId: 32,
      answer: 'HTTP 降级回答。',
      canAnswer: true,
      confidence: 0.7,
      transferSuggested: false,
      transferReason: null,
      citations: []
    })

    const wrapper = mount(RagChatView)
    await flushPromises()

    await wrapper.find('[data-testid="question-input"]').setValue('如何重置密码？')
    await wrapper.find('[data-testid="ask-button"]').trigger('click')
    await flushPromises()

    expect(askQuestionStreamMock).toHaveBeenCalledWith(
      {
        question: '如何重置密码？',
        sessionId: undefined,
        topK: 4
      },
      expect.objectContaining({ onToken: expect.any(Function) })
    )
    expect(askQuestionMock).toHaveBeenCalledWith({
      question: '如何重置密码？',
      sessionId: undefined,
      topK: 4
    })
    expect(wrapper.text()).toContain('HTTP 降级回答。')
  })
})
