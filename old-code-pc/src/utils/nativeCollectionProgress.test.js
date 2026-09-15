import { afterEach, describe, expect, it, vi } from 'vitest'
import {
  createNativeProgressNonce,
  parseNativeProgressMessage,
  verifyNativeSettlementProgress,
} from './nativeCollectionProgress'

const origin = 'http://localhost:8082'
const frameWindow = {}
const nonce = 'a1'.repeat(16)
const intent = 'n1.' + 'a'.repeat(70)
const entryPath = '/v2-native/native-settlement#intent=' + intent
const provided = { phase: 'provided', profileVersion: 4, tenantId: 7, accessVersion: 2 }
function event(data = {}, overrides = {}) {
  return {
    origin,
    source: frameWindow,
    data: { type: 'deshi:native-settlement:progress', protocolVersion: 1, channel: nonce, ...provided, ...data },
    ...overrides,
  }
}
function parse(message) {
  return parseNativeProgressMessage(message, frameWindow, nonce, origin)
}
function profile(overrides = {}) {
  return {
    subjectId: 11,
    version: 4,
    profile: { id: 19, maskedName: 'synthetic-masked-name' },
    tenantAccess: [{ tenantId: 7, status: 'PROVIDED', version: 2 }],
    ...overrides,
  }
}
function response(value = profile()) {
  return { ok: true, json: vi.fn().mockResolvedValue(value) }
}

afterEach(() => { vi.restoreAllMocks(); vi.useRealTimers() })

describe('native collection progress messages', () => {
  it('generates a 16-byte cryptographic nonce without a random-number fallback', () => {
    const random = vi.spyOn(globalThis.crypto, 'getRandomValues').mockImplementation(bytes => {
      expect(bytes).toBeInstanceOf(Uint8Array)
      expect(bytes).toHaveLength(16)
      bytes.set(Array.from({ length: 16 }, (_, index) => index))
      return bytes
    })
    expect(createNativeProgressNonce()).toBe('000102030405060708090a0b0c0d0e0f')
    expect(random).toHaveBeenCalledOnce()
  })

  it('only returns allowed state fields and discards supplied identity or bank data', () => {
    expect(parse(event({ phone: 'synthetic-phone', bankAccountNumber: 'synthetic-bank', subjectId: 123 }))).toEqual(provided)
    for (const phase of ['editing', 'review', 'unavailable']) {
      expect(parse(event({ phase, phone: 'synthetic-phone' }))).toEqual({ phase })
    }
  })

  it('rejects other origins, frames, channels, and protocol versions', () => {
    for (const invalid of [
      event({}, { origin: 'https://other.example' }),
      event({}, { origin: 'null' }),
      event({}, { source: {} }),
      event({}, { source: null }),
      event({ channel: 'b'.repeat(32) }),
      event({ channel: nonce + 'a' }),
      event({ protocolVersion: 2 }),
      event({ protocolVersion: '1' }),
      event({ protocolVersion: undefined }),
      event({ type: 'unrelated' }),
      event({ phase: 'complete' }),
    ]) expect(parse(invalid)).toBeNull()
    expect(parseNativeProgressMessage(event(), null, nonce, origin)).toBeNull()
    expect(parseNativeProgressMessage(event(), frameWindow, '', origin)).toBeNull()
    expect(parseNativeProgressMessage(event({ channel: 'z'.repeat(32) }), frameWindow, 'z'.repeat(32), origin)).toBeNull()
  })

  it('rejects malformed messages and unsafe or missing versions', () => {
    for (const invalid of [null, {}, { data: null }, event({}, { data: null }), event({}, { data: [] }), event({}, { data: 'provided' })]) {
      expect(parse(invalid)).toBeNull()
    }
    for (const field of ['profileVersion', 'tenantId', 'accessVersion']) {
      for (const value of [undefined, null, '1', -1, 1.5, NaN, Infinity, Number.MAX_SAFE_INTEGER + 1]) {
        expect(parse(event({ [field]: value }))).toBeNull()
      }
    }
    expect(parse(event({ tenantId: 0 }))).toBeNull()
    expect(parse(event({ profileVersion: 0 }))).toBeNull()
    expect(parse(event({ accessVersion: 0 }))).toEqual({ ...provided, accessVersion: 0 })
    expect(parse(event({ profileVersion: Number.MAX_SAFE_INTEGER }))).toEqual({ ...provided, profileVersion: Number.MAX_SAFE_INTEGER })
  })

  it('defaults to the current page origin and does not throw on unreadable input', () => {
    expect(parseNativeProgressMessage(event({}, { origin: window.location.origin }), frameWindow, nonce)).toEqual(provided)
    const unreadable = Object.defineProperty({ origin, source: frameWindow }, 'data', { get() { throw new Error('synthetic secret') } })
    expect(parseNativeProgressMessage(unreadable, frameWindow, nonce, origin)).toBeNull()
  })
})

