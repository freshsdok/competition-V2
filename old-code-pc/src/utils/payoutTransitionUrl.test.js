import { describe, expect, it } from 'vitest'
import { isAllowedPayoutTransitionUrl } from './payoutTransitionUrl'

describe('payout transition URL policy', () => {
  it('accepts HTTPS', () => {
    expect(isAllowedPayoutTransitionUrl('https://v2.example.test/payout/transition?code=x')).toBe(true)
  })

  it('accepts loopback HTTP for local end-to-end development', () => {
    expect(isAllowedPayoutTransitionUrl('http://localhost:5174/payout/transition?code=x')).toBe(true)
    expect(isAllowedPayoutTransitionUrl('http://127.0.0.1:5174/payout/transition?code=x')).toBe(true)
    expect(isAllowedPayoutTransitionUrl('http://[::1]:5174/payout/transition?code=x')).toBe(true)
  })

  it('rejects non-loopback HTTP and malformed values', () => {
    expect(isAllowedPayoutTransitionUrl('http://192.168.1.20:5174/payout/transition?code=x')).toBe(false)
    expect(isAllowedPayoutTransitionUrl('javascript:alert(1)')).toBe(false)
    expect(isAllowedPayoutTransitionUrl('')).toBe(false)
  })
})
