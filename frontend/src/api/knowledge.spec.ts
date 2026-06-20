import { beforeEach, describe, expect, it, vi } from 'vitest'
import { http } from './http'
import { createTextDocument, listDocumentChunks, listDocuments, searchKnowledge } from './knowledge'

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

describe('knowledge api', () => {
  beforeEach(() => {
    getMock.mockReset()
    postMock.mockReset()
  })

  it('loads knowledge documents', async () => {
    getMock.mockResolvedValueOnce({
      data: {
        success: true,
        data: [{ id: 1, title: '账号手册', parseStatus: 'PARSED', enabled: true }],
        message: 'ok'
      }
    })

    await expect(listDocuments()).resolves.toHaveLength(1)
    expect(getMock).toHaveBeenCalledWith('/kb/documents')
  })

  it('creates text documents', async () => {
    postMock.mockResolvedValueOnce({
      data: {
        success: true,
        data: { id: 2, title: '账号手册', parseStatus: 'PARSED', enabled: true },
        message: 'ok'
      }
    })

    await expect(
      createTextDocument({ title: '账号手册', content: '重置密码步骤', categoryId: 1 })
    ).resolves.toMatchObject({ id: 2, title: '账号手册' })
    expect(postMock).toHaveBeenCalledWith('/kb/documents/text', {
      title: '账号手册',
      content: '重置密码步骤',
      categoryId: 1
    })
  })

  it('searches knowledge chunks', async () => {
    postMock.mockResolvedValueOnce({
      data: {
        success: true,
        data: [{ chunkId: 8, documentId: 2, sourceTitle: '账号手册', content: '重置密码步骤', similarity: 0.91 }],
        message: 'ok'
      }
    })

    await expect(searchKnowledge({ query: '重置密码', topK: 4 })).resolves.toHaveLength(1)
    expect(postMock).toHaveBeenCalledWith('/kb/search', { query: '重置密码', topK: 4 })
  })

  it('loads document chunks', async () => {
    getMock.mockResolvedValueOnce({
      data: {
        success: true,
        data: [{ id: 3, chunkIndex: 0, content: '重置密码步骤', sourceTitle: '账号手册' }],
        message: 'ok'
      }
    })

    await expect(listDocumentChunks(2)).resolves.toHaveLength(1)
    expect(getMock).toHaveBeenCalledWith('/kb/documents/2/chunks')
  })
})
