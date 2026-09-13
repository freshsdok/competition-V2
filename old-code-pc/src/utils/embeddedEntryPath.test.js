import { describe, expect, it } from 'vitest'
import { isAllowedEmbeddedEntryPath } from './embeddedEntryPath'

describe('embedded entry allowlist', () => {
  it('accepts only fixed same-origin V2 capability paths', () => {
    expect(isAllowedEmbeddedEntryPath(`/v2-embedded/embedded/settlement-profile#bootstrap=${'a'.repeat(43)}`)).toBe(true)
    expect(isAllowedEmbeddedEntryPath(`/v2-embedded/embedded/credential-exchange/7#bootstrap=${'b'.repeat(43)}`)).toBe(true)
    expect(isAllowedEmbeddedEntryPath(`https://attacker.test/v2-embedded/embedded/settlement-profile#bootstrap=${'a'.repeat(43)}`)).toBe(false)
    expect(isAllowedEmbeddedEntryPath('/v2-embedded/embedded/../admin#bootstrap=' + 'a'.repeat(43))).toBe(false)
  })
})
