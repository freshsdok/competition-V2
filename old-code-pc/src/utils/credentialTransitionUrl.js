export function isAllowedCredentialTransitionUrl(value, development = false) {
  try {
    const url = new URL(value)
    const localHttp = development && url.protocol === 'http:'
      && ['localhost', '127.0.0.1', '[::1]'].includes(url.hostname)
    return (url.protocol === 'https:' || localHttp)
      && !url.username && !url.password && !url.search
      && url.pathname.endsWith('/credential/transition')
      && /^[A-Za-z0-9_-]{43}$/.test(new URLSearchParams(url.hash.slice(1)).get('code') ?? '')
  } catch { return false }
}
