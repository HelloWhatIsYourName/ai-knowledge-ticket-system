import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import RagChatView from './RagChatView.vue'
import { askQuestion, listSessionMessages, listSessions } from '../../api/ragChat'
import { createTicketFromAiSession, listTicketCategories } from '../../api/tickets'

vi.mock('../../api/ragChat', () => ({
  askQuestion: vi.fn(),
  listSessions: vi.fn(),
  listSessionMessages: vi.fn()
}))

vi.mock('../../api/tickets', () => ({
  createTicketFromAiSession: vi.fn(),
  listTicketCategories: vi.fn()
}))

const askQuestionMock = vi.mocked(askQuestion)
const listSessionsMock = vi.mocked(listSessions)
const listSessionMessagesMock = vi.mocked(listSessionMessages)
const listTicketCategoriesMock = vi.mocked(listTicketCategories)
const createTicketFromAiSessionMock = vi.mocked(createTicketFromAiSession)

describe('RagChatView', () => {
  it('renders the RAG workspace and submits a question with citations', async () => {
    listSessionsMock.mockResolvedValue([{ id: 3, title: '历史会话', lastQuestion: '怎么登录？' }])
    listSessionMessagesMock.mockResolvedValue([])
    listTicketCategoriesMock.mockResolvedValue([{ id: 1, name: '账号问题', enabled: true }])
    createTicketFromAiSessionMock.mockResolvedValue({
      id: 8,
      ticketNo: 'TK-20260620-0001',
      title: '无法登录',
      status: 'PENDING'
    })
    askQuestionMock.mockResolvedValue({
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

    expect(askQuestionMock).toHaveBeenCalledWith({
      question: '如何重置密码？',
      sessionId: undefined,
      topK: 4
    })
    expect(wrapper.text()).toContain('请先在账号安全页面重置密码。')
    expect(wrapper.text()).toContain('账号手册')
    expect(wrapper.text()).toContain('账号安全页面提供密码重置入口。')
  })
})
