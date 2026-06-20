import { beforeEach, describe, expect, it, vi } from 'vitest'
import { http } from './http'
import { getHotQuestions, getOverview, getTicketCategoryStats } from './adminStatistics'

vi.mock('./http', async () => {
  const actual = await vi.importActual<typeof import('./http')>('./http')

  return {
    ...actual,
    http: {
      get: vi.fn()
    }
  }
})

const getMock = vi.mocked(http.get)

describe('adminStatistics api', () => {
  beforeEach(() => {
    getMock.mockReset()
  })

  it('calls overview endpoint and unwraps ApiResponse data', async () => {
    getMock.mockResolvedValueOnce({
      data: {
        success: true,
        data: { totalTickets: 12, pendingTickets: 3 },
        message: 'ok'
      }
    })

    await expect(getOverview()).resolves.toMatchObject({ totalTickets: 12, pendingTickets: 3 })
    expect(getMock).toHaveBeenCalledWith('/admin/statistics/overview')
  })

  it('calls category and hot question endpoints with limits', async () => {
    getMock
      .mockResolvedValueOnce({
        data: {
          success: true,
          data: [{ categoryId: 1, categoryName: '账号', ticketCount: 4 }],
          message: 'ok'
        }
      })
      .mockResolvedValueOnce({
        data: {
          success: true,
          data: [{ question: '如何重置密码？', askCount: 7 }],
          message: 'ok'
        }
      })

    await expect(getTicketCategoryStats(5)).resolves.toHaveLength(1)
    await expect(getHotQuestions(6)).resolves.toHaveLength(1)
    expect(getMock).toHaveBeenNthCalledWith(1, '/admin/statistics/ticket-categories', { params: { limit: 5 } })
    expect(getMock).toHaveBeenNthCalledWith(2, '/admin/statistics/hot-questions', { params: { limit: 6 } })
  })
})
