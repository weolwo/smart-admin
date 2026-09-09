import { type Id, toId } from '@/types/contract'

import { request, requestVoid } from './http'

/** 设备端，取值对齐 t_member_login_log.device_type */
export const DEVICE_TYPES = ['APP', 'H5', 'WECHAT', 'PC'] as const
export type DeviceType = (typeof DEVICE_TYPES)[number]

/**
 * 登录方式。对齐后端 MemberLoginType。
 *
 * `identity` 和 `credential` 装什么，由它决定：
 *   PHONE_PASSWORD  手机号 + 密码
 *   EMAIL_PASSWORD  邮箱   + 密码
 *   EMAIL_CODE      邮箱   + 邮箱验证码
 */
export const LOGIN_TYPES = ['PHONE_PASSWORD', 'EMAIL_PASSWORD', 'EMAIL_CODE'] as const
export type LoginType = (typeof LOGIN_TYPES)[number]

/** 注册方式。对齐后端 MemberRegisterType */
export const REGISTER_TYPES = ['PHONE_PASSWORD', 'EMAIL_CODE'] as const
export type RegisterType = (typeof REGISTER_TYPES)[number]

/**
 * 登录入参。
 *
 * 🔴 字段叫 `identity` / `credential`，**不是** `phone` / `password`。
 * 后端 2026-09-09 改的名，理由写在 MemberLoginRequest 的注释里：
 * 「继续叫 phone 但有时候放的是邮箱」是一个迟早会骗到人的字段名。
 */
export interface LoginPayload {
  loginType?: LoginType
  identity: string
  credential: string
  deviceType?: DeviceType
}

/**
 * 当前登录会员。对应后端 MemberPrincipal。
 *
 * **这里没有手机号是后端刻意的**：该对象会进 Redis、进日志，放明文手机号会让整套 PII 加密失效。
 * 需要展示手机号的页面走单独接口，拿的是脱敏后的值。
 */
export interface MemberProfile {
  memberId: Id
  memberName: string
  nickname: string
  /** 头像 file_id，可能为空 */
  avatarFileId: Id | null
  /** 0-未知 1-男 2-女 */
  gender: number | null
}

/**
 * 注册入参。
 *
 * 两条通道共用这一个形状，由 `registerType` 分派：
 *   PHONE_PASSWORD  identity=手机号，要 smsCode 和 password
 *   EMAIL_CODE      identity=邮箱，  要 emailCode，password 可以不填
 *
 * `smsCode` **一直传**就行：服务端有一个 `phone-code-required` 开关，
 * 关掉时它不看这个字段，多传一个没有代价；而漏传会在开关打开的那天变成注册全线失败。
 */
export interface RegisterPayload {
  registerType?: RegisterType
  identity: string
  /** 邮箱注册的验证码 */
  emailCode?: string
  /** 手机号注册的短信验证码 */
  smsCode?: string
  /** 邮箱注册时可以不填 —— 那种会员之后走验证码登录 */
  password?: string
  deviceType?: DeviceType
}

export interface LoginResult {
  accessToken: string
  /** 有效期秒数。用来提前续期，而不是等 401 才反应 */
  expiresIn: number
  member: MemberProfile
}

/** 后端原始形状：memberId / avatarFileId 是 Long，小值下发为数字 */
interface RawMemberProfile {
  memberId: string | number
  memberName: string
  nickname: string
  avatarFileId: string | number | null
  gender: number | null
}

interface RawLoginResult {
  accessToken: string
  expiresIn: number
  member: RawMemberProfile
}

/** 反序列化边界：所有 Long 字段在这里归一成字符串，往后不再出现 number 型 ID */
function normalizeMember(raw: RawMemberProfile): MemberProfile {
  return {
    memberId: toId(raw.memberId),
    memberName: raw.memberName,
    nickname: raw.nickname,
    avatarFileId: raw.avatarFileId === null ? null : toId(raw.avatarFileId),
    gender: raw.gender,
  }
}

function toLoginResult(raw: RawLoginResult): LoginResult {
  return {
    accessToken: raw.accessToken,
    expiresIn: raw.expiresIn,
    member: normalizeMember(raw.member),
  }
}

