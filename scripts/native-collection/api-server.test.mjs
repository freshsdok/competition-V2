import { test } from 'node:test'
import assert from 'node:assert/strict'
import http from 'node:http'
import { createNativeApiServer } from './api-server.mjs'
test('recipient directory GET reaches the exact upstream with isolated cookies and unchanged intent',async()=>{
  const intent='n1.'+'a'.repeat(70),path='/api/v1/settlement-profiles/me/recipient-tenants'
  let calls=0
  const upstream=http.createServer((req,res)=>{
    calls++
    assert.equal(req.method,'GET');assert.equal(req.url,path)
    assert.equal(req.headers.cookie,'DESHI_SESSION=synthetic-native; XSRF-TOKEN=synthetic-csrf')
    assert.equal(req.headers['x-v2-native-settlement-intent'],intent)
    assert.equal(req.headers.authorization,undefined)
    assert.equal(req.headers['x-platform-client'],undefined)
    res.setHeader('Content-Type','application/json')
    res.end(JSON.stringify([{tenantId:17,tenantCode:'SYNTHETIC',tenantName:'合成接收方'}]))
  })
  await new Promise(resolve=>upstream.listen(0,'127.0.0.1',resolve))
  const proxy=createNativeApiServer(`http://127.0.0.1:${upstream.address().port}`)
  await new Promise(resolve=>proxy.listen(0,'127.0.0.1',resolve))
  try {
    const url=`http://127.0.0.1:${proxy.address().port}/v2-native-api${path}`
    assert.equal((await fetch(url)).status,403)
    assert.equal((await fetch(url,{method:'POST',headers:{'x-v2-native-settlement-intent':intent}})).status,403)
    assert.equal(calls,0)
    const result=await fetch(url,{headers:{'x-v2-native-settlement-intent':intent,
      cookie:'DESHI_SESSION=old; Admin-Token=old; V2_NATIVE_SESSION=synthetic-native; V2_NATIVE_XSRF=synthetic-csrf',
      authorization:'Bearer synthetic-old','x-platform-client':'mp-weixin'}})
    assert.equal(result.status,200)
    assert.equal(result.headers.get('cache-control'),'no-store')
    assert.deepEqual(await result.json(),[{tenantId:17,tenantCode:'SYNTHETIC',tenantName:'合成接收方'}])
    assert.equal(calls,1)
  } finally {
    await Promise.all([new Promise(resolve=>proxy.close(resolve)),new Promise(resolve=>upstream.close(resolve))])
  }
})
for (const sessionName of ['DESHI_SESSION', 'DESHI_PROD_SESSION']) {
test(`HTTP proxy isolates ${sessionName} cookies and only streams allowed guarded requests`,async()=>{
  let calls=0
  const upstream=http.createServer((req,res)=>{
    calls++;assert.equal(req.headers.cookie,`${sessionName}=native; XSRF-TOKEN=nativecsrf`)
    assert.equal(req.headers.origin,'https://www.ksup.cn')
    assert.equal(req.headers.authorization,undefined);assert.equal(req.headers['x-platform-client'],undefined)
    assert.equal(req.url,'/api/v1/settlement-profiles/me/versions')
    res.setHeader('set-cookie',[`${sessionName}=renewed; Path=/; HttpOnly`,'XSRF-TOKEN=renewedcsrf; Path=/'])
    req.pipe(res)
  })
  await new Promise(resolve=>upstream.listen(0,'127.0.0.1',resolve))
  const proxy=createNativeApiServer(`http://127.0.0.1:${upstream.address().port}`,sessionName)
  await new Promise(resolve=>proxy.listen(0,'127.0.0.1',resolve))
  try{
    const url=`http://127.0.0.1:${proxy.address().port}/v2-native-api/api/v1/settlement-profiles/me/versions`
    assert.equal((await fetch(url,{method:'POST'})).status,403);assert.equal(calls,0)
    const response=await fetch(url,{method:'POST',headers:{origin:'https://www.ksup.cn',cookie:`${sessionName}=old; XSRF-TOKEN=oldcsrf; V2_NATIVE_SESSION=native; V2_NATIVE_XSRF=nativecsrf`,authorization:'Bearer old',
      'x-platform-client':'mp-weixin','x-v2-native-settlement-intent':'n1.'+'a'.repeat(70)},body:'synthetic-body'})
    assert.equal(response.status,200);assert.equal(await response.text(),'synthetic-body');assert.equal(calls,1)
    assert.deepEqual(response.headers.getSetCookie(),['V2_NATIVE_SESSION=renewed; HttpOnly; Path=/','V2_NATIVE_XSRF=renewedcsrf; Path=/'])
  }finally{await Promise.all([new Promise(resolve=>proxy.close(resolve)),new Promise(resolve=>upstream.close(resolve))])}
})
}
