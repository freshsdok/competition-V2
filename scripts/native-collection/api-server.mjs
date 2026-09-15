// 可选静态部署适配器：仅监听回环地址，复用开发代理的白名单与Cookie隔离。
// 银行请求体只以流转发，不落库、不记录请求体/凭据。
import http from 'node:http'
import https from 'node:https'
import { pathToFileURL } from 'node:url'
import { NATIVE_API, nativeProxyGuard, nativeRequestCookies, nativeResponseCookie } from './proxy.mjs'
export function createNativeApiServer(target, sessionName='DESHI_SESSION') {
  const upstream=new URL(target)
  if(!['http:','https:'].includes(upstream.protocol)||upstream.username||upstream.password)throw new Error('Invalid native API target')
  let guard;nativeProxyGuard().configureServer({middlewares:{use(fn){guard=fn}}})
  return http.createServer((req,res)=>{
    if(!req.url?.startsWith(NATIVE_API+'/')){res.writeHead(404).end();return}
    guard(req,res,()=>{
      const headers={...req.headers,host:upstream.host,cookie:nativeRequestCookies(req.headers.cookie,sessionName)}
      delete headers.authorization;delete headers['x-platform-client']
      const outgoing=(upstream.protocol==='https:'?https:http).request({protocol:upstream.protocol,hostname:upstream.hostname,port:upstream.port,
        method:req.method,path:req.url.slice(NATIVE_API.length),headers,timeout:30000},incoming=>{
        const responseHeaders={...incoming.headers,'cache-control':'no-store'}
        if(responseHeaders['set-cookie'])responseHeaders['set-cookie']=responseHeaders['set-cookie'].map(cookie=>nativeResponseCookie(cookie,sessionName)).filter(Boolean)
        res.writeHead(incoming.statusCode??502,responseHeaders);incoming.pipe(res)
      })
      outgoing.on('timeout',()=>outgoing.destroy())
      outgoing.on('error',()=>{if(!res.headersSent)res.writeHead(502,{'Content-Type':'application/problem+json'});res.end(JSON.stringify({code:'INTERNAL_DEPENDENCY_UNAVAILABLE',title:'服务暂不可用',status:502}))})
      req.on('aborted',()=>outgoing.destroy());req.pipe(outgoing)
    })
  })
}
if(process.argv[1]&&import.meta.url===pathToFileURL(process.argv[1]).href){
  const port=Number(process.env.V2_NATIVE_PROXY_PORT||5180)
  createNativeApiServer(process.env.V2_NATIVE_API_TARGET||'http://127.0.0.1:8080',process.env.V2_NATIVE_SESSION_COOKIE_NAME||'DESHI_SESSION')
    .listen(port,'127.0.0.1',()=>process.stdout.write(`Native API proxy listening on 127.0.0.1:${port}\n`))
}
