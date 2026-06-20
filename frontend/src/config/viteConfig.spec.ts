import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'

describe('vite dev server config', () => {
  it('proxies API requests to the local backend during development', () => {
    const configSource = readFileSync('vite.config.ts', 'utf8')

    expect(configSource).toContain("'/api'")
    expect(configSource).toContain("target: 'http://127.0.0.1:8080'")
    expect(configSource).toContain('changeOrigin: true')
  })
})