/**
 * 注册。**返回形状与登录完全一致**，所以调用方走同一条「存令牌 + 存会员信息」的路。
 *
 * 后端注册成功直接签令牌（见 MemberLoginController.register 的注释）——
 * 没有「注册完再登一次」这一步，那一步不产生任何信息，只多一次可能失败的调用。
 *
 * 失败时抛 ApiError，几个码各有含义，注册页据此分支：
 *   CONFLICT(409)          手机号/邮箱已注册 → 引导去登录，不要只显示一行红字
 *   INVALID_ARGUMENT(400)  格式错 / 密码太弱 → message 就是规则原文，直接展示
 *   BAD_CREDENTIALS(401)   验证码错、失效、错太多次 → 挂在验证码框上
 *   OPERATION_LIMITED(429) 注册过于频繁 → message 里已带「还要等多久」
 */
export async function register(payload: RegisterPayload): Promise<LoginResult> {
  const raw = await request<RawLoginResult>({
    url: '/auth/register',
    method: 'POST',
    data: { registerType: 'PHONE_PASSWORD', deviceType: 'H5', ...payload },
  })
  return toLoginResult(raw)
}

export async function login(payload: LoginPayload): Promise<LoginResult> {
  const raw = await request<RawLoginResult>({
    url: '/auth/login',
    method: 'POST',
    data: { loginType: 'PHONE_PASSWORD', deviceType: 'H5', ...payload },
  })
  return toLoginResult(raw)
}

/**
 * 短信验证码的用途。对齐后端 SmsScene。
 *
 * 比邮箱**少一个 BIND** —— 绑定手机号还没做。
 *
 * 🔴 场景必须显式传，服务端不给默认值：默认成注册的话，
 * 用户拿去重置密码时验不过，而两边看起来都很正常。
 */
export const SMS_SCENES = ['REGISTER', 'LOGIN', 'RESET_PASSWORD'] as const
export type SmsScene = (typeof SMS_SCENES)[number]

/** 邮箱验证码的用途。对齐后端 EmailCodeScene */
export const EMAIL_CODE_SCENES = ['REGISTER', 'LOGIN', 'BIND', 'RESET_PASSWORD'] as const
export type EmailCodeScene = (typeof EMAIL_CODE_SCENES)[number]

/**
 * 索取短信验证码。成功返回 204，**没有响应体**。
 *
 * <p>⚠️ 短信是**要花钱**的接口，服务端的 IP 日限比邮箱紧得多。
 * 所以这颗按钮必须有冷却（见 useCodeSender），不能让用户连点。
 *
 * <p>失败时抛 ApiError：
 *   INVALID_ARGUMENT(400)  号码格式不对
 *   OPERATION_LIMITED(429) 冷却中 / 今天发太多了，message 里已带「还要等多久」
 *   INTERNAL(500)          发不出去 —— 这是**我们的**问题，别让用户以为号码填错了
 */
export async function sendSmsCode(scene: SmsScene, phone: string): Promise<void> {
  await requestVoid({ url: '/auth/sms/code', method: 'POST', data: { scene, phone } })
}

/**
 * 索取邮箱验证码。成功返回 204，**没有响应体**。
 *
 * <p>🔴 **成功不代表真的寄了一封信。** 邮箱与场景不匹配时（拿一个没注册过的邮箱
 * 要登录验证码、拿一个已注册的邮箱要注册验证码）服务端会静默成功 ——
 * 如实回答等于送出一个账号枚举接口。
 *
 * <p>所以调用方**不要**把成功解释成「这个邮箱存在/不存在」，
 * 提示语只能是「已发送」这一句，不能有第二种。
 */
export async function sendEmailCode(scene: EmailCodeScene, email: string): Promise<void> {
  await requestVoid({ url: '/auth/email/code', method: 'POST', data: { scene, email } })
}

/** 后端返回 204，没有响应体 */
export async function logout(): Promise<void> {
  await requestVoid({ url: '/auth/logout', method: 'POST' })
}

/** 冷启动时确认本地令牌是否还有效。注意是 POST，不是 GET */
export async function fetchMe(): Promise<MemberProfile> {
  const raw = await request<RawMemberProfile>({ url: '/auth/me', method: 'POST' })
  return normalizeMember(raw)
}
