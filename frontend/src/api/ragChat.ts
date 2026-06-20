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

export interface RagStreamHandlers {
  onToken?: (token: string) => void
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

export async function askQuestionStream(
  request: AskQuestionRequest,
  handlers: RagStreamHandlers = {}
): Promise<RagAnswerResponse> {
  const response = await fetch(buildStreamUrl(request), {
    method: 'GET',
    headers: authHeaders()
  })

  if (!response.ok || !response.body) {
    throw new Error('RAG stream request failed')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let metadata: RagAnswerResponse | null = null

  const consumeBlock = (block: string) => {
    const event = parseSseBlock(block)

    if (!event) {
      return
    }

    if (event.name === 'token') {
      handlers.onToken?.(event.data)
    }

    if (event.name === 'metadata') {
      metadata = JSON.parse(event.data) as RagAnswerResponse
    }

    if (event.name === 'error') {
      throw new Error(event.data || 'RAG stream failed')
    }
  }

  while (true) {
    const { value, done } = await reader.read()

    if (done) {
      break
    }

    buffer += decoder.decode(value, { stream: true })
    const blocks = buffer.split(/\r?\n\r?\n/)
    buffer = blocks.pop() ?? ''
    blocks.forEach(consumeBlock)
  }

  buffer += decoder.decode()
  if (buffer.trim()) {
    consumeBlock(buffer)
  }

  if (!metadata) {
    throw new Error('RAG stream metadata missing')
  }

  return metadata
}

export async function listSessions(): Promise<AiSession[]> {
  const response = await http.get<ApiResponse<AiSession[]>>('/ai/chat/sessions')

  return unwrapData(response.data)
}

function buildStreamUrl(request: AskQuestionRequest): string {
  const params = new URLSearchParams()
  params.set('question', request.question)

  if (request.sessionId !== undefined) {
    params.set('sessionId', String(request.sessionId))
  }
  if (request.categoryId !== undefined) {
    params.set('categoryId', String(request.categoryId))
  }
  if (request.topK !== undefined) {
    params.set('topK', String(request.topK))
  }
  if (request.minSimilarity !== undefined) {
    params.set('minSimilarity', String(request.minSimilarity))
  }

  return `${apiBaseUrl()}/ai/chat/stream?${params.toString()}`
}

function apiBaseUrl(): string {
  return (import.meta.env.VITE_API_BASE_URL ?? '/api').replace(/\/$/, '')
}

function authHeaders(): Record<string, string> {
  const token = localStorage.getItem('akt_token')

  return token ? { Authorization: `Bearer ${token}` } : {}
}

function parseSseBlock(block: string): { name: string; data: string } | null {
  let name = 'message'
  const data: string[] = []

  for (const line of block.split(/\r?\n/)) {
    if (line.startsWith('event:')) {
      name = line.slice('event:'.length).trim()
    }
    if (line.startsWith('data:')) {
      data.push(line.slice('data:'.length).trimStart())
    }
  }

  if (data.length === 0) {
    return null
  }

  return { name, data: data.join('\n') }
}

export async function listSessionMessages(sessionId: number): Promise<AiMessage[]> {
  const response = await http.get<ApiResponse<AiMessage[]>>(`/ai/chat/sessions/${sessionId}/messages`)

  return unwrapData(response.data)
}
