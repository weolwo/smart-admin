<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { sendSmsCode } from '@/api/auth'
import { ApiError } from '@/api/errors'
import { useCodeSender } from '@/composables/useCodeSender'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const phone = ref('')
const smsCode = ref('')
const password = ref('')
const confirmPassword = ref('')
const submitting = ref(false)
const errorMessage = ref('')
const errorTraceId = ref<string | null>(null)
/** 手机号已被注册时立起来：此时错误区要多给一个「去登录」的出口，光一行红字没用 */
const phoneTaken = ref(false)

const phoneError = ref<string | undefined>(undefined)
const smsCodeError = ref<string | undefined>(undefined)
const passwordError = ref<string | undefined>(undefined)
const confirmError = ref<string | undefined>(undefined)

/**
 * 「获取验证码」。倒计时、发送中、失败提示全在 useCodeSender 里。
 *
 * 🔴 手机号没填时**在这里拦掉**，不发请求。短信是要花钱的接口，
 * 让一个空号码走一趟真实请求去换一句「格式不正确」没有道理。
 */
const codeSender = useCodeSender(() => sendSmsCode('REGISTER', phone.value.trim()), {
  precheck: () => {
    if (phone.value.trim() === '') {
      // 提示挂在【手机号】那一栏上，不是验证码那一栏 —— 要填的是手机号
      phoneError.value = '请先输入手机号'
      return false
    }
    return true
  },
})

/*
 * 换了手机号，之前那个码就是发给别人的了 —— 状态必须跟着清。
 * 不清的话：用户给 A 号发了码，改成 B 号，那个码还躺在框里，
 * 提交时报「验证码错误」，而他明明刚收到过一条。
 */
watch(phone, () => {
  smsCode.value = ''
  smsCodeError.value = undefined
  /*
   * 手机号那一栏的错也要清。不清的话，用户按提示补上号码、成功拿到验证码之后，
   * 「请先输入手机号」还挂在框下面 —— 他刚照做完，页面却还在指责他。
   * 这条错在下一次 validate() 时才会被覆盖，也就是要等到他点提交。
   */
  phoneError.value = undefined
  phoneTaken.value = false
  codeSender.reset()
})

/** 文案，不是校验。权威规则在后端 MemberPasswordPolicy */
const PASSWORD_HINT = '8-32 位，需同时包含字母和数字'

/**
 * 刻意不在前端写手机号正则，也不在前端实现密码强度规则。
 *
 * 与 LoginView 同一个理由：手机号的格式校验在后端 MemberPhoneUtil.normalize 里，
 * 规范化和校验是同一件事的两面；密码规则在 MemberPasswordPolicy 里。
 * 前端各写一份，就是第二份规则，两份迟早对不上。
 *
 * 所以这里只判三件<b>前端自己就能确定</b>的事：三个框都填了、两次密码一致。
 * 「两次密码不一致」是纯交互问题 —— 用户在同一个表单里打了两遍，传上去再比一次
 * 发现不了任何前端发现不了的问题。后端 MemberRegisterRequest 因此刻意没有
 * confirmPassword 字段。
 */
function validate(): boolean {
  phoneError.value = phone.value.trim() === '' ? '请输入手机号' : undefined
  smsCodeError.value = smsCode.value.trim() === '' ? '请输入验证码' : undefined
  passwordError.value = password.value === '' ? '请设置密码' : undefined

  if (confirmPassword.value === '') {
    confirmError.value = '请再次输入密码'
  } else if (confirmPassword.value !== password.value) {
    confirmError.value = '两次输入的密码不一致'
  } else {
    confirmError.value = undefined
  }

  return (
    phoneError.value === undefined &&
    smsCodeError.value === undefined &&
    passwordError.value === undefined &&
    confirmError.value === undefined
  )
}

async function submit(): Promise<void> {
  if (submitting.value) {
    return
  }
  errorMessage.value = ''
  errorTraceId.value = null
  phoneTaken.value = false
  smsCodeError.value = undefined
  if (!validate()) {
    return
  }

  submitting.value = true
  try {
    await auth.register({
      registerType: 'PHONE_PASSWORD',
      identity: phone.value.trim(),
      smsCode: smsCode.value.trim(),
      password: password.value,
      deviceType: 'H5',
    })
    // 注册即登录：后端把令牌一起返回了，直接进目标页，不再跳一次登录
    const redirect = route.query.redirect
    await router.replace(typeof redirect === 'string' ? redirect : '/')
  } catch (error) {
    if (error instanceof ApiError) {
      if (error.code === 'CONFLICT') {
        // 409：这个号已经有主了。挂到手机号字段上，并在下面给一个「去登录」的出口
        phoneTaken.value = true
        phoneError.value = error.message
      } else if (error.code === 'BAD_CREDENTIALS') {
        /*
         * 401 在注册这条路上只有一个来源：验证码不对/已失效/错太多次。
         * 挂到验证码框上，而不是丢进表单级错误区 —— 用户要知道该重填哪一栏，
         * 而「验证码错误」这四个字放在最下面时，他往往先去检查手机号
         */
        smsCodeError.value = error.message
      } else {
        // WEAK_PASSWORD 时后端的 message 就是规则原文，比前端那句提示更权威，原样展示
        errorMessage.value = error.message
      }
      errorTraceId.value = error.traceId
    } else {
      errorMessage.value = '注册失败，请稍后再试'
    }
  } finally {
    submitting.value = false
  }
}

