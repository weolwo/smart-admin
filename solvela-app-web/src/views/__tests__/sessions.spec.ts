import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import type { MemberSession } from '@/api/session'

import SessionsView from '../SessionsView.vue'

/**
 * 我的登录设备。
 *
 * <h3>这一页最容易做错的一条：把「本机」也给一个下线按钮</h3>
 * 用户点下去会<b>当场把自己踢出这个页面</b>，而他多半只是想踢别人 ——
 * 然后他会以为页面出了 bug。所以当前这一条排最前、带「本机」标签、
 * <b>没有下线按钮</b>。
 *
 * <h3>其余两条</h3>
 * <ul>
 *   <li><b>只有一台设备时不出现「下线其它设备」</b> —— 点下去什么都不会发生的按钮，
 *       会让人以为功能坏了；</li>
 *   <li><b>信息缺失要如实说</b>：老会话没有设备端和登录时间，
 *       显示成留白或 1970 年都会让用户以为数据错乱。</li>
 * </ul>
 */

const fetchSessions = vi.hoisted(() => vi.fn())
const revokeSession = vi.hoisted(() => vi.fn(() => Promise.resolve()))
const revokeOtherSessions = vi.hoisted(() => vi.fn(() => Promise.resolve()))

vi.mock('@/api/session', () => ({ fetchSessions, revokeSession, revokeOtherSessions }))

function session(over: Partial<MemberSession>): MemberSession {
  return {
    sessionId: 's1',
    deviceType: 'APP',
    deviceId: null,
    ip: '203.0.113.5',
    region: null,
    loginTime: Date.parse('2026-09-10T10:30:00'),
    current: false,
    ...over,
  }
}

async function mountPage() {
  const w = mount(SessionsView)
  await flushPromises()
  return w
}

type Wrapper = Awaited<ReturnType<typeof mountPage>>

function revokeButtons(w: Wrapper) {
  return w.findAll('button').filter((b) => b.text() === '下线')
}

beforeEach(() => {
  fetchSessions.mockReset()
  revokeSession.mockReset()
  revokeSession.mockResolvedValue(undefined)
  revokeOtherSessions.mockReset()
  revokeOtherSessions.mockResolvedValue(undefined)
})

describe('列表', () => {
  it('🔴 本机那一条【没有】下线按钮 —— 点下去会把自己踢出这个页面', async () => {
    fetchSessions.mockResolvedValue([
      session({ sessionId: 'mine', current: true }),
      session({ sessionId: 'other', deviceType: 'PC', current: false }),
    ])

    const w = await mountPage()

    expect(w.text()).toContain('本机')
    expect(revokeButtons(w), '两条会话，只有【不是本机】的那一条能下线').toHaveLength(1)
  })

  it('显示设备端、IP 和登录时间 —— 这三样是用户判断「这是不是我」的全部依据', async () => {
    fetchSessions.mockResolvedValue([session({ deviceType: 'PC', ip: '198.51.100.9' })])

    const w = await mountPage()

    expect(w.text()).toContain('电脑')
    expect(w.text()).toContain('198.51.100.9')
    expect(w.text()).toContain('2026-09-10')
  })

  it('🔴 老会话信息不全 → 如实说「未知」，不是留白，也不是 1970 年', async () => {
    fetchSessions.mockResolvedValue([
      session({ deviceType: null, ip: null, loginTime: 0, current: true }),
    ])

    const w = await mountPage()

    expect(w.text()).toContain('未知设备')
    expect(w.text()).toContain('位置未知')
    expect(w.text()).toContain('登录时间未知')
    expect(w.text(), 'loginTime=0 直接格式化就是 1970，那会让用户以为数据错乱').not.toContain(
      '1970',
    )
  })

  it('认不出的设备端原样显示，不吞掉', async () => {
    // 后端加了新的 deviceType 而前端还没跟上时，显示它本身比显示「未知」诚实
    fetchSessions.mockResolvedValue([session({ deviceType: 'TV' as MemberSession['deviceType'] })])

    const w = await mountPage()

    expect(w.text()).toContain('TV')
  })
})

describe('下线', () => {
  it('点下线传的是那一条的 sessionId，然后重新拉列表', async () => {
    fetchSessions.mockResolvedValue([
      session({ sessionId: 'mine', current: true }),
      session({ sessionId: 'other-1', current: false }),
    ])
    const w = await mountPage()

    await revokeButtons(w)[0]?.trigger('click')
    await flushPromises()

    expect(revokeSession).toHaveBeenCalledWith('other-1')
    // 重新拉而不是本地删一行：期间可能有别的设备登进来
    expect(fetchSessions).toHaveBeenCalledTimes(2)
  })

  it('🔴 只有一台设备时，不出现「下线其它所有设备」', async () => {
    fetchSessions.mockResolvedValue([session({ current: true })])

    const w = await mountPage()

    expect(
      w.findAll('button').some((b) => b.text().includes('下线其它')),
      '点下去什么都不会发生的按钮，会让人以为功能坏了',
    ).toBe(false)
  })

  it('🔴 「下线其它设备」要二次确认，不是点一下就执行', async () => {
    fetchSessions.mockResolvedValue([
      session({ sessionId: 'mine', current: true }),
      session({ sessionId: 'a', current: false }),
    ])
    const w = await mountPage()

    await w
      .findAll('button')
      .find((b) => b.text().includes('下线其它'))
      ?.trigger('click')
    await flushPromises()

    expect(revokeOtherSessions, '第一下只该是展开确认').not.toHaveBeenCalled()
    expect(w.text()).toContain('除本机外的 1 台设备都会被登出')

    await w
      .findAll('button')
      .find((b) => b.text() === '确认下线')
      ?.trigger('click')
    await flushPromises()

    expect(revokeOtherSessions).toHaveBeenCalledTimes(1)
  })
})
