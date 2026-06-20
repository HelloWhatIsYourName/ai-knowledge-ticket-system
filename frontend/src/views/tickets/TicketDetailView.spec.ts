import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import TicketDetailView from './TicketDetailView.vue'
import {
  createTicketComment,
  getTicket,
  listTicketComments,
  resolveTicket,
  startTicket
} from '../../api/tickets'

vi.mock('vue-router', () => ({
  useRoute: () => ({
    params: {
      ticketId: '8'
    }
  })
}))

vi.mock('../../api/tickets', () => ({
  closeTicket: vi.fn(),
  confirmCloseTicket: vi.fn(),
  createTicketComment: vi.fn(),
  getTicket: vi.fn(),
  listTicketComments: vi.fn(),
  reopenTicket: vi.fn(),
  resolveTicket: vi.fn(),
  startTicket: vi.fn()
}))

const getTicketMock = vi.mocked(getTicket)
const listTicketCommentsMock = vi.mocked(listTicketComments)
const createTicketCommentMock = vi.mocked(createTicketComment)
const startTicketMock = vi.mocked(startTicket)
const resolveTicketMock = vi.mocked(resolveTicket)

describe('TicketDetailView', () => {
  it('renders ticket detail, comments, and workflow actions', async () => {
    getTicketMock.mockResolvedValue({
      id: 8,
      ticketNo: 'TK-20260620-0001',
      title: '无法登录后台',
      description: '用户反馈后台登录失败。',
      status: 'PENDING',
      priority: 'HIGH',
      source: 'AI_SESSION',
      transferReason: 'AI 置信度低，需要人工处理',
      aiSummary: '用户无法登录后台',
      aiSuggestion: '检查账号状态和密码策略',
      createdAt: '2026-06-20T10:00:00',
      flowLogs: [
        {
          id: 1,
          ticketId: 8,
          action: 'CREATE',
          operatorId: 2,
          remark: 'AI 会话转入工单',
          createdAt: '2026-06-20T10:00:00'
        }
      ]
    })
    listTicketCommentsMock.mockResolvedValue([
      {
        id: 3,
        ticketId: 8,
        authorId: 2,
        commentType: 'REPLY',
        content: '已收到，正在排查。',
        internal: false,
        createdAt: '2026-06-20T10:05:00'
      },
      {
        id: 4,
        ticketId: 8,
        authorId: 3,
        commentType: 'INTERNAL_NOTE',
        content: '内部备注：疑似账号锁定。',
        internal: true,
        createdAt: '2026-06-20T10:08:00'
      }
    ])
    createTicketCommentMock.mockResolvedValue({
      id: 5,
      ticketId: 8,
      authorId: 2,
      commentType: 'REPLY',
      content: '请用户重新尝试登录。',
      internal: false
    })
    startTicketMock.mockResolvedValue({
      id: 8,
      ticketNo: 'TK-20260620-0001',
      title: '无法登录后台',
      status: 'PROCESSING'
    })
    resolveTicketMock.mockResolvedValue({
      id: 8,
      ticketNo: 'TK-20260620-0001',
      title: '无法登录后台',
      status: 'RESOLVED'
    })

    const wrapper = mount(TicketDetailView)
    await flushPromises()

    expect(wrapper.text()).toContain('TK-20260620-0001')
    expect(wrapper.text()).toContain('无法登录后台')
    expect(wrapper.text()).toContain('用户反馈后台登录失败。')
    expect(wrapper.text()).toContain('AI 置信度低，需要人工处理')
    expect(wrapper.text()).toContain('AI 会话转入工单')
    expect(wrapper.text()).toContain('已收到，正在排查。')
    expect(wrapper.text()).toContain('内部备注：疑似账号锁定。')
    expect(wrapper.text()).toContain('开始处理')
    expect(wrapper.text()).toContain('标记解决')

    await wrapper.find('[data-testid="action-comment"]').setValue('开始处理这个工单')
    await wrapper.find('[data-testid="start-ticket"]').trigger('click')
    await flushPromises()

    expect(startTicketMock).toHaveBeenCalledWith(8, '开始处理这个工单')
  })
})
