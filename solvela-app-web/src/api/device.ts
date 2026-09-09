import { readDevice, writeDevice, type StoredDevice } from '@/utils/device-storage'

import { type DeviceType } from './auth'
import { DEVICE_REGISTER_URL, request } from './http'

/**
 * 设备身份。**首次启动时领一次，不是首次登录**。
 *
 * <p>匿名接口（注册、登录、活动页）也要有设备身份 —— 而防刷要防的恰恰是它们。
 * 等到登录才领，等于把最需要保护的那几条路留在外面。
 */

export interface DeviceRegisterPayload {
  deviceType: DeviceType
  /**
   * 品牌型号，如 "iPhone 15 Pro"。
   *
   * 🔴 **H5 不要填**。浏览器里拿得到的只有 userAgent，那不是型号 ——
   * 把一串 UA 塞进 `t_device.model`，那一列就再也没法用来分组统计了
   * （「这批号是不是同一款机器注册的」正是它的用途）。
   * 等原生壳把这个页面包起来，由壳传真值。
   */
  model?: string
  osVersion?: string
  appVersion?: string
}

interface RawDeviceRegisterView {
  deviceToken: string
  deviceId: string
}

/**
 * 领一个设备身份并存下来。
 *
 * <p>⚠️ **不是幂等的**：每调一次服务端就新建一台设备（它无从判断「这是不是同一台」——
 * 能判断的前提是客户端能自证身份，而此刻它恰恰还没有身份）。
 * 防重复全靠客户端只在没有的时候调，也就是 {@link ensureDevice}。
 */
export async function registerDevice(payload: DeviceRegisterPayload): Promise<StoredDevice> {
  const raw = await request<RawDeviceRegisterView>({
    url: DEVICE_REGISTER_URL,
    method: 'POST',
    data: payload,
  })
  writeDevice(raw.deviceToken, raw.deviceId)
  return { token: raw.deviceToken, deviceId: raw.deviceId }
}

/**
 * 并发去重用的在途 promise。
 *
 * 🔴 没有它的话，冷启动那一瞬间并发的几个请求会**各领一台设备** ——
 * 而领设备不是幂等的，于是一个用户在 t_device 里凭空多出三四行，
 * 「一台设备碰过哪些账号」那类统计从第一天起就是错的。
 */
let inflight: Promise<StoredDevice | null> | null = null

/**
 * 确保有设备身份：有就直接返回，没有就领一个。
 *
 * <p>**失败返回 null，不抛**。设备身份是一层增强，不是前置条件：
 * 领不到的时候用户照样该能登录、能抽奖。服务端当前是 observe 模式
 *（见 solvela.app.auth.device.mode），没有设备头只是少一条记录。
 */
export async function ensureDevice(deviceType: DeviceType = 'H5'): Promise<StoredDevice | null> {
  const stored = readDevice()
  if (stored !== null) {
    return stored
  }
  if (inflight === null) {
    inflight = registerDevice({ deviceType })
      .catch(() => null)
      .finally(() => {
        inflight = null
      })
  }
  return inflight
}
