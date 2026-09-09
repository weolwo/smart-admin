import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createMemoryHistory, createRouter } from 'vue-router'

import { ApiError } from '@/api/errors'

import RegisterView from '../RegisterView.vue'

/**
 * 手机号注册页。
 *
 * <h3>它现在多了一步，而那一步是整条链路上最容易做拧的</h3>
 * 服务端 2026-09-10 起要求手机号注册必须验短信验证码。前端这一侧要做对四件事，
 * 每一件做错的表现都是「用户卡住，而服务端日志里一切正常」：
 * <ul>
 *   <li>手机号没填就点「获取」→ **不能发请求**。短信是要花钱的接口；</li>
 *   <li>改了手机号 → 之前那个码必须清掉，否则用户拿着 A 号的码去注册 B 号；</li>
 *   <li>提交时要真的把 smsCode 带上；</li>
 *   <li>401 要挂到验证码框上，不能丢进表单底部那个大红字区。</li>
 * </ul>
 */

const sendSmsCode = vi.hoisted(() => vi.fn(() => Promise.resolve()))
const registerApi = vi.hoisted(() => vi.fn())

/* mock 工厂里不能写 import() 类型注解（eslint），先在这里起个别名 */
/* eslint-disable-next-line @typescript-eslint/consistent-type-imports */
type AuthModule = typeof import('@/api/auth')

vi.mock('@/api/auth', async (importOriginal) => ({
  ...(await importOriginal<AuthModule>()),
  sendSmsCode,
  register: registerApi,
}))

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/', name: 'feed', component: { template: '<div/>' } },
    { path: '/login', name: 'login', component: { template: '<div/>' } },
    { path: '/register', name: 'register', component: { template: '<div/>' } },
  ],
})

const OK = {
  accessToken: 'mb_x',
  expiresIn: 3600,
  member: {
    memberId: '1000000001',
    memberName: 'sv1000000001',
    nickname: '会员',
    avatarFileId: null,
    gender: 0,
  },
}

async function mountPage() {
  await router.push('/register')
  await router.isReady()
  const w = mount(RegisterView, { global: { plugins: [router] } })
  await flushPromises()
  return w
}

type Wrapper = Awaited<ReturnType<typeof mountPage>>

/** 按 placeholder 找输入框 —— 这一页四个框全靠 placeholder 区分 */
function fieldByPlaceholder(w: Wrapper, placeholder: string) {
  const found = w.findAll('input').find((i) => i.attributes('placeholder') === placeholder)
  expect(found, `没有 placeholder 为「${placeholder}」的输入框`).toBeDefined()
  return found!
}

function codeButton(w: Wrapper) {
  const found = w.findAll('button').find((b) => /获取验证码|重新获取|发送中|^\d+s$/.test(b.text()))
  expect(found, '没有找到「获取验证码」按钮').toBeDefined()
  return found!
}

beforeEach(() => {
  setActivePinia(createPinia())
  sendSmsCode.mockReset()
  sendSmsCode.mockResolvedValue(undefined)
  registerApi.mockReset()
  registerApi.mockResolvedValue(OK)
})

