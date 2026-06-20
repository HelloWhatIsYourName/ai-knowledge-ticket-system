import { http, unwrapData, type ApiResponse } from './http'

export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
export type TicketStatus = 'PENDING' | 'PROCESSING' | 'RESOLVED' | 'CLOSED'
export type TicketSource = 'MANUAL' | 'AI_SESSION'
export type TicketCommentType = 'REPLY' | 'INTERNAL_NOTE' | 'SYSTEM'

export interface TicketCategory {
  id: number
  name: string
  parentId?: number | null
  sortOrder?: number | null
  enabled: boolean
  createdAt?: string
  updatedAt?: string
}

export interface TicketSummary {
  id: number
  ticketNo: string
  title: string
  description?: string
  status: TicketStatus
  priority?: TicketPriority | null
  categoryId?: number | null
  creatorId?: number
  assigneeId?: number | null
  source?: TicketSource
  sourceSessionId?: number | null
  sourceMessageId?: number | null
  aiSummary?: string | null
  aiSuggestion?: string | null
  transferReason?: string | null
  reopenCount?: number
  firstResolvedAt?: string | null
  closedAt?: string | null
  createdAt?: string
  updatedAt?: string
}

export interface TicketFlowLog {
  id: number
  ticketId: number
  fromStatus?: TicketStatus | null
  toStatus?: TicketStatus | null
  action: string
  operatorId: number
  remark?: string | null
  createdAt?: string
}

export interface TicketDetail extends TicketSummary {
  flowLogs: TicketFlowLog[]
}

export interface TicketComment {
  id: number
  ticketId: number
  authorId: number
  commentType: TicketCommentType
  content: string
  internal: boolean
  createdAt?: string
}

export interface CreateTicketFromAiSessionRequest {
  sessionId: number
  assistantMessageId?: number
  title: string
  description: string
  categoryId?: number
  priority?: TicketPriority
  transferReason?: string
}

export interface CreateTicketCommentRequest {
  commentType: TicketCommentType
  content: string
}

export async function listTicketCategories(includeDisabled = false): Promise<TicketCategory[]> {
  const response = await http.get<ApiResponse<TicketCategory[]>>('/ticket-categories', {
    params: { includeDisabled }
  })

  return unwrapData(response.data)
}

export async function createTicketFromAiSession(request: CreateTicketFromAiSessionRequest): Promise<TicketSummary> {
  const response = await http.post<ApiResponse<TicketSummary>>('/tickets/from-ai-session', request)

  return unwrapData(response.data)
}

export async function listMyTickets(): Promise<TicketSummary[]> {
  const response = await http.get<ApiResponse<TicketSummary[]>>('/tickets/my')

  return unwrapData(response.data)
}

export async function listAssignedTickets(): Promise<TicketSummary[]> {
  const response = await http.get<ApiResponse<TicketSummary[]>>('/tickets/assigned')

  return unwrapData(response.data)
}

export async function getTicket(ticketId: number): Promise<TicketDetail> {
  const response = await http.get<ApiResponse<TicketDetail>>(`/tickets/${ticketId}`)

  return unwrapData(response.data)
}

export async function listTicketComments(ticketId: number): Promise<TicketComment[]> {
  const response = await http.get<ApiResponse<TicketComment[]>>(`/tickets/${ticketId}/comments`)

  return unwrapData(response.data)
}

export async function createTicketComment(
  ticketId: number,
  request: CreateTicketCommentRequest
): Promise<TicketComment> {
  const response = await http.post<ApiResponse<TicketComment>>(`/tickets/${ticketId}/comments`, request)

  return unwrapData(response.data)
}
