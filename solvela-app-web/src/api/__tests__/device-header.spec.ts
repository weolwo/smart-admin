import type { AxiosRequestConfig } from 'axios'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import http, { DEVICE_REGISTER_URL, configureHttp, request } from '../http'

/**
 * 请求头上的设备令牌。
 *
 * <h3>为什么要单独测这一层</h3>
 * `device.spec.ts` 验的是「领到了、存住了」，而这里验的是**它有没有真的被带上**。
 * 两者之间那一步坏掉时症状完全一样：一切正常，只是服务端那个
 * 「设备令牌覆盖率」永远上不去 —— 而它是决定「能不能从 observe 切到 enforce」
 * 的唯一依据。
 *
 * <h3>🔴 第二条用例防的是死锁，不是漏头</h3>
 * 领设备身份的那条请求自己会走同一个拦截器。如果它也去等设备令牌，
 * 等的就是它自己 —— 页面表现为**所有请求永久挂起**，连报错都没有。
 */

/** 把 adapter 换掉，请求不出网，同时拿到最终发出去的 config */
function captureAdapter(): { seen: AxiosRequestConfig[]; restore: () => void } {
  const seen: AxiosRequestConfig[] = []
  const original = http.defaults.adapter
  http.defaults.adapter = (config) => {
    seen.push(config)
    return Promise.resolve({ data: {}, status: 200, statusText: 'OK', headers: {}, config })
  }
  return {
    seen,
    restore: () => {
      // exactOptionalPropertyTypes 下 `= undefined` 不合法，要真的把这个键删掉
      if (original === undefined) {
        delete http.defaults.adapter
      } else {
        http.defaults.adapter = original
      }
    },
  }
}

function headerOf(config: AxiosRequestConfig | undefined, name: string): unknown {
  return (config?.headers as Record<string, unknown> | undefined)?.[name]
}

beforeEach(() => {
  configureHttp({
    getToken: () => 'mb_token',
    onLoginRequired: () => {},
    ensureDeviceToken: () => Promise.resolve('dv_1.payload.sig'),
  })
})

describe('设备头', () => {
  it('🔴 头名必须是 X-Device-Token，不是 X-Device-Id', async () => {
    const { seen, restore } = captureAdapter()
    try {
      await request({ url: '/auth/me', method: 'POST' })
    } finally {
      restore()
    }

    expect(headerOf(seen[0], 'X-Device-Token')).toBe('dv_1.payload.sig')
    /*
     * X-Device-Id 是【网关 → 内部服务】那一段的头，装的是验签后的设备号。
     * 客户端往那个头里塞令牌，网关根本不看 —— 请求全部成功，
     * 只是 device_id 恒为 NULL。第一版客户端就是这么错的，而且一点报错都没有。
     */
    expect(headerOf(seen[0], 'X-Device-Id')).toBeUndefined()
  })

  it('🔴 领设备身份那条请求自己不带，也不等 —— 等它就是等自己，全站永久挂起', async () => {
    const { seen, restore } = captureAdapter()
    configureHttp({
      getToken: () => null,
      onLoginRequired: () => {},
      // 真实实现在这里会去调 /device/register，从而回到本拦截器
      ensureDeviceToken: () => Promise.reject(new Error('不该被调用')),
    })

    try {
      await request({ url: DEVICE_REGISTER_URL, method: 'POST', data: { deviceType: 'H5' } })
    } finally {
      restore()
    }

    expect(headerOf(seen[0], 'X-Device-Token')).toBeUndefined()
  })

  it('拿不到设备令牌时照常发请求 —— 设备身份是增强，不是前置条件', async () => {
    const { seen, restore } = captureAdapter()
    configureHttp({
      getToken: () => null,
      onLoginRequired: () => {},
      ensureDeviceToken: () => Promise.resolve(null),
    })

    try {
      await request({ url: '/auth/login', method: 'POST' })
    } finally {
      restore()
    }

    expect(seen).toHaveLength(1)
    expect(headerOf(seen[0], 'X-Device-Token')).toBeUndefined()
  })

  it('🔴 不传 ensureDeviceToken = 不带设备头，而不是沿用上一次注入的', async () => {
    const { seen, restore } = captureAdapter()
    // 上面的 beforeEach 刚注入过一个会返回令牌的 provider。
    // 「传了才覆盖」的写法下，这一行改不掉它，而且没有任何办法看出来
    configureHttp({ getToken: () => null, onLoginRequired: vi.fn() })

    try {
      await request({ url: '/auth/login', method: 'POST' })
    } finally {
      restore()
    }

    expect(seen).toHaveLength(1)
    expect(headerOf(seen[0], 'X-Device-Token')).toBeUndefined()
  })
})
