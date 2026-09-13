/** 只接受服务端返回的V1同源固定前缀，禁止将任意URL写入iframe。 */
export function isAllowedEmbeddedEntryPath(value) {
  if (typeof value !== 'string' || !value.startsWith('/v2-embedded/embedded/')) return false
  if (value.includes('://') || value.includes('\\') || value.includes('..')) return false
  try {
    const url = new URL(value, window.location.origin)
    return url.origin === window.location.origin
      && /^\/v2-embedded\/embedded\/(settlement-profile|credential-exchange\/[1-9][0-9]*)$/.test(url.pathname)
      && /^#bootstrap=[A-Za-z0-9_-]{43}$/.test(url.hash)
  } catch { return false }
}