describe('获取验证码', () => {
  it('🔴 手机号没填就点 → 一个请求都不发。短信是要花钱的接口', async () => {
    const w = await mountPage()

    await codeButton(w).trigger('click')
    await flushPromises()

    expect(sendSmsCode).not.toHaveBeenCalled()
    expect(w.text()).toContain('请先输入手机号')
    /*
     * 🔴 而且【只有】这一条。本地校验失败不是一次失败的发送 ——
     * 早先这里会同时冒出「验证码发送失败，请稍后再试」，那句是假的：
     * 什么都没发生过，而它会让用户以为服务坏了，去反复重试而不是去填手机号。
     */
    expect(w.text()).not.toContain('验证码发送失败')
  })

  it('🔴 补上手机号之后，那句「请先输入手机号」要消失 —— 他刚照做完', async () => {
    const w = await mountPage()
    await codeButton(w).trigger('click')
    await flushPromises()
    expect(w.text()).toContain('请先输入手机号')

    await fieldByPlaceholder(w, '手机号').setValue('13800138000')
    await flushPromises()

    expect(w.text()).not.toContain('请先输入手机号')
  })

  it('填了就发，场景是 REGISTER —— 场景传错的话码验不过，而两边都看不出问题', async () => {
    const w = await mountPage()
    await fieldByPlaceholder(w, '手机号').setValue('13800138000')

    await codeButton(w).trigger('click')
    await flushPromises()

    expect(sendSmsCode).toHaveBeenCalledWith('REGISTER', '13800138000')
  })

  it('发出去之后进冷却，按钮点不动', async () => {
    const w = await mountPage()
    await fieldByPlaceholder(w, '手机号').setValue('13800138000')
    await codeButton(w).trigger('click')
    await flushPromises()

    await codeButton(w).trigger('click')
    await flushPromises()

    expect(sendSmsCode).toHaveBeenCalledTimes(1)
  })

  it('🔴 改了手机号 → 已填的验证码要清掉，冷却也要解除', async () => {
    const w = await mountPage()
    await fieldByPlaceholder(w, '手机号').setValue('13800138000')
    await codeButton(w).trigger('click')
    await flushPromises()
    await fieldByPlaceholder(w, '短信验证码').setValue('123456')

    await fieldByPlaceholder(w, '手机号').setValue('13900139000')
    await flushPromises()

    expect(
      fieldByPlaceholder(w, '短信验证码').element.value,
      '留着上一个号的码，用户提交时会收到「验证码错误」—— 而他明明刚收到过一条',
    ).toBe('')
    expect(codeButton(w).text()).toBe('获取验证码')
  })
})

describe('提交', () => {
  it('验证码没填 → 本地就拦下，不发注册请求', async () => {
    const w = await mountPage()
    await fieldByPlaceholder(w, '手机号').setValue('13800138000')
    await fieldByPlaceholder(w, '设置密码').setValue('abcd1234')
    await fieldByPlaceholder(w, '再次输入密码').setValue('abcd1234')

    await w.find('form').trigger('submit')
    await flushPromises()

    expect(registerApi).not.toHaveBeenCalled()
    expect(w.text()).toContain('请输入验证码')
  })

  it('🔴 smsCode 要真的带上去', async () => {
    const w = await mountPage()
    await fieldByPlaceholder(w, '手机号').setValue('13800138000')
    await fieldByPlaceholder(w, '短信验证码').setValue('123456')
    await fieldByPlaceholder(w, '设置密码').setValue('abcd1234')
    await fieldByPlaceholder(w, '再次输入密码').setValue('abcd1234')

    await w.find('form').trigger('submit')
    await flushPromises()

    expect(registerApi).toHaveBeenCalledWith(
      expect.objectContaining({
        registerType: 'PHONE_PASSWORD',
        identity: '13800138000',
        smsCode: '123456',
      }),
    )
  })

  it('🔴 验证码错（401）挂在验证码框上，不是丢进表单底部', async () => {
    registerApi.mockRejectedValue(new ApiError('BAD_CREDENTIALS', '验证码错误', 'tr-1', 401))
    const w = await mountPage()
    await fieldByPlaceholder(w, '手机号').setValue('13800138000')
    await fieldByPlaceholder(w, '短信验证码').setValue('000000')
    await fieldByPlaceholder(w, '设置密码').setValue('abcd1234')
    await fieldByPlaceholder(w, '再次输入密码').setValue('abcd1234')

    await w.find('form').trigger('submit')
    await flushPromises()

    // 挂在字段上时，错误文案在那个 Field 自己的提示行里
    const fieldMsgs = w.findAll('.sv-field__msg--error').map((n) => n.text())
    expect(fieldMsgs, '「验证码错误」放在页面最下面时，用户往往先去检查手机号').toContain(
      '验证码错误',
    )
  })
})
