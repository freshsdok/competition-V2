/**
 * Production transitions require HTTPS. Local end-to-end development may use
 * HTTP only on an explicit loopback host; LAN and public HTTP remain blocked.
 */
export function isAllowedPayoutTransitionUrl(value) {
  if (typeof value !== 'string' || !value.trim()) return false
  try {
    const url = new URL(value)
    if (url.protocol === 'https:') return true
    return url.protocol === 'http:' && ['localhost', '127.0.0.1', '[::1]'].includes(url.hostname)
  } catch {
    return false
  }
}
