import { isAllowedNativeCollectionEntry } from './nativeCollectionEntry'

const MESSAGE_TYPE = 'deshi:native-settlement:progress'
const NONCE_PATTERN = /^[a-f0-9]{32}$/i
const PHASES = new Set(['editing', 'review', 'provided', 'unavailable'])

const isRecord = value => value !== null && typeof value === 'object' && !Array.isArray(value)
const isVersion = value => Number.isSafeInteger(value) && value >= 0

function cleanProgress(value) {
  if (!isRecord(value) || !PHASES.has(value.phase)) return null
  if (value.phase !== 'provided') return { phase: value.phase }
  if (!isVersion(value.profileVersion) || value.profileVersion <= 0 || !Number.isSafeInteger(value.tenantId)
    || value.tenantId <= 0 || !isVersion(value.accessVersion)) return null
  return {
    phase: 'provided',
    profileVersion: value.profileVersion,
    tenantId: value.tenantId,
    accessVersion: value.accessVersion,
  }
}

/** 每次打开窗口使用独立随机通道；此值只关联页面，不充当认证凭据。 */
export function createNativeProgressNonce() {
  if (typeof globalThis.crypto?.getRandomValues !== 'function') throw new Error('NATIVE_PROGRESS_UNAVAILABLE')
  const bytes = globalThis.crypto.getRandomValues(new Uint8Array(16))
  return Array.from(bytes, byte => byte.toString(16).padStart(2, '0')).join('')
}

/** 消息只是待核验进度，只保留界面状态和版本标识，不接收收款资料。 */
export function parseNativeProgressMessage(event, expectedWindow, expectedNonce, expectedOrigin = window.location.origin) {
  try {
    if (!event || !expectedWindow || typeof expectedOrigin !== 'string' || !expectedOrigin
      || event.origin !== expectedOrigin || event.source !== expectedWindow
      || typeof expectedNonce !== 'string' || !NONCE_PATTERN.test(expectedNonce)) return null
    const data = event.data
    if (!isRecord(data) || data.type !== MESSAGE_TYPE || data.protocolVersion !== 1
      || data.channel !== expectedNonce) return null
    return cleanProgress(data)
  } catch {
    return null
  }
}

/** 重新读取受 Native Identity Guard 保护的本人视图；窗口消息不能证明资料已提供。 */
export async function verifyNativeSettlementProgress(entryPath, progress, fetcher = fetch) {
  let timeout
  try {
    if (!isAllowedNativeCollectionEntry(entryPath)) throw new Error()
    const candidate = cleanProgress(progress)
    if (candidate?.phase !== 'provided') throw new Error()
    const intent = entryPath.split('#intent=')[1]
    const controller = new AbortController()
    const deadline = new Promise((_, reject) => {
      timeout = setTimeout(() => {
        controller.abort()
        reject(new Error('NATIVE_PROGRESS_UNVERIFIED'))
      }, 15000)
    })
    const verification = (async () => {
      const response = await fetcher('/v2-native-api/api/v1/settlement-profiles/me', {
        method: 'GET',
        credentials: 'include',
        cache: 'no-store',
        redirect: 'error',
        signal: controller.signal,
        headers: {
          Accept: 'application/json',
          'X-V2-Native-Settlement-Intent': intent,
        },
      })
      if (response?.ok !== true) throw new Error()
      // SettlementProfileController returns the View directly, without a data envelope.
      const result = await response.json()
      if (!isRecord(result) || !isRecord(result.profile) || result.version !== candidate.profileVersion
        || !Array.isArray(result.tenantAccess)) throw new Error()
      const matchingAccess = result.tenantAccess.filter(access => isRecord(access) && access.tenantId === candidate.tenantId)
      if (matchingAccess.length !== 1 || matchingAccess[0].status !== 'PROVIDED'
        || matchingAccess[0].version !== candidate.accessVersion) throw new Error()
      return candidate
    })()
    // Bound both network and response-body waits, even if a transport ignores abort.
    return await Promise.race([verification, deadline])
  } catch {
    // Never propagate a response body, token, contact value, or fetch error into the host UI.
    throw new Error('NATIVE_PROGRESS_UNVERIFIED')
  } finally {
    clearTimeout(timeout)
  }
}
