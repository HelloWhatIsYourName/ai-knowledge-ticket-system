import { http, unwrapData, type ApiResponse } from './http'

export interface AdminDashboardOverview {
  totalTickets: number
  pendingTickets: number
  processingTickets: number
  resolvedTickets: number
  closedTickets: number
  averageResolveHours: number
  knowledgeDocuments: number
  aiQuestions: number
  knowledgeHitRate: number
}

export interface TicketCategoryStat {
  categoryId: number | null
  categoryName: string
  ticketCount: number
}

export interface HotQuestionStat {
  question: string
  askCount: number
}

export async function getOverview(): Promise<AdminDashboardOverview> {
  const response = await http.get<ApiResponse<AdminDashboardOverview>>('/admin/statistics/overview')

  return unwrapData(response.data)
}

export async function getTicketCategoryStats(limit = 10): Promise<TicketCategoryStat[]> {
  const response = await http.get<ApiResponse<TicketCategoryStat[]>>('/admin/statistics/ticket-categories', {
    params: { limit }
  })

  return unwrapData(response.data)
}

export async function getHotQuestions(limit = 10): Promise<HotQuestionStat[]> {
  const response = await http.get<ApiResponse<HotQuestionStat[]>>('/admin/statistics/hot-questions', {
    params: { limit }
  })

  return unwrapData(response.data)
}
