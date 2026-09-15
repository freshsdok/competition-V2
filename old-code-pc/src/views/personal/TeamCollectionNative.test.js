import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import TeamCollection from './TeamCollectionNative.vue'
import * as api from '@/api/teamCollection'
import { enterNativeCollection as enterEmbeddedSettlement, getMyNativeCollections } from '@/api/teamCollectionNative'
import { ElMessageBox } from 'element-plus'

vi.mock('@/api/teamCollection', () => ({ getMyTeamCollections: vi.fn(), startTeamCollection: vi.fn(), confirmTeamCollection: vi.fn(), releaseTeamCollection: vi.fn() }))
vi.mock('@/api/teamCollectionNative', () => ({ enterNativeCollection: vi.fn(), getMyNativeCollections: vi.fn() }))
vi.mock('element-plus', () => ({ ElMessageBox: { confirm: vi.fn() } }))

let wrapper, state, fetcher, childFrameNavigation, originalScrollPadding, originalScrollPriority
const available = () => ({ id: '1', teamCode: 'TEAM_A', teamName: '测试队伍', awardLevel: '一等奖', status: 'AVAILABLE', version: 0, heldByMe: false, canStart: true })
const held = () => ({ ...available(), status: 'IN_PROGRESS', version: 1, heldByMe: true, canStart: false, handlerName: '测试学生', startedAt: '2026-09-13T00:00:00Z' })
function button(text) { return wrapper.findAll('button').find(b => b.text() === text) }
async function render(attachTo = document.body) { wrapper = mount(TeamCollection, { attachTo }); await flushPromises() }
function hostLayout(height) {
  const host = document.createElement('div')
  host.className = 'app-glb-container'
  const header = document.createElement('header'), content = document.createElement('div')
  host.append(header, content)
  document.body.append(host)
  const measurement = { height }
  vi.spyOn(header, 'getBoundingClientRect').mockImplementation(() => ({ height: measurement.height }))
  return { host, header, content, measurement }
}
function settlementResponse(access = 'PROVIDED') {
  return { ok: true, json: vi.fn().mockResolvedValue({ version: 2, profile: { maskedName: '测*' },
    tenantAccess: [{ tenantId: 7, status: access, version: 0 }] }) }
}
function deferred() {
  let resolve
  const promise = new Promise(done => { resolve = done })
  return { promise, resolve }
}
async function openFrame() {
  await button('继续填报').trigger('click'); await flushPromises()
  const iframe = wrapper.get('iframe')
  const source = iframe.element.contentWindow
  const postMessage = vi.spyOn(source, 'postMessage').mockImplementation(() => {})
  await iframe.trigger('load'); await flushPromises()
  const connection = postMessage.mock.calls.at(-1)
  expect(connection[0]).toEqual({ type: 'deshi:native-settlement:connect', protocolVersion: 1, channel: expect.stringMatching(/^[a-f0-9]{32}$/) })
  expect(connection[1]).toBe(window.location.origin)
  return { iframe, source, postMessage, nonce: connection[0].channel }
}
async function sendProgress(context, fields = {}, options = {}) {
  window.dispatchEvent(new MessageEvent('message', {
    origin: window.location.origin, source: context.source,
    data: { type: 'deshi:native-settlement:progress', protocolVersion: 1, channel: context.nonce,
      phase: 'provided', profileVersion: 2, tenantId: 7, accessVersion: 0, ...fields },
    ...options,
  }))
  await flushPromises()
}

