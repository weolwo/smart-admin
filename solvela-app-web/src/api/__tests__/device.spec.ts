import type { AxiosRequestConfig } from 'axios'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import { ensureDevice, registerDevice } from '../device'
import { clearDevice, readDevice, writeDevice } from '@/utils/device-storage'

/**
 * 设备身份的三条规矩。它们坏掉的时候**不会有任何报错**。
 *
 * <h3>为什么值得一整个 spec</h3>
 * 设备号是整套防刷里最便宜、回报最高的一笔：有了它，「一台设备碰过哪些账号」
 * 才查得出来，而那正是将来判断「要不要花钱买厂商指纹」的唯一依据。
 *
 * <p>而它退化的方式全是静默的 —— 页面照常打开，请求照常成功，只是
 * `t_device` 里凭空多出几行、或者某一列恒为空。等到需要它的那天，
 * 数据已经错了几个月，补不回来。
 *
 * <p>三条：**领了要存住**、**存住了不重复领**、**并发只领一次**。
 */

const request = vi.hoisted(() => vi.fn())

vi.mock('../http', () => ({
  request,
  requestVoid: vi.fn(() => Promise.resolve()),
  DEVICE_REGISTER_URL: '/device/register',
}))

const REPLY = {
  deviceToken: 'dv_1.eyJ4IjoxfQ.c2ln',
  deviceId: '0123456789abcdef0123456789abcdef',
}

beforeEach(() => {
  clearDevice()
  request.mockReset()
  request.mockResolvedValue(REPLY)
})

afterEach(() => {
  clearDevice()
})

describe('领设备身份', () => {
  it('领到之后必须存住 —— 存不住就等于每次访问都是一台新设备', async () => {
    await registerDevice({ deviceType: 'H5' })

    expect(readDevice()).toEqual({ token: REPLY.deviceToken, deviceId: REPLY.deviceId })
  })

  it('🔴 H5 不上报 model —— 浏览器里那串 UA 不是型号', async () => {
    await registerDevice({ deviceType: 'H5' })

    const config = request.mock.calls[0]?.[0] as AxiosRequestConfig | undefined
    const body = (config?.data ?? {}) as Record<string, unknown>
    expect(body.deviceType).toBe('H5')
    expect(body.model).toBeUndefined()
  })
})

describe('ensureDevice', () => {
  it('已经有了就直接用，一个请求都不发 —— 领设备不是幂等的', async () => {
    writeDevice(REPLY.deviceToken, REPLY.deviceId)

    const device = await ensureDevice('H5')

    expect(device?.token).toBe(REPLY.deviceToken)
    expect(request).not.toHaveBeenCalled()
  })

  it('🔴 并发调用只领一台 —— 否则冷启动那一瞬间一个用户会多出好几行 t_device', async () => {
    const [a, b, c] = await Promise.all([ensureDevice(), ensureDevice(), ensureDevice()])

    expect(request).toHaveBeenCalledTimes(1)
    expect(a?.deviceId).toBe(REPLY.deviceId)
    expect(b?.deviceId).toBe(REPLY.deviceId)
    expect(c?.deviceId).toBe(REPLY.deviceId)
  })

  it('领不到时返回 null，不抛 —— 设备身份是增强，不是登录的前置条件', async () => {
    request.mockRejectedValue(new Error('断网了'))

    await expect(ensureDevice()).resolves.toBeNull()
  })

  it('这一次失败不该把后面的也卡死', async () => {
    request.mockRejectedValueOnce(new Error('断网了'))
    expect(await ensureDevice()).toBeNull()

    // 在途 promise 必须被清掉，否则第二次会拿到上一次那个已 reject 的结果
    expect(await ensureDevice()).not.toBeNull()
    expect(request).toHaveBeenCalledTimes(2)
  })
})

describe('存储语义', () => {
  it('🔴 设备令牌存在 localStorage，而不是 sessionStorage', () => {
    writeDevice(REPLY.deviceToken, REPLY.deviceId)

    // 一个标签页一关就没的设备身份，等于每次访问都是新设备 —— 那就没有防刷可言
    expect(localStorage.getItem('solvela.app.device.token')).toBe(REPLY.deviceToken)
    expect(sessionStorage.getItem('solvela.app.device.token')).toBeNull()
  })

  it('🔴 退出登录清的是令牌，设备身份必须原封不动', async () => {
    writeDevice(REPLY.deviceToken, REPLY.deviceId)
    const { clearToken, writeToken } = await import('@/utils/token-storage')
    writeToken('mb_xxx', 3600, true)

    clearToken()

    expect(readDevice()).not.toBeNull()
  })
})
