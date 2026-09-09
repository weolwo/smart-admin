import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import type { MemberContact } from '@/api/auth'

import EmailBindView from '../EmailBindView.vue'
import PhoneBindView from '../PhoneBindView.vue'

/**
 * 绑定 / 更换 手机号与邮箱。
 *
 * <h3>要钉的是那条权限提升链在界面上的落点</h3>
 * <pre>会话被盗 → 换绑成攻击者的联系方式 → 用它登录/找回密码 → 永久接管</pre>
 * 服务端已经拦住了（换绑必须给原主证明）。但界面这一侧有两种做错的方式，
 * 而且都<b>不会报错</b>：
 * <ul>
 *   <li><b>首次绑定也要原主证明</b> —— 没绑过的人拿不出任何证明，
 *       这一页对他就是死的；</li>
 *   <li><b>给没设过密码的人显示「输入当前密码」</b> —— 他对着一个
 *       永远填不了的框，而页面不会告诉他为什么。</li>
 * </ul>
 *
 * <p>两个页面逐条对称测，因为它们的规则本来就该一样 ——
 * 差异只应该来自通道本身，而不是「当时谁写的」。
 */

const fetchContact = vi.hoisted(() => vi.fn())
const bindPhone = vi.hoisted(() => vi.fn(() => Promise.resolve()))
const bindEmail = vi.hoisted(() => vi.fn(() => Promise.resolve()))
const sendSmsCode = vi.hoisted(() => vi.fn(() => Promise.resolve()))
const sendEmailCode = vi.hoisted(() => vi.fn(() => Promise.resolve()))

/* mock 工厂里不能写 import() 类型注解（eslint），先在这里起个别名 */
/* eslint-disable-next-line @typescript-eslint/consistent-type-imports */
type AuthModule = typeof import('@/api/auth')

vi.mock('@/api/auth', async (importOriginal) => ({
  ...(await importOriginal<AuthModule>()),
  fetchContact,
  bindPhone,
  bindEmail,
  sendSmsCode,
  sendEmailCode,
}))

function contact(over: Partial<MemberContact>): MemberContact {
  return { phone: null, email: null, passwordSet: false, ...over }
}

async function mountPage(component: unknown) {
  const w = mount(component as never)
  await flushPromises()
  return w
}

type Wrapper = Awaited<ReturnType<typeof mountPage>>

function inputOf(w: Wrapper, placeholder: string) {
  return w.findAll('input').find((i) => i.attributes('placeholder') === placeholder)
}

function fieldOf(w: Wrapper, placeholder: string) {
  const found = inputOf(w, placeholder)
  expect(found, `没有 placeholder 为「${placeholder}」的输入框`).toBeDefined()
  return found!
}

beforeEach(() => {
  fetchContact.mockReset()
  bindPhone.mockReset()
  bindPhone.mockResolvedValue(undefined)
  bindEmail.mockReset()
  bindEmail.mockResolvedValue(undefined)
  sendSmsCode.mockReset()
  sendSmsCode.mockResolvedValue(undefined)
  sendEmailCode.mockReset()
  sendEmailCode.mockResolvedValue(undefined)
})

