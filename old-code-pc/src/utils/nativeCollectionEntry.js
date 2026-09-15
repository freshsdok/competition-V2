export function isAllowedNativeCollectionEntry(value) {
  return typeof value==='string' && /^\/v2-native\/native-settlement#intent=n1\.[A-Za-z0-9_-]{70}$/.test(value)
}
