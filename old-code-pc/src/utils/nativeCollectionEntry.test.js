import { expect, it } from 'vitest'
import { isAllowedNativeCollectionEntry as allowed } from './nativeCollectionEntry'
it('accepts only the independent fragment entry, never legacy or external URLs', () => {
  const valid='/v2-native/native-settlement#intent=n1.'+'a'.repeat(70)
  expect(allowed(valid)).toBe(true)
  for(const value of [null, valid+'&userId=1', 'https://evil.test'+valid, '//evil.test'+valid,
    '/v2-embedded/embedded/settlement-profile#bootstrap='+'a'.repeat(43),valid.replace('#intent=', '?intent=')]) expect(allowed(value)).toBe(false)
})
