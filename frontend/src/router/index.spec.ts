import { describe, expect, it } from 'vitest'
import { routes } from './index'

describe('routes', () => {
  it('exposes public, login, and app routes', () => {
    expect(routes.map((route) => route.path)).toEqual(expect.arrayContaining(['/', '/login', '/app']))
  })
})