beforeEach(() => {
  vi.resetAllMocks()
  const scrollingRoot = document.scrollingElement || document.documentElement
  originalScrollPadding = scrollingRoot.style.getPropertyValue('scroll-padding-top')
  originalScrollPriority = scrollingRoot.style.getPropertyPriority('scroll-padding-top')
  // Keep a real iframe Window for source validation while preventing test-network navigation.
  childFrameNavigation = window.happyDOM.settings.navigation.disableChildFrameNavigation
  window.happyDOM.settings.navigation.disableChildFrameNavigation = true
  state = [available()]
  getMyNativeCollections.mockImplementation(async () => ({ data: structuredClone(state) }))
  enterEmbeddedSettlement.mockResolvedValue({ data: { entryPath: '/v2-native/native-settlement#intent=n1.' + 'a'.repeat(70) } })
  ElMessageBox.confirm.mockResolvedValue('confirm')
  fetcher = vi.fn().mockImplementation(async () => settlementResponse())
  vi.stubGlobal('fetch', fetcher)
})
afterEach(() => {
  wrapper?.unmount(); wrapper = null
  const scrollingRoot = document.scrollingElement || document.documentElement
  if (originalScrollPadding) scrollingRoot.style.setProperty('scroll-padding-top', originalScrollPadding, originalScrollPriority)
  else scrollingRoot.style.removeProperty('scroll-padding-top')
  vi.restoreAllMocks()
  vi.unstubAllGlobals()
  window.happyDOM.settings.navigation.disableChildFrameNavigation = childFrameNavigation
  document.body.innerHTML = ''
})

