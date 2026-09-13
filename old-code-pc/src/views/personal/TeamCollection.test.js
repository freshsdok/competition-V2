import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import TeamCollection from './TeamCollection.vue'
import * as api from '@/api/teamCollection'
import { enterEmbeddedSettlement } from '@/api/internalEmbeddedBridge'
import { ElMessageBox } from 'element-plus'

vi.mock('@/api/teamCollection', () => ({ getMyTeamCollections: vi.fn(), startTeamCollection: vi.fn(), confirmTeamCollection: vi.fn(), releaseTeamCollection: vi.fn() }))
vi.mock('@/api/internalEmbeddedBridge', () => ({ enterEmbeddedSettlement: vi.fn() }))
vi.mock('element-plus', () => ({ ElMessageBox: { confirm: vi.fn() } }))

let wrapper, state
const available = () => ({ id: '1', teamCode: 'TEAM_A', teamName: '测试队伍', awardLevel: '一等奖', status: 'AVAILABLE', version: 0, heldByMe: false, canStart: true })
const held = () => ({ ...available(), status: 'IN_PROGRESS', version: 1, heldByMe: true, canStart: false, handlerName: '测试学生', startedAt: '2026-09-13T00:00:00Z' })
function button(text) { return wrapper.findAll('button').find(b => b.text() === text) }
async function render() { wrapper = mount(TeamCollection); await flushPromises() }

beforeEach(() => {
  vi.resetAllMocks()
  state = [available()]
  api.getMyTeamCollections.mockImplementation(async () => ({ data: structuredClone(state) }))
  enterEmbeddedSettlement.mockResolvedValue({ data: { entryPath: '/v2-embedded/embedded/settlement-profile#bootstrap=' + 'a'.repeat(43) } })
  ElMessageBox.confirm.mockResolvedValue('confirm')
})
afterEach(() => { wrapper?.unmount(); wrapper = null })

describe('V1队伍办理及人工确认', () => {
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

  it('confirmation closes iframe and posts the held version only after explicit confirmation', async () => {
    state = [held()]
    api.confirmTeamCollection.mockImplementation(async () => { state = [{ ...held(), status: 'USER_CONFIRMED_COMPLETE', heldByMe: false, version: 2, confirmedAt: '2026-09-13T01:00:00Z' }]; return { data: state[0] } })
    await render()
    await button('继续填报').trigger('click'); await flushPromises()
    expect(wrapper.find('iframe').exists()).toBe(true)
    await button('我已完成填报').trigger('click'); await flushPromises()
    expect(ElMessageBox.confirm).toHaveBeenCalled()
    expect(api.confirmTeamCollection).toHaveBeenCalledWith('1', 1)
    expect(wrapper.find('iframe').exists()).toBe(false)
    expect(wrapper.text()).toContain('此状态由办理人确认')
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
    api.getMyTeamCollections.mockRejectedValue(new Error('offline'))
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
    resolveEntry({ data: { entryPath: '/v2-embedded/embedded/settlement-profile#bootstrap=' + 'b'.repeat(43) } })
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
