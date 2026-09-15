// 新入口使用独立API前缀与Cookie名称，不扩张旧/v2-embedded代理合同。
export const NATIVE_API='/v2-native-api'
export const NATIVE_SESSION='V2_NATIVE_SESSION'
export const NATIVE_CSRF='V2_NATIVE_XSRF'
export function allowedNativeRequest(method,path) {
  // 收款接收方目录与工作租户目录不同；只放行此精确GET，仍由下方Intent和V2登录链校验。
  if (method==='GET' && path==='/api/v1/settlement-profiles/me/recipient-tenants') return true
  if (method==='GET') return /^\/api\/v1\/(auth\/session(?:\/tenants)?|legal\/requirements|me(?:\/identity)?|me\/account-security-center|native-settlement\/context|settlement-profiles\/me)$/.test(path)
  if (method==='DELETE') return path==='/api/v1/auth/session'
  if (method==='POST') return /^\/api\/v1\/auth\/(password-sessions|sessions|email-sessions|sms-challenges|email-challenges|session\/step-up\/(sms|email|password))$/.test(path)
    || /^\/api\/v1\/settlement-profiles\/me\/(versions|tenant-access)$/.test(path)
  return false
}
export function nativeRequestCookies(raw='',sessionName='DESHI_SESSION') {
  return raw.split(';').map(v=>v.trim()).flatMap(v=>{
    if(v.startsWith(NATIVE_SESSION+'='))return [sessionName+v.slice(NATIVE_SESSION.length)]
    if(v.startsWith(NATIVE_CSRF+'='))return ['XSRF-TOKEN'+v.slice(NATIVE_CSRF.length)]
    return []
  }).join('; ')
}
export function nativeResponseCookie(raw,sessionName='DESHI_SESSION') {
  let name=raw.slice(0,raw.indexOf('='))
  if(name!==sessionName&&name!=='XSRF-TOKEN')return null
  const mapped=name===sessionName?NATIVE_SESSION:NATIVE_CSRF
  return (mapped+raw.slice(name.length)).replace(/;\s*Domain=[^;]+/ig,'').replace(/;\s*Path=[^;]+/ig,'')+'; Path=/'
}
export function nativeWebUnavailable(_error,req,res) {
  // Vite invokes configured listeners before its default empty-500 handler.
  // WebSocket errors have a Socket instead of ServerResponse; leave those to Vite.
  if(!res || typeof res.writeHead!=='function' || res.headersSent || res.writableEnded)return
  const documentRequest=req.headers?.accept?.includes('text/html')
    || ['iframe','document'].includes(req.headers?.['sec-fetch-dest'])
    || /^\/v2-native\/(native-settlement|login)(?:[?#]|$)/.test(req.url||'')
  res.writeHead(503,{'Content-Type':documentRequest?'text/html; charset=utf-8':'application/problem+json; charset=utf-8','Cache-Control':'no-store'})
  // Static content only: never echo upstream errors, request URLs or entry credentials.
  res.end(documentRequest
    ? '<!doctype html><html lang="zh-CN"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>填报页面暂不可用</title><body style="font-family:system-ui;max-width:640px;margin:48px auto;padding:24px;color:#243247"><h1>填报页面暂时无法打开</h1><p>页面服务暂不可用，请稍后返回上方办理页，点击“继续填报”。</p><p>您的办理名额仍然保留，此错误不会自动放弃办理或确认完成。</p></body></html>'
    : JSON.stringify({status:503,code:'INTERNAL_DEPENDENCY_UNAVAILABLE',title:'填报页面暂不可用'}))
}
export function nativeProxy(env) {
  const sessionName=env.VITE_V2_NATIVE_SESSION_COOKIE_NAME||'DESHI_SESSION'
  return {
    '/v2-native/': {target:env.VITE_V2_NATIVE_WEB_TARGET||'http://127.0.0.1:5177',changeOrigin:true,ws:true,
      configure(proxy){proxy.on('error',nativeWebUnavailable)}},
    [NATIVE_API]: {target:env.VITE_V2_NATIVE_API_TARGET||'http://127.0.0.1:8080',changeOrigin:true,
      rewrite:path=>path.slice(NATIVE_API.length),
      configure(proxy) {
        proxy.on('proxyReq',(out,req)=>{
          out.removeHeader('authorization')
          out.removeHeader('x-platform-client')
          out.setHeader('cookie',nativeRequestCookies(req.headers.cookie,sessionName))
        })
        proxy.on('proxyRes',response=>{
          const cookies=response.headers['set-cookie']
          if(cookies)response.headers['set-cookie']=cookies.map(v=>nativeResponseCookie(v,sessionName)).filter(Boolean)
        })
      }},
  }
}
export function nativeProxyGuard() {
  return {name:'native-collection-api-guard',configureServer(server){
    server.middlewares.use((req,res,next)=>{
      if(!req.url?.startsWith(NATIVE_API+'/'))return next()
      const path=new URL(req.url,'http://localhost').pathname.slice(NATIVE_API.length)
      const protectedProfile=path.startsWith('/api/v1/settlement-profiles/me')||path==='/api/v1/native-settlement/context'
      if(!allowedNativeRequest(req.method,path)||(protectedProfile&&!/^n1\.[A-Za-z0-9_-]{70}$/.test(req.headers['x-v2-native-settlement-intent']||''))){
        res.statusCode=403;res.setHeader('Content-Type','application/problem+json');res.end(JSON.stringify({title:'PERMISSION_DENIED',code:'PERMISSION_DENIED',status:403,detail:'请从原平台新版收款入口重新进入'}));return
      }
      next()
    })
  }}
}