describe('绑定手机号', () => {
  it('未绑定 → 标题是「绑定」，且【不要】原主证明', async () => {
    fetchContact.mockResolvedValue(contact({}))

    const w = await mountPage(PhoneBindView)

    expect(w.text()).toContain('未绑定')
    expect(
      inputOf(w, '当前密码'),
      '首次绑定的人拿不出任何证明，要了这一页对他就是死的',
    ).toBeUndefined()
  })

  it('🔴 已绑定 + 有密码 → 要原主证明，默认用当前密码', async () => {
    fetchContact.mockResolvedValue(contact({ phone: '138****8000', passwordSet: true }))

    const w = await mountPage(PhoneBindView)

    expect(w.text()).toContain('138****8000')
    expect(inputOf(w, '当前密码'), '换绑不要证明的话，偷到一个 token 就等于拿走账号').toBeDefined()
  })

  it('🔴 已绑定 + 没设过密码 → 不给「当前密码」，只给原手机号验证码', async () => {
    fetchContact.mockResolvedValue(contact({ phone: '138****8000', passwordSet: false }))

    const w = await mountPage(PhoneBindView)

    expect(
      inputOf(w, '当前密码'),
      '他从来没设过密码，这个框永远填不对，而页面不会告诉他为什么',
    ).toBeUndefined()
    expect(inputOf(w, '当前绑定的手机号')).toBeDefined()
    expect(w.text()).toContain('你还没有设置密码')
  })

  it('发码用 BIND 场景 —— 与注册那条码互不相干', async () => {
    fetchContact.mockResolvedValue(contact({}))
    const w = await mountPage(PhoneBindView)
    await fieldOf(w, '新手机号').setValue('13800138000')

    await w
      .findAll('button')
      .find((b) => b.text().includes('获取验证码'))
      ?.trigger('click')
    await flushPromises()

    expect(sendSmsCode).toHaveBeenCalledWith('BIND', '13800138000')
  })

  it('首次绑定提交：只带新号码和码，不带任何证明', async () => {
    fetchContact.mockResolvedValue(contact({}))
    const w = await mountPage(PhoneBindView)
    await fieldOf(w, '新手机号').setValue('13800138000')
    await fieldOf(w, '新手机号收到的验证码').setValue('123456')

    await w.find('form').trigger('submit')
    await flushPromises()

    expect(bindPhone).toHaveBeenCalledWith({ phone: '13800138000', code: '123456' })
  })

  it('🔴 换绑提交：只带用上的那一种证明，另一种不出现在请求里', async () => {
    fetchContact.mockResolvedValue(contact({ phone: '138****8000', passwordSet: true }))
    const w = await mountPage(PhoneBindView)
    await fieldOf(w, '新手机号').setValue('13900139000')
    await fieldOf(w, '新手机号收到的验证码').setValue('123456')
    await fieldOf(w, '当前密码').setValue('abcd1234')

    await w.find('form').trigger('submit')
    await flushPromises()

    const sent = (bindPhone.mock.calls as unknown as unknown[][])[0]?.[0] as Record<string, unknown>
    expect(sent.currentPassword).toBe('abcd1234')
    expect(sent, '两个都带的话另一个白填，而用户不知道自己填的哪个生效了').not.toHaveProperty(
      'oldPhoneCode',
    )
  })

  it('换绑没给证明 → 本地就拦下，不发请求', async () => {
    fetchContact.mockResolvedValue(contact({ phone: '138****8000', passwordSet: true }))
    const w = await mountPage(PhoneBindView)
    await fieldOf(w, '新手机号').setValue('13900139000')
    await fieldOf(w, '新手机号收到的验证码').setValue('123456')

    await w.find('form').trigger('submit')
    await flushPromises()

    expect(bindPhone).not.toHaveBeenCalled()
    expect(w.text()).toContain('请输入当前密码')
  })
})

describe('绑定邮箱', () => {
  it('未绑定 → 不要原主证明', async () => {
    fetchContact.mockResolvedValue(contact({}))

    const w = await mountPage(EmailBindView)

    expect(w.text()).toContain('未绑定')
    expect(inputOf(w, '当前密码')).toBeUndefined()
  })

  it('🔴 已绑定 + 没设过密码 → 只给旧邮箱那条路', async () => {
    fetchContact.mockResolvedValue(contact({ email: 'a***@example.com', passwordSet: false }))

    const w = await mountPage(EmailBindView)

    expect(inputOf(w, '当前密码')).toBeUndefined()
    expect(inputOf(w, '当前绑定的邮箱'), '这是他唯一的出路，必须给').toBeDefined()
  })

  it('发码用 BIND 场景', async () => {
    fetchContact.mockResolvedValue(contact({}))
    const w = await mountPage(EmailBindView)
    await fieldOf(w, '新邮箱').setValue('new@example.com')

    await w
      .findAll('button')
      .find((b) => b.text().includes('获取验证码'))
      ?.trigger('click')
    await flushPromises()

    expect(sendEmailCode).toHaveBeenCalledWith('BIND', 'new@example.com')
  })

  it('🔴 旧邮箱那条码发到【用户自己填的那个地址】—— 页面上只有脱敏值', async () => {
    fetchContact.mockResolvedValue(contact({ email: 'a***@example.com', passwordSet: false }))
    const w = await mountPage(EmailBindView)
    await fieldOf(w, '当前绑定的邮箱').setValue('old@example.com')

    // 第二个「获取验证码」是旧邮箱那一栏的
    const buttons = w.findAll('button').filter((b) => b.text().includes('获取验证码'))
    await buttons[1]?.trigger('click')
    await flushPromises()

    expect(sendEmailCode).toHaveBeenCalledWith('BIND', 'old@example.com')
  })
})
