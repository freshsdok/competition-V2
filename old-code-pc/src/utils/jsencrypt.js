import { JSEncrypt } from 'jsencrypt'

// 密钥对生成 http://web.chacuo.net/netrsakeypair

const publicKey = 'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCL/ITLMWZZ1kXmWyxjxPVD30ica9+Mcm7eSvBI+MBgzbPdx8EEGGgUqbJoIx7ecx17sqKt2L0nSxn1ob3CdIqc1/DmWXEVZsr4WDdo7R71hmiL3q1Tb6Eyv87Wb7T4+Jk6j43+3OC8nh2MtRs0bDPclFCDKYHXAIWuDlvWxSdQzwIDAQAB'

const privateKey = 'MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAIv8hMsxZlnWReZbLGPE9UPfSJxr34xybt5K8Ej4wGDNs93HwQQYaBSpsmgjHt5zHXuyoq3YvSdLGfWhvcJ0ipzX8OZZcRVmyvhYN2jtHvWGaIverVNvoTK/ztZvtPj4mTqPjf7c4LyeHYy1GzRsM9yUUIMpgdcAha4OW9bFJ1DPAgMBAAECgYAQjPwUXBRaVcuw5yGx8BUBf9JBcD2fiN4T2S9cqVBxgZCdDaOD/PC9VKz7w/8/1MNtHxs9y6zdivMYSBW7+nRy1tCtVfEBhoKBabs7XjMDG1dZlB1DxjTqiXUtStwPEftMjYDNctnmsQTmVYUc5DEdbIN3dMNArMCEDHdOZwUEcQJBAKp82wfhir/x3au8WshAxOcIoin7A5QLbEE7tP+IfEzQu2tjDoOo/Er5PRa5uVZx568M0JBdpd5aLmuU3TVdJN8CQQDSMzNscWDftOE+p8lLMlzAIkguWZtaHecwuoVcxLs+saH8sPpvzf496VeEfytA5+oJwl5stslXpzfo1ysK9eIRAkEAk1EsGtVDhbTDrUAm2d9Nxa1qIwhqASUVuBCVyDDx55Z+PL7trcr5pvdTWC3H/vCSGGrkVbr2NvqKHiAWPaRs1QJBAKEOmB1ENizSZC/k0chrO0QAQHw7Llx7QxREJkExgCMGag667/jQxjhb3THpWpPt3pZBtqXn3BfsSVt/2jwYsHECQAPzEKvdV6/r8FO1RKhQWWMgSFGlRV+3T8zS6z3oiNQcjRFguRZ6q3bv31ohY0or/mBzGHIyKo1DEMl7iLJ/zyM='

// 加密
export function encrypt(txt) {
  const encryptor = new JSEncrypt()
  encryptor.setPublicKey(publicKey) // 设置公钥
  return encryptor.encrypt(txt) // 对数据进行加密
}

// 解密
export function decrypt(txt) {
  const encryptor = new JSEncrypt()
  encryptor.setPrivateKey(privateKey) // 设置私钥
  if(txt){
      return encryptor.decrypt(txt) // 对数据进行解密
  }else{
    return ''
  }

}

