import { beforeEach, describe, expect, it, vi } from 'vitest'
import { http } from './http'
import {
  createTicketFromAiSession,
  listAssignedTickets,
  listMyTickets,
  listTicketCategories
} from './tickets'

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

describe('tickets api', () => {
  beforeEach(() => {
    getMock.mockReset()
    postMock.mockReset()
  })

  it('loads enabled ticket categories', async () => {
    getMock.mockResolvedValueOnce({
      data: {
        success: true,
        data: [{ id: 1, name: '账号问题', enabled: true }],
        message: 'ok'
      }
    })

    await expect(listTicketCategories()).resolves.toEqual([{ id: 1, name: '账号问题', enabled: true }])
    expect(getMock).toHaveBeenCalledWith('/ticket-categories', { params: { includeDisabled: false } })
  })

  it('creates a ticket from an AI session', async () => {
    postMock.mockResolvedValueOnce({
      data: {
        success: true,
        data: { id: 8, ticketNo: 'TK-20260620-0001', title: '无法登录', status: 'PENDING' },
        message: 'ok'
      }
    })

    await expect(
      createTicketFromAiSession({
        sessionId: 7,
        assistantMessageId: 9,
        title: '无法登录',
        description: 'AI 建议转人工',
        priority: 'HIGH',
        transferReason: 'AI 置信度低'
      })
    ).resolves.toMatchObject({ id: 8, ticketNo: 'TK-20260620-0001' })
    expect(postMock).toHaveBeenCalledWith(
      '/tickets/from-ai-session',
      expect.objectContaining({ sessionId: 7, priority: 'HIGH' })
    )
  })

  it('loads my and assigned tickets', async () => {
    getMock
      .mockResolvedValueOnce({
        data: {
          success: true,
          data: [{ id: 1, ticketNo: 'TK-1', title: '我的工单' }],
          message: 'ok'
        }
      })
      .mockResolvedValueOnce({
        data: {
          success: true,
          data: [{ id: 2, ticketNo: 'TK-2', title: '待处理工单' }],
          message: 'ok'
        }
      })

    await expect(listMyTickets()).resolves.toHaveLength(1)
    await expect(listAssignedTickets()).resolves.toHaveLength(1)
    expect(getMock).toHaveBeenNthCalledWith(1, '/tickets/my')
    expect(getMock).toHaveBeenNthCalledWith(2, '/tickets/assigned')
  })
})
