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
const password = ref('')

/**
 * 这台设备处在观察档，本次登录要多验一道短信验证码。
 *
 * 🔴 **不是登录失败**：密码已经验过了，只是还差一步。所以这一栏是
 * 「冒出来的第二步」，而不是把用户退回去重来 —— 手机号和密码都保持原样。
 */
const challengeRequired = ref(false)
const verificationCode = ref('')
const verificationError = ref<string | undefined>(undefined)
/**
 * 默认勾上。令牌有效期 30 天就是为了让人别每次都登，
 * 默认不勾等于把那个配置作废了。共用设备的人会自己取消。
 */
const remember = ref(true)
const submitting = ref(false)
/** 成功后先显示一下再跳，用户需要一个「确实成了」的确认 */
const succeeded = ref(false)

/** 整体性错误（账号密码不对、账号被禁、限流）。字段级的问题走 xxxError */
const errorMessage = ref('')
const errorTraceId = ref<string | null>(null)
const phoneError = ref<string | undefined>(undefined)
const passwordError = ref<string | undefined>(undefined)

/** 二次验证的「获取验证码」。场景是 LOGIN，与注册那条码互不相干 */
const codeSender = useCodeSender(() => sendSmsCode('LOGIN', phone.value.trim()), {
  precheck: () => {
    if (phone.value.trim() === '') {
      phoneError.value = '请先输入手机号'
      return false
    }
    return true
  },
})

/*
 * 换了手机号，这一整轮二次验证就作废了 —— 那道码是发给上一个号的。
 * 不收起来的话，用户会拿着 A 号的码去登 B 号，得到一句「验证码错误」，
 * 而他完全不知道自己错在哪。
 */
watch(phone, () => {
  challengeRequired.value = false
  verificationCode.value = ''
  verificationError.value = undefined
  phoneError.value = undefined
  codeSender.reset()
})

/** 成功提示停留多久再跳。够看清，又不至于让人等 */
const SUCCESS_DWELL_MS = 700

/**
 * 刻意不在前端写手机号正则。
 *
 * 后端 LoginRequest 的注释说得很清楚：格式校验在 MemberPhoneUtil.normalize 里，
 * 规范化和校验是同一件事的两面。前端再写一条正则就是第二份手机号规则，两份迟早对不上。
 * 这里只做「非空」这种纯交互层面的拦截。
 *
 * 🔴 而且是在【点击之后】拦，不是把按钮置灰。
 * 按钮置灰时用户看不出差哪一项，只知道点不动。
 */
function validate(): boolean {
  phoneError.value = phone.value.trim() === '' ? '请输入手机号' : undefined
  passwordError.value = password.value === '' ? '请输入密码' : undefined
  return phoneError.value === undefined && passwordError.value === undefined
}

async function submit(): Promise<void> {
  if (submitting.value) {
    return
  }
  errorMessage.value = ''
  errorTraceId.value = null
  verificationError.value = undefined
  if (!validate()) {
    return
  }
  if (challengeRequired.value && verificationCode.value.trim() === '') {
    verificationError.value = '请输入验证码'
    return
  }

  submitting.value = true
  try {
    await auth.login(
      {
        // 这一页只做手机号+密码。邮箱那两条通道有各自的入口，
        // 把三种方式塞进同一个表单只会让每一种都别扭
        loginType: 'PHONE_PASSWORD',
        identity: phone.value.trim(),
        credential: password.value,
        // 正常设备上这一项永远是 undefined —— 绝大多数登录不受影响
        verificationCode: verificationCode.value.trim() || undefined,
        deviceType: 'H5',
      },
      remember.value,
    )
    succeeded.value = true
    await new Promise((resolve) => setTimeout(resolve, SUCCESS_DWELL_MS))
    const redirect = route.query.redirect
    await router.replace(typeof redirect === 'string' ? redirect : '/')
  } catch (error) {
    if (error instanceof ApiError) {
      if (error.code === 'DEVICE_VERIFICATION_REQUIRED') {
        /*
         * 🔴 密码是对的，只是这台设备要多验一道。
         * 把验证码那一栏亮出来，手机号和密码原样留着 ——
         * 当成登录失败清空重来的话，用户每次都会走到同一个地方。
         */
        challengeRequired.value = true
        errorMessage.value = error.message
      } else if (challengeRequired.value && error.code === 'BAD_CREDENTIALS') {
        // 已经进到二次验证这一步了，此时的 401 说的是【验证码】不对，不是密码
        verificationError.value = error.message
      } else {
        // BAD_CREDENTIALS / ACCOUNT_DISABLED / OPERATION_LIMITED 都在这里展示后端给的人话。
        // 注意它们里有 401，但拦截器不会把它们当成「掉登录态」，所以能走到这一行。
        errorMessage.value = error.message
      }
      errorTraceId.value = error.traceId
    } else {
      errorMessage.value = '登录失败，请稍后再试'
    }
  } finally {
    // 成功时不复位：跳转前按钮应当一直是 loading，否则会闪一下「可点」
    if (!succeeded.value) {
      submitting.value = false
    }
  }
}
</script>

<template>
  <div class="page">
    <header class="page__head">
      <h1 class="page__title">欢迎回来</h1>
      <p class="page__subtitle">输入手机号和密码，继续参与抽奖</p>
    </header>

    <!--
      用 <form> 包起来是为了拿到两件浏览器免费给的东西：
      密码框回车直接提交，以及移动端键盘把回车键变成「前往」。
      自己监听 keyup.enter 做不到后者。
    -->
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
        v-model="password"
        icon="lock"
        type="password"
        placeholder="密码"
        autocomplete="current-password"
        :error="passwordError"
      />

      <!--
        只在服务端明确要求时才出现。默认就摆在这里的话，
        绝大多数用户会以为每次登录都要验一道码。
      -->
      <Field
        v-if="challengeRequired"
        v-model="verificationCode"
        icon="lock"
        type="tel"
        placeholder="短信验证码"
        autocomplete="one-time-code"
        :maxlength="6"
        :error="verificationError ?? codeSender.error.value"
      >
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

      <p v-if="errorMessage !== ''" class="page__error" role="alert">
        {{ errorMessage }}
        <!-- traceId 一定要露出来：用户截图报障时，凭它一次定位到服务端日志 -->
        <span v-if="errorTraceId !== null" class="page__trace">（编号 {{ errorTraceId }}）</span>
      </p>

      <Button type="submit" :loading="submitting" class="page__submit">登录</Button>

      <div class="page__options">
        <Checkbox v-model="remember" label="记住我" />
        <!--
          忘记密码还没有页面。做成灰色不可点，而不是给一个点了没反应的蓝色链接——
          后者是在骗用户，他会一直点。等找回流程做完再放开
        -->
        <span class="page__forgot" aria-disabled="true">忘记密码？</span>
      </div>
    </form>

    <p class="page__alt">
      还没有账号？<RouterLink class="page__link" :to="{ name: 'register', query: route.query }">
        立即注册
      </RouterLink>
    </p>

    <Result :open="succeeded" text="登录成功" />
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

/* 编号退到最后，但一直在 */
.page__trace {
  color: var(--sv-text-placeholder);
}

.page__submit {
  margin-top: var(--sv-space-sm);
}

.page__options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--sv-space-xs);
}

.page__forgot {
  color: var(--sv-text-placeholder);
  font-size: var(--sv-font-caption);
}

/*
 * 次要出口就是一行小字，钉在页面底部。
 * margin-top: auto 把中间的空白全部留给表单，短屏时它自然上移，不会盖住内容。
 */
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