async function goLogin(): Promise<void> {
  await router.replace({ name: 'login', query: route.query })
}
</script>

<template>
  <div class="page">
    <header class="page__head">
      <h1 class="page__title">创建账号</h1>
      <p class="page__subtitle">手机号注册，注册完直接开抽</p>
    </header>

    <form class="page__form" novalidate @submit.prevent="submit">
      <Field
        v-model="phone"
        icon="phone"
        type="tel"
        placeholder="手机号"
        autocomplete="username"
        :maxlength="11"
        :error="phoneError"
      />
      <Field
        v-model="smsCode"
        icon="lock"
        type="tel"
        placeholder="短信验证码"
        autocomplete="one-time-code"
        :maxlength="6"
        :error="smsCodeError ?? codeSender.error.value"
      >
        <!--
          按钮放进输入框内部，而不是并排两个控件：并排时两者宽度要靠 flex 分，
          在窄屏上验证码框会被挤到只剩四五个字符宽。
        -->
        <template #suffix>
          <Button
            variant="text"
            type="button"
            :disabled="!codeSender.canSend.value"
            @click="codeSender.send"
          >
            {{ codeSender.label.value }}
          </Button>
        </template>
      </Field>

      <Field
        v-model="password"
        icon="lock"
        type="password"
        placeholder="设置密码"
        autocomplete="new-password"
        :hint="PASSWORD_HINT"
        :error="passwordError"
      />
      <Field
        v-model="confirmPassword"
        icon="lock"
        type="password"
        placeholder="再次输入密码"
        autocomplete="new-password"
        :error="confirmError"
      />

      <p v-if="errorMessage !== ''" class="page__error" role="alert">
        {{ errorMessage }}
        <!-- traceId 一定要露出来：用户截图报障时，凭它一次定位到服务端日志 -->
        <span v-if="errorTraceId !== null" class="page__trace">（编号 {{ errorTraceId }}）</span>
      </p>

      <Button type="submit" :loading="submitting" class="page__submit">注册</Button>

      <!-- 号已被占用是唯一一种「用户下一步很明确」的失败，给个直达按钮比让他找返回键强 -->
      <Button v-if="phoneTaken" variant="text" @click="goLogin">该手机号已注册，去登录</Button>
    </form>

    <p class="page__alt">
      已有账号？<RouterLink class="page__link" :to="{ name: 'login', query: route.query }">
        去登录
      </RouterLink>
    </p>
  </div>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  min-height: 100%;
  padding: calc(var(--sv-safe-top) + var(--sv-space-xl)) var(--sv-space-page)
    calc(var(--sv-safe-bottom) + var(--sv-space-lg));
}

.page__head {
  margin-bottom: var(--sv-space-xl);
}

.page__title {
  margin: 0;
  font-size: var(--sv-font-title);
  font-weight: 700;
  letter-spacing: -0.01em;
  line-height: 1.2;
}

.page__subtitle {
  margin: var(--sv-space-sm) 0 0;
  color: var(--sv-text-secondary);
  font-size: var(--sv-font-caption);
}

.page__form {
  display: flex;
  flex-direction: column;
  gap: var(--sv-space-md);
}

.page__error {
  margin: 0;
  padding: 0 var(--sv-space-md);
  color: var(--sv-color-danger);
  font-size: var(--sv-font-footnote);
  line-height: 1.5;
}

.page__trace {
  color: var(--sv-text-placeholder);
}

.page__submit {
  margin-top: var(--sv-space-sm);
}

.page__alt {
  margin: var(--sv-space-xl) 0 0;
  padding-top: var(--sv-space-lg);
  text-align: center;
  color: var(--sv-text-secondary);
  font-size: var(--sv-font-caption);
}

.page__link {
  color: var(--sv-color-primary);
  font-weight: 500;
  text-decoration: none;
}

.page__link:active {
  opacity: 0.6;
}
</style>