describe('native collection progress server verification', () => {
  it('rechecks the current guarded profile and exact unique tenant access before returning only version evidence', async () => {
    const fetcher = vi.fn().mockResolvedValue(response())
    await expect(verifyNativeSettlementProgress(entryPath, { ...provided, bankAccountNumber: 'synthetic-bank' }, fetcher)).resolves.toEqual(provided)
    expect(fetcher).toHaveBeenCalledWith('/v2-native-api/api/v1/settlement-profiles/me', {
      method: 'GET', credentials: 'include', cache: 'no-store', redirect: 'error',
      signal: expect.any(AbortSignal),
      headers: { Accept: 'application/json', 'X-V2-Native-Settlement-Intent': intent },
    })
  })

  it('blocks external, malformed, legacy and query-token entry paths before any fetch', async () => {
    const fetcher = vi.fn()
    for (const invalid of [null, '', 'https://other.example' + entryPath, '//other.example' + entryPath,
      entryPath + '&phone=synthetic', entryPath.replace('#intent=', '?intent='),
      '/v2-embedded/embedded/settlement-profile#bootstrap=synthetic', '/v2-native/native-settlement#intent=invalid']) {
      await expect(verifyNativeSettlementProgress(invalid, provided, fetcher)).rejects.toThrow('NATIVE_PROGRESS_UNVERIFIED')
    }
    expect(fetcher).not.toHaveBeenCalled()
  })

  it('rejects unprovided or malformed progress before any fetch', async () => {
    const fetcher = vi.fn()
    for (const invalid of [null, {}, { phase: 'review' }, { ...provided, profileVersion: 0 },
      { ...provided, tenantId: 0 }, { ...provided, accessVersion: -1 }, { ...provided, profileVersion: '4' }]) {
      await expect(verifyNativeSettlementProgress(entryPath, invalid, fetcher)).rejects.toThrow('NATIVE_PROGRESS_UNVERIFIED')
    }
    expect(fetcher).not.toHaveBeenCalled()
  })

  it('rejects absent profiles, envelopes, mismatched versions, revoked and duplicate recipient access', async () => {
    for (const invalid of [
      null, [], { data: profile() }, profile({ profile: null }), profile({ profile: false }),
      profile({ version: 5 }), profile({ version: '4' }), profile({ tenantAccess: null }),
      profile({ tenantAccess: [] }),
      profile({ tenantAccess: [{ tenantId: 8, status: 'PROVIDED', version: 2 }] }),
      profile({ tenantAccess: [{ tenantId: 7, status: 'REVOKED', version: 2 }] }),
      profile({ tenantAccess: [{ tenantId: 7, status: 'PROVIDED', version: 3 }] }),
      profile({ tenantAccess: [{ tenantId: 7, status: 'PROVIDED', version: '2' }] }),
      profile({ tenantAccess: [{ tenantId: 7, status: 'PROVIDED', version: 2 }, { tenantId: 7, status: 'REVOKED', version: 1 }] }),
    ]) {
      const fetcher = vi.fn().mockResolvedValue(response(invalid))
      await expect(verifyNativeSettlementProgress(entryPath, provided, fetcher)).rejects.toThrow('NATIVE_PROGRESS_UNVERIFIED')
    }
  })

  it('allows unrelated recipient access while requiring the selected recipient exact version', async () => {
    const fetcher = vi.fn().mockResolvedValue(response(profile({ tenantAccess: [
      { tenantId: 8, status: 'REVOKED', version: 5 }, { tenantId: 7, status: 'PROVIDED', version: 2 },
    ] })))
    await expect(verifyNativeSettlementProgress(entryPath, provided, fetcher)).resolves.toEqual(provided)
  })

  it('normalizes HTTP, network and JSON failures without exposing response or intent values', async () => {
    const json = vi.fn().mockResolvedValue({ detail: 'synthetic secret' })
    for (const fetcher of [
      vi.fn().mockResolvedValue({ ok: false, status: 403, json }),
      vi.fn().mockRejectedValue(new Error(intent)),
      vi.fn().mockResolvedValue({ ok: true, json: vi.fn().mockRejectedValue(new Error('synthetic secret')) }),
      vi.fn().mockResolvedValue(null),
    ]) {
      await expect(verifyNativeSettlementProgress(entryPath, provided, fetcher)).rejects.toEqual(new Error('NATIVE_PROGRESS_UNVERIFIED'))
    }
    expect(json).not.toHaveBeenCalled()
  })

  it('times out and aborts a fetch that never settles, even when the transport ignores abort', async () => {
    vi.useFakeTimers()
    const fetcher = vi.fn().mockImplementation(() => new Promise(() => {}))
    const result = verifyNativeSettlementProgress(entryPath, provided, fetcher)
    const rejected = expect(result).rejects.toEqual(new Error('NATIVE_PROGRESS_UNVERIFIED'))
    const signal = fetcher.mock.calls[0][1].signal
    expect(signal.aborted).toBe(false)
    await vi.advanceTimersByTimeAsync(14999)
    expect(signal.aborted).toBe(false)
    await vi.advanceTimersByTimeAsync(1)
    await rejected
    expect(signal.aborted).toBe(true)
    expect(vi.getTimerCount()).toBe(0)
  })

  it('also bounds a response body that never finishes parsing', async () => {
    vi.useFakeTimers()
    const json = vi.fn().mockImplementation(() => new Promise(() => {}))
    const fetcher = vi.fn().mockResolvedValue({ ok: true, json })
    const result = verifyNativeSettlementProgress(entryPath, provided, fetcher)
    const rejected = expect(result).rejects.toEqual(new Error('NATIVE_PROGRESS_UNVERIFIED'))
    await vi.advanceTimersByTimeAsync(15000)
    await rejected
    expect(json).toHaveBeenCalledOnce()
    expect(fetcher.mock.calls[0][1].signal.aborted).toBe(true)
    expect(vi.getTimerCount()).toBe(0)
  })

  it('clears the timeout after successful verification without aborting later', async () => {
    vi.useFakeTimers()
    const fetcher = vi.fn().mockResolvedValue(response())
    await expect(verifyNativeSettlementProgress(entryPath, provided, fetcher)).resolves.toEqual(provided)
    const signal = fetcher.mock.calls[0][1].signal
    expect(vi.getTimerCount()).toBe(0)
    await vi.advanceTimersByTimeAsync(15000)
    expect(signal.aborted).toBe(false)
  })
})
