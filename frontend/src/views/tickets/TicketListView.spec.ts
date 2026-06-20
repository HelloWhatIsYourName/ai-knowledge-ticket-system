import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import TicketListView from './TicketListView.vue'
import { listMyTickets } from '../../api/tickets'

vi.mock('../../api/tickets', () => ({
  listMyTickets: vi.fn()
}))

const listMyTicketsMock = vi.mocked(listMyTickets)

describe('TicketListView', () => {
  it('renders my tickets from the API', async () => {
    listMyTicketsMock.mockResolvedValue([
      {
        id: 1,
        ticketNo: 'TK-20260620-0001',
        title: '无法登录后台',
        status: 'PENDING',
        priority: 'HIGH',
        source: 'AI_SESSION',
        transferReason: 'AI 置信度低，需要人工处理',
        createdAt: '2026-06-20T10:00:00'
      }
    ])

    const wrapper = mount(TicketListView)
    await flushPromises()

    expect(wrapper.text()).toContain('我的工单')
    expect(wrapper.text()).toContain('TK-20260620-0001')
    expect(wrapper.text()).toContain('无法登录后台')
    expect(wrapper.text()).toContain('待处理')
    expect(wrapper.text()).toContain('高')
    expect(wrapper.text()).toContain('AI 置信度低，需要人工处理')
  })
})
