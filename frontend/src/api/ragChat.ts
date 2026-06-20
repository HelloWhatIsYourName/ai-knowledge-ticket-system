import { http, unwrapData, type ApiResponse } from './http'

export interface AskQuestionRequest {
  question: string
  sessionId?: number
  categoryId?: number
  topK?: number
  minSimilarity?: number
}

export interface RagCitation {
  citationIndex: number
  chunkId?: number
  documentId?: number
  categoryId?: number
  sourceTitle: string
  similarity?: number
  snippet: string
}

export interface RagAnswerResponse {
  sessionId: number
  userMessageId: number
  assistantMessageId: number
  answer: string
  canAnswer: boolean
  confidence: number
  transferSuggested: boolean
  transferReason?: string | null
  citations: RagCitation[]
}

export interface AiSession {
  id: number
  title: string
  lastQuestion?: string | null
  transferSuggested?: boolean | null
  createdAt?: string
  updatedAt?: string
}

export type AiMessageRole = 'USER' | 'ASSISTANT'

export interface AiMessage {
  id: number
  sessionId: number
  role: AiMessageRole
  content: string
  modelName?: string | null
  canAnswer?: boolean | null
  confidence?: number | null
  transferSuggested?: boolean | null
  transferReason?: string | null
  createdAt?: string
  citations?: RagCitation[]
}

export async function askQuestion(request: AskQuestionRequest): Promise<RagAnswerResponse> {
  const response = await http.post<ApiResponse<RagAnswerResponse>>('/ai/chat/ask', request)

  return unwrapData(response.data)
}

export async function listSessions(): Promise<AiSession[]> {
  const response = await http.get<ApiResponse<AiSession[]>>('/ai/chat/sessions')

  return unwrapData(response.data)
}

export async function listSessionMessages(sessionId: number): Promise<AiMessage[]> {
  const response = await http.get<ApiResponse<AiMessage[]>>(`/ai/chat/sessions/${sessionId}/messages`)

  return unwrapData(response.data)
}
