import request from '@/utils/request'
export const discoverCredentials = () => request({ url: '/system/v2CredentialBridge/discovery', method: 'get' })
export const enterCredentialExchange = () => request({ url: '/system/v2CredentialBridge/entry', method: 'post' })
