import { request, requestVoid } from './http'

import type { DeviceType } from './auth'

/**
 * 「我的登录设备」。
 *
 * <h3>它回答的是「现在有谁登着我的号」</h3>
 * 只列**活着的**会话 —— 历史登录记录是另一件事（后台的登录日志）。
 * 混进来的话，用户会看到一堆早已失效的设备，对着点不动的下线按钮发愁。
 */

export interface MemberSession {
  /** 这一行的对外标识，「下线」回传的就是它。不是令牌，也推导不出令牌 */
  sessionId: string
  deviceType: DeviceType | null
  /** 服务端签发的设备号，老客户端可能为 null */
  deviceId: string | null
  ip: string | null
  /**
   * IP 归属地。
   *
   * ⚠️ 目前**服务端永远给 null** —— 解析归属地的工具在网关拿不到的模块里
   * （见后端 MemberSessionContext 的类注释）。留着这个字段是因为它迟早会有值，
   * 而那时客户端不用再改一次。
   */
  region: string | null
  /** 登录时间（毫秒）。0 表示这是一条 2026-09-10 之前签发的旧会话，不知道时间 */
  loginTime: number
  /** 是不是用户此刻正在用的这一个。列表里必须标出来 */
  current: boolean
}

/** 当前活着的会话。服务端已按「当前的排最前，其余按登录时间倒序」排好 */
export async function fetchSessions(): Promise<MemberSession[]> {
  return request<MemberSession[]>({ url: '/auth/sessions', method: 'POST' })
}

/**
 * 让某个会话下线。返回 204。
 *
 * 「已经不在了」也是 204 —— 用户要的结果是「那台设备下线」，
 * 而它本来就不在线时这个结果已经成立。
 */
export async function revokeSession(sessionId: string): Promise<void> {
  await requestVoid({ url: '/auth/sessions/revoke', method: 'POST', data: { sessionId } })
}

/** 下线除当前之外的所有会话。自己留着，不用重新登 */
export async function revokeOtherSessions(): Promise<void> {
  await requestVoid({ url: '/auth/sessions/revokeOthers', method: 'POST' })
}