describe('V1队伍办理及人工确认', () => {
  it('adapts both owning application shells without changing unrelated layouts and removes the adaptation on leaving', async () => {
    const { host, content } = hostLayout(70)
    const outer = document.createElement('div'), layout = document.createElement('div'), unrelated = document.createElement('div')
    outer.className = layout.className = unrelated.className = 'app'
    layout.style.minWidth = '1080px'
    layout.append(host, document.createElement('footer'))
    outer.append(layout)
    document.body.append(outer, unrelated)
    await render(content)
    for (const element of [outer, layout, host]) expect(element.classList.contains('native-collection-shell')).toBe(true)
    expect(unrelated.className).toBe('app')
    expect(layout.style.minWidth).toBe('1080px')
    wrapper.unmount(); wrapper = null
    expect(outer.querySelector('.native-collection-shell')).toBeNull()
    expect(outer.className).toBe('app')
    expect(layout.className).toBe('app')
    expect(host.className).toBe('app-glb-container')
    expect(layout.style.minWidth).toBe('1080px')
    expect(unrelated.className).toBe('app')
  })

  it('preserves a shell adaptation owned by another mount when this route leaves', async () => {
    const { host, content } = hostLayout(70)
    const outer = document.createElement('div')
    outer.className = 'app native-collection-shell'
    outer.append(host)
    document.body.append(outer)
    await render(content)
    expect(host.classList.contains('native-collection-shell')).toBe(true)
    wrapper.unmount(); wrapper = null
    expect(outer.className).toBe('app native-collection-shell')
    expect(host.className).toBe('app-glb-container')
  })

  it('keeps scroll targets below the measured host navigation and restores the previous offset on leaving', async () => {
    const host = hostLayout(70.5)
    const scrollingRoot = document.scrollingElement || document.documentElement
    scrollingRoot.style.setProperty('scroll-padding-top', '7px', 'important')
    let resized, observer
    vi.stubGlobal('ResizeObserver', class {
      constructor(callback) { resized = callback; observer = this }
      observe = vi.fn()
      disconnect = vi.fn()
    })
    await render(host.content)
    expect(observer.observe).toHaveBeenCalledWith(host.header)
    expect(scrollingRoot.style.scrollPaddingTop).toBe('83px')
    expect(wrapper.element.style.getPropertyValue('--collection-top-inset')).toBe('83px')
    host.measurement.height = 110.25
    resized(); await flushPromises()
    expect(scrollingRoot.style.scrollPaddingTop).toBe('123px')
    expect(wrapper.element.style.getPropertyValue('--collection-top-inset')).toBe('123px')
    wrapper.unmount(); wrapper = null
    expect(observer.disconnect).toHaveBeenCalledOnce()
    expect(scrollingRoot.style.scrollPaddingTop).toBe('7px')
    expect(scrollingRoot.style.getPropertyPriority('scroll-padding-top')).toBe('important')
    resized()
    expect(scrollingRoot.style.scrollPaddingTop).toBe('7px')
  })

  it('does not replace the document scroll offset when there is no host navigation', async () => {
    const scrollingRoot = document.scrollingElement || document.documentElement
    scrollingRoot.style.scrollPaddingTop = '17px'
    await render()
    expect(scrollingRoot.style.scrollPaddingTop).toBe('17px')
    expect(wrapper.element.style.getPropertyValue('--collection-top-inset')).toBe('')
  })

  it('updates the inset on viewport resize and preserves a later owner\'s scroll offset at cleanup', async () => {
    const host = hostLayout(60)
    const scrollingRoot = document.scrollingElement || document.documentElement
    vi.stubGlobal('ResizeObserver', undefined)
    await render(host.content)
    expect(scrollingRoot.style.scrollPaddingTop).toBe('72px')
    host.measurement.height = 90
    window.dispatchEvent(new Event('resize')); await flushPromises()
    expect(scrollingRoot.style.scrollPaddingTop).toBe('102px')
    scrollingRoot.style.scrollPaddingTop = '140px'
    wrapper.unmount(); wrapper = null
    expect(scrollingRoot.style.scrollPaddingTop).toBe('140px')
  })

  it('does not request a V2 entry on mount; acquiring precedes iframe issuance', async () => {
    api.startTeamCollection.mockImplementation(async () => { state = [held()]; return { data: state[0] } })
    await render()
    expect(enterEmbeddedSettlement).not.toHaveBeenCalled()
    await button('申请办理并进入填报').trigger('click'); await flushPromises()
    expect(api.startTeamCollection).toHaveBeenCalledWith('1', 0)
    expect(enterEmbeddedSettlement).toHaveBeenCalledWith('1', 1)
    expect(wrapper.find('iframe').exists()).toBe(true)
  })

  it('peers see handler/time but cannot open or finish; completion is explicitly manual', async () => {
    state = [{ ...held(), heldByMe: false }]
    await render()
    expect(wrapper.text()).toContain('测试学生')
    expect(wrapper.text()).toContain('08:00:00')
    expect(button('继续填报')).toBeUndefined()
    expect(button('我已完成填报')).toBeUndefined()
    expect(enterEmbeddedSettlement).not.toHaveBeenCalled()
    state = [{ ...state[0], status: 'USER_CONFIRMED_COMPLETE', confirmedAt: '2026-09-13T01:00:00Z', version: 2 }]
    await button('刷新状态').trigger('click'); await flushPromises()
    expect(wrapper.text()).toContain('此状态由办理人确认')
    expect(wrapper.text()).toContain('09:00:00')
  })

  it('requires server-verified provision, then confirms once without another confirmation dialog', async () => {
    state = [held()]
    api.confirmTeamCollection.mockImplementation(async () => { state = [{ ...held(), status: 'USER_CONFIRMED_COMPLETE', heldByMe: false, version: 2, confirmedAt: '2026-09-13T01:00:00Z' }]; return { data: state[0] } })
    await render()
    expect(button('我已完成填报').attributes('disabled')).toBeDefined()
    const context = await openFrame()
    expect(wrapper.find('iframe').exists()).toBe(true)
    expect(button('我已完成填报').attributes('disabled')).toBeDefined()
    await button('我已完成填报').trigger('click'); await flushPromises()
    expect(api.confirmTeamCollection).not.toHaveBeenCalled()
    await sendProgress(context)
    expect(fetcher).toHaveBeenCalledWith('/v2-native-api/api/v1/settlement-profiles/me', expect.objectContaining({
      method: 'GET', credentials: 'include', cache: 'no-store',
      headers: expect.objectContaining({ 'X-V2-Native-Settlement-Intent': 'n1.' + 'a'.repeat(70) }),
    }))
    expect(button('我已完成填报').attributes('disabled')).toBeUndefined()
    expect(api.confirmTeamCollection).not.toHaveBeenCalled()
    await button('我已完成填报').trigger('click'); await flushPromises()
    expect(ElMessageBox.confirm).not.toHaveBeenCalled()
    expect(fetcher).toHaveBeenCalledTimes(2)
    expect(api.confirmTeamCollection).toHaveBeenCalledOnce()
    expect(api.confirmTeamCollection).toHaveBeenCalledWith('1', 1)
    expect(wrapper.find('iframe').exists()).toBe(false)
    expect(wrapper.text()).toContain('此状态由办理人确认')
  })

  it('keeps an already verified final action available when regaining focus starts background verification', async () => {
    state = [held()]; await render()
    const context = await openFrame()
    await sendProgress(context)
    api.confirmTeamCollection.mockImplementationOnce(async () => {
      state = [{ ...held(), status: 'USER_CONFIRMED_COMPLETE', heldByMe: false, version: 2 }]
      return { data: state[0] }
    })
    const background = deferred()
    fetcher.mockReturnValueOnce(background.promise)
    window.dispatchEvent(new Event('focus')); await flushPromises()
    // A real pointer click can focus the window before its click event is delivered.
    const enabledDuringFocus = button('我已完成填报').attributes('disabled') === undefined
    await button('我已完成填报').trigger('click'); await flushPromises()
    const confirmationsFromClick = api.confirmTeamCollection.mock.calls.length
    background.resolve(settlementResponse()); await flushPromises()
    expect(enabledDuringFocus).toBe(true)
    expect(confirmationsFromClick).toBe(1)
    expect(fetcher).toHaveBeenCalledTimes(3)
    expect(wrapper.find('iframe').exists()).toBe(false)
    expect(wrapper.text()).toContain('本队已完成填报')
  })

  it.each(['REVOKED', 'HTTP_ERROR'])('disables the final action when a background recheck returns %s', async (result) => {
    state = [held()]; await render()
    const context = await openFrame()
    await sendProgress(context)
    fetcher.mockResolvedValueOnce(result === 'HTTP_ERROR' ? { ok: false } : settlementResponse(result))
    window.dispatchEvent(new Event('focus')); await flushPromises()
    expect(button('我已完成填报').attributes('disabled')).toBeDefined()
    expect(wrapper.find('#collection-guide-title').exists()).toBe(false)
    expect(wrapper.find('iframe').exists()).toBe(true)
    expect(wrapper.text()).toContain('暂未核实资料提供结果')
    await button('我已完成填报').trigger('click'); await flushPromises()
    expect(api.confirmTeamCollection).not.toHaveBeenCalled()
  })

  it.each([
    ['handler', { heldByMe: false }],
    ['held version', { version: 2 }],
  ])('closes the iframe when a background recheck detects a changed %s', async (_label, changed) => {
    state = [held()]; await render()
    const context = await openFrame()
    await sendProgress(context)
    state = [{ ...held(), ...changed }]
    window.dispatchEvent(new Event('focus')); await flushPromises()
    expect(wrapper.find('iframe').exists()).toBe(false)
    expect(wrapper.find('#collection-guide-title').exists()).toBe(false)
    expect(wrapper.text()).toContain('本队办理状态已变化')
    expect(api.confirmTeamCollection).not.toHaveBeenCalled()
  })

  it('requires a fresh successful final check even while an older background result is pending', async () => {
    state = [held()]; await render()
    const context = await openFrame()
    await sendProgress(context)
    const background = deferred()
    fetcher.mockReturnValueOnce(background.promise)
    window.dispatchEvent(new Event('focus')); await flushPromises()
    fetcher.mockResolvedValueOnce(settlementResponse('REVOKED'))
    await button('我已完成填报').trigger('click'); await flushPromises()
    expect(fetcher).toHaveBeenCalledTimes(3)
    expect(api.confirmTeamCollection).not.toHaveBeenCalled()
    expect(button('我已完成填报').attributes('disabled')).toBeDefined()
    background.resolve(settlementResponse()); await flushPromises()
    expect(api.confirmTeamCollection).not.toHaveBeenCalled()
    expect(button('我已完成填报').attributes('disabled')).toBeDefined()
    expect(wrapper.find('iframe').exists()).toBe(true)
    expect(wrapper.find('#collection-guide-title').exists()).toBe(false)
    expect(wrapper.text()).toContain('暂未核实资料提供结果')
  })

  it('ignores progress sent by another origin, another window, or a different nonce', async () => {
    state = [held()]; await render()
    const context = await openFrame()
    await sendProgress(context, {}, { origin: 'https://other.example' })
    await sendProgress(context, {}, { source: window })
    await sendProgress(context, { channel: '0'.repeat(32) })
    expect(fetcher).not.toHaveBeenCalled()
    expect(button('我已完成填报').attributes('disabled')).toBeDefined()
    expect(wrapper.find('#collection-guide-title').exists()).toBe(false)
  })

  it('does not restore completion readiness when an old verification finishes after editing resumes', async () => {
    state = [held()]; await render()
    const context = await openFrame()
    const pending = deferred()
    fetcher.mockReturnValueOnce(pending.promise)
    await sendProgress(context)
    expect(fetcher).toHaveBeenCalledOnce()
    expect(button('我已完成填报').attributes('disabled')).toBeDefined()
    await sendProgress(context, { phase: 'editing' })
    pending.resolve(settlementResponse())
    await flushPromises()
    expect(button('我已完成填报').attributes('disabled')).toBeDefined()
    expect(wrapper.find('#collection-guide-title').exists()).toBe(false)
    expect(api.confirmTeamCollection).not.toHaveBeenCalled()
  })

  it('rechecks recipient access on the final click and blocks a revoked grant', async () => {
    state = [held()]; await render()
    const context = await openFrame()
    await sendProgress(context)
    fetcher.mockResolvedValueOnce(settlementResponse('REVOKED'))
    await button('我已完成填报').trigger('click'); await flushPromises()
    expect(fetcher).toHaveBeenCalledTimes(2)
    expect(api.confirmTeamCollection).not.toHaveBeenCalled()
    expect(wrapper.find('iframe').exists()).toBe(true)
    expect(button('我已完成填报').attributes('disabled')).toBeDefined()
    expect(wrapper.text()).toContain('暂未核实资料提供结果')
    expect(wrapper.text()).not.toContain('本队已完成填报')
  })

  it.each([
    ['handler', { heldByMe: false }],
    ['held version', { version: 2 }],
  ])('rejects final confirmation after the %s changes', async (_label, changed) => {
    state = [held()]; await render()
    const context = await openFrame()
    await sendProgress(context)
    state = [{ ...held(), ...changed }]
    await button('我已完成填报').trigger('click'); await flushPromises()
    expect(api.confirmTeamCollection).not.toHaveBeenCalled()
    expect(wrapper.find('iframe').exists()).toBe(false)
    expect(wrapper.text()).toContain('本队办理状态已变化')
  })

  it('rotates the channel on iframe load and rejects messages from the previous document', async () => {
    state = [held()]; await render()
    const context = await openFrame()
    await context.iframe.trigger('load'); await flushPromises()
    const newNonce = context.postMessage.mock.calls.at(-1)[0].channel
    expect(newNonce).not.toBe(context.nonce)
    await sendProgress(context)
    expect(fetcher).not.toHaveBeenCalled()
    expect(button('我已完成填报').attributes('disabled')).toBeDefined()
    await sendProgress({ ...context, nonce: newNonce })
    expect(fetcher).toHaveBeenCalledOnce()
    expect(button('我已完成填报').attributes('disabled')).toBeUndefined()
  })

  it('dismissing the guide retains the provided state without confirming the team', async () => {
    state = [held()]; await render()
    const context = await openFrame()
    await sendProgress(context)
    expect(wrapper.find('#collection-guide-title').exists()).toBe(true)
    await wrapper.get('#collection-guide-dismiss').trigger('click'); await flushPromises()
    expect(wrapper.find('#collection-guide-title').exists()).toBe(false)
    expect(wrapper.find('iframe').exists()).toBe(true)
    expect(button('我已完成填报').attributes('disabled')).toBeUndefined()
    expect(api.confirmTeamCollection).not.toHaveBeenCalled()
    expect(wrapper.text()).not.toContain('本队已完成填报')
  })

  it('retains the iframe after a failed confirmation and recovers by verifying the current state again', async () => {
    state = [held()]; await render()
    const context = await openFrame()
    await sendProgress(context)
    api.confirmTeamCollection.mockRejectedValueOnce(new Error('offline'))
    await button('我已完成填报').trigger('click'); await flushPromises()
    expect(api.confirmTeamCollection).toHaveBeenCalledOnce()
    expect(wrapper.find('iframe').exists()).toBe(true)
    expect(button('我已完成填报').attributes('disabled')).toBeDefined()
    expect(wrapper.text()).toContain('操作未完成')
    expect(wrapper.text()).not.toContain('本队已完成填报')
    await button('刷新状态').trigger('click'); await flushPromises()
    expect(fetcher).toHaveBeenCalledTimes(3)
    expect(button('我已完成填报').attributes('disabled')).toBeUndefined()
    expect(api.confirmTeamCollection).toHaveBeenCalledOnce()
  })

  it('shows the completed server state when confirmation succeeded but its response was lost', async () => {
    state = [held()]; await render()
    const context = await openFrame()
    await sendProgress(context)
    api.confirmTeamCollection.mockImplementationOnce(async () => {
      state = [{ ...held(), status: 'USER_CONFIRMED_COMPLETE', heldByMe: false, version: 2,
        confirmedAt: '2026-09-13T01:00:00Z' }]
      throw new Error('response lost')
    })
    await button('我已完成填报').trigger('click'); await flushPromises()
    expect(api.confirmTeamCollection).toHaveBeenCalledOnce()
    expect(wrapper.find('iframe').exists()).toBe(false)
    expect(wrapper.text()).toContain('本队已完成填报')
    expect(wrapper.text()).toContain('最新队伍状态：本队已确认完成。')
    expect(wrapper.text()).not.toContain('操作未完成')
  })

  it('cancelled dialog and leaving the page do not release a team', async () => {
    state = [held()]; ElMessageBox.confirm.mockRejectedValue('cancel')
    await render()
    await button('放弃本次办理，释放名额').trigger('click'); await flushPromises()
    wrapper.unmount(); wrapper = null
    expect(api.releaseTeamCollection).not.toHaveBeenCalled()
  })

  it('release closes frame and permits another round after server confirmation', async () => {
    state = [held()]
    api.releaseTeamCollection.mockImplementation(async () => { state = [{ ...available(), version: 2 }]; return { data: state[0] } })
    await render()
    await button('继续填报').trigger('click'); await flushPromises()
    await button('放弃本次办理，释放名额').trigger('click'); await flushPromises()
    expect(api.releaseTeamCollection).toHaveBeenCalledWith('1', 1)
    expect(wrapper.find('iframe').exists()).toBe(false)
    expect(button('申请办理并进入填报')).toBeDefined()
  })

  it('status failure closes frame instead of presenting an available team', async () => {
    state = [held()]; await render()
    await button('继续填报').trigger('click'); await flushPromises()
    getMyNativeCollections.mockRejectedValue(new Error('offline'))
    await button('刷新状态').trigger('click'); await flushPromises()
    expect(wrapper.find('iframe').exists()).toBe(false)
    expect(wrapper.text()).toContain('暂时无法核实办理状态')
    expect(button('我已完成填报').attributes('disabled')).toBeDefined()
  })

  it('empty eligibility never offers an entry', async () => {
    state = []; await render()
    expect(wrapper.text()).toContain('没有可查看')
    expect(wrapper.find('iframe').exists()).toBe(false)
    expect(enterEmbeddedSettlement).not.toHaveBeenCalled()
  })

  it('a delayed bridge response cannot reopen an old attempt after another tab releases it', async () => {
    state = [held()]
    let resolveEntry
    enterEmbeddedSettlement.mockImplementation(() => new Promise(resolve => { resolveEntry = resolve }))
    await render()
    await button('继续填报').trigger('click'); await flushPromises()
    state = [{ ...available(), version: 2 }]
    resolveEntry({ data: { entryPath: '/v2-native/native-settlement#intent=n1.' + 'b'.repeat(70) } })
    await flushPromises()
    expect(wrapper.find('iframe').exists()).toBe(false)
    expect(button('申请办理并进入填报')).toBeDefined()
  })

  it('a rejected acquisition never calls V2 and refreshes the winning member status', async () => {
    api.startTeamCollection.mockImplementation(async () => {
      state = [{ ...held(), heldByMe: false }]
      throw new Error('conflict')
    })
    await render()
    await button('申请办理并进入填报').trigger('click'); await flushPromises()
    expect(enterEmbeddedSettlement).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('本队正在办理')
  })
})
