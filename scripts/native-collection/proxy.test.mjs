import { test } from 'node:test'
import assert from 'node:assert/strict'
import { allowedNativeRequest, nativeRequestCookies, nativeResponseCookie, nativeProxyGuard, nativeWebUnavailable } from './proxy.mjs'
test('an unavailable iframe upstream displays a static recoverable error instead of an empty page',()=>{
  let status,headers,body
  const res={writeHead(code,values){status=code;headers=values},end(value){body=value}}
  nativeWebUnavailable(new Error('private upstream details'),{url:'/v2-native/native-settlement?private-value',headers:{}},res)
  assert.equal(status,503);assert.equal(headers['Cache-Control'],'no-store')
  assert.match(headers['Content-Type'],/text\/html/);assert.match(body,/继续填报/)
  assert.match(body,/名额仍然保留/);assert.doesNotMatch(body,/private/)
  let ended=false
  nativeWebUnavailable(null,{headers:{}},{headersSent:true,writeHead(){},end(){ended=true}})
  assert.equal(ended,false)
})
test('allows exact native login and original bank API contract only',()=>{
  for(const [method,path] of [['GET','auth/session'],['GET','auth/session/tenants'],['GET','legal/requirements'],['GET','me/account-security-center'],['GET','native-settlement/context'],['POST','auth/password-sessions'],['POST','auth/session/step-up/password'],['POST','settlement-profiles/me/versions'],['POST','settlement-profiles/me/tenant-access']]) assert.equal(allowedNativeRequest(method,'/api/v1/'+path),true)
  for(const [method,path] of [['GET','settlement-profiles/admin'],['POST','internal/v1-bridge/sessions'],['PUT','settlement-profiles/me'],['GET','auth/password-sessions'],['POST','auth/session/identity-context']]) assert.equal(allowedNativeRequest(method,'/api/v1/'+path),false)
})
test('isolates session and CSRF cookies in both directions including logout expiry',()=>{
  assert.equal(nativeRequestCookies('DESHI_SESSION=old; XSRF-TOKEN=oldcsrf; Admin-Token=v1; V2_NATIVE_SESSION=new; V2_NATIVE_XSRF=newcsrf'),'DESHI_SESSION=new; XSRF-TOKEN=newcsrf')
  assert.equal(nativeRequestCookies('DESHI_SESSION=old'),'')
  assert.equal(nativeResponseCookie('DESHI_SESSION=; Max-Age=0; Path=/; HttpOnly; SameSite=Lax'),'V2_NATIVE_SESSION=; Max-Age=0; HttpOnly; SameSite=Lax; Path=/')
  assert.equal(nativeResponseCookie('XSRF-TOKEN=c; Path=/api; Domain=upstream; Secure'),'V2_NATIVE_XSRF=c; Secure; Path=/')
  assert.equal(nativeResponseCookie('unknown=value'),null)
})
test('recipient directory permits only the exact GET and retains the native intent guard',()=>{
  const path='/api/v1/settlement-profiles/me/recipient-tenants'
  assert.equal(allowedNativeRequest('GET',path),true)
  for(const method of ['POST','PUT','PATCH','DELETE','HEAD','OPTIONS']) assert.equal(allowedNativeRequest(method,path),false)
  for(const suffix of ['/','/17','-admin']) assert.equal(allowedNativeRequest('GET',path+suffix),false)
  assert.equal(allowedNativeRequest('GET','/api/v1/settlement-profiles/admin/recipient-tenants'),false)
  let middleware; nativeProxyGuard().configureServer({middlewares:{use(fn){middleware=fn}}})
  function check(method, intent) {
    let forwarded=false
    const headers=intent===undefined?{}:{'x-v2-native-settlement-intent':intent}
    const res={setHeader(){},end(){}}
    middleware({url:'/v2-native-api'+path,method,headers},res,()=>forwarded=true)
    return {forwarded,status:res.statusCode}
  }
  for(const intent of [undefined,'invalid','n1.'+'a'.repeat(69)]) {
    assert.deepEqual(check('GET',intent),{forwarded:false,status:403})
  }
  assert.equal(check('GET','n1.'+'a'.repeat(70)).forwarded,true)
  assert.deepEqual(check('POST','n1.'+'a'.repeat(70)),{forwarded:false,status:403})
})
test('proxy fails closed for missing intents; old API prefix is untouched',()=>{
  let middleware; nativeProxyGuard().configureServer({middlewares:{use(fn){middleware=fn}}})
  function check(url,headers={}) { let next=false,ended=false;const res={setHeader(){},end(){ended=true}};middleware({url,method:'GET',headers},res,()=>next=true);return {next,ended,status:res.statusCode} }
  assert.equal(check('/v2-native-api/api/v1/settlement-profiles/me').status,403)
  assert.equal(check('/v2-native-api/api/v1/settlement-profiles/me',{'x-v2-native-settlement-intent':'n1.'+'a'.repeat(70)}).next,true)
  assert.equal(check('/api/v1/settlement-profiles/me').next,true)
  assert.equal(check('/v2-native-api/api/v1/settlement-profiles/admin').status,403)
})
