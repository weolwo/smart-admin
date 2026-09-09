<script setup lang="ts">
/**
 * 我的登录设备。
 *
 * <h3>这一页存在的理由</h3>
 * 用户要能自己回答两个问题：**现在有谁登着我的号**，以及**怎么把他踢掉**。
 * 在它之前，答案分别是「看不到」和「改密码」—— 而改密码会把自己也登出，
 * 代价高到大多数人宁可什么都不做。
 *
 * <h3>只列活着的会话</h3>
 * 历史登录记录是另一件事（后台的登录日志）。混进来的话，用户会看到一堆早已失效的
 * 设备，对着一个点不动的「下线」按钮发愁。
 *
 * <h3>🔴 当前这一台必须标出来</h3>
 * 不标的话，用户很容易把自己这台点下线，然后当场被踢出去 —— 而他会以为是页面出了 bug。
 * 所以当前这一条排最前、带标签、**没有下线按钮**（要退出登录走「我的」页那一项）。
 */
import { onMounted, ref } from 'vue'

import {
  fetchSessions,
  revokeOtherSessions,
  revokeSession,
  type MemberSession,
} from '@/api/session'
import { ApiError } from '@/api/errors'

const sessions = ref<MemberSession[]>([])
const loading = ref(true)
const errorMessage = ref('')
/** 正在下线的那一条，按钮转圈用 */
const revoking = ref<string | null>(null)
const revokingOthers = ref(false)
/** 「下线其它设备」的二次确认。这个操作会把用户的其它设备全部踢掉，不该点一下就执行 */
const confirmingOthers = ref(false)

const DEVICE_LABELS: Record<string, string> = {
  APP: '手机 App',
  H5: '手机浏览器',
  WECHAT: '微信',
  PC: '电脑',
}

/** 老会话可能没有设备端。显示「未知设备」而不是留白 —— 留白像是页面坏了 */
function deviceLabel(session: MemberSession): string {
  if (session.deviceType === null) {
    return '未知设备'
  }
  return DEVICE_LABELS[session.deviceType] ?? session.deviceType
}

/**
 * 登录时间。
 *
 * loginTime 为 0 表示这是一条 2026-09-10 之前签发的旧会话 —— 那时还没记时间。
 * 显示「时间未知」而不是 1970 年，后者会让用户以为数据错乱。
 */
function loginLabel(session: MemberSession): string {
  if (session.loginTime <= 0) {
    return '登录时间未知'
  }
  const d = new Date(session.loginTime)
  const pad = (n: number): string => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function locationLabel(session: MemberSession): string {
  // region 目前服务端永远给 null（见 api/session.ts），所以实际显示的是 IP
  return [session.region, session.ip].filter(Boolean).join(' · ') || '位置未知'
}

async function load(): Promise<void> {
  loading.value = true
  errorMessage.value = ''
  try {
    sessions.value = await fetchSessions()
  } catch (error) {
    errorMessage.value = error instanceof ApiError ? error.message : '加载失败，请稍后再试'
  } finally {
    loading.value = false
  }
}

async function revoke(session: MemberSession): Promise<void> {
  if (revoking.value !== null) {
    return
  }
  revoking.value = session.sessionId
  errorMessage.value = ''
  try {
    await revokeSession(session.sessionId)
    // 重新拉一次而不是本地删掉那一行：期间可能有别的设备登进来，
    // 而这个页面上「列表是不是真的」比「响应快一点」重要
    await load()
  } catch (error) {
    errorMessage.value = error instanceof ApiError ? error.message : '下线失败，请稍后再试'
  } finally {
    revoking.value = null
  }
}

async function revokeOthers(): Promise<void> {
  revokingOthers.value = true
  errorMessage.value = ''
  try {
    await revokeOtherSessions()
    confirmingOthers.value = false
    await load()
  } catch (error) {
    errorMessage.value = error instanceof ApiError ? error.message : '操作失败，请稍后再试'
  } finally {
    revokingOthers.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <NavBar title="登录设备" />

    <div class="page__body">
      <p class="page__intro">这些设备当前登录着你的账号。不认识的，直接让它下线。</p>

      <!--
        加载 / 出错 / 空三态交给 Section —— 尤其是「出错」：
        手写最容易漏的就是它，漏了的表现是后端挂掉时页面永远停在「加载中」。
        它还自带重试按钮，而我原来那版只显示一行红字，用户除了退出去没有别的办法。
      -->
      <Section
        title="当前登录的设备"
        :loading="loading"
        :error="errorMessage === '' ? null : errorMessage"
        :empty="sessions.length === 0"
        empty-text="没有其它设备登录"
        @retry="load"
      >
        <ul class="list">
          <li v-for="session in sessions" :key="session.sessionId" class="row">
            <div class="row__main">
              <div class="row__title">
                {{ deviceLabel(session) }}
                <span v-if="session.current" class="row__badge">本机</span>
              </div>
              <div class="row__sub">{{ locationLabel(session) }}</div>
              <div class="row__sub">{{ loginLabel(session) }}</div>
            </div>

            <!--
              当前这一台【没有】下线按钮。想退出当前设备走「我的」页那一项 ——
              在这里给一个，用户点下去会当场被踢出这个页面，而他多半只是想踢别人
            -->
            <Button
              v-if="!session.current"
              variant="text"
              :loading="revoking === session.sessionId"
              @click="revoke(session)"
            >
              下线
            </Button>
          </li>
        </ul>
      </Section>

      <!--
        只有真的存在其它设备时才出现。只有一台时给一个「下线其它设备」，
        点下去什么都不会发生 —— 那种按钮会让人以为功能坏了
      -->
      <div v-if="sessions.length > 1" class="page__actions">
        <Button v-if="!confirmingOthers" variant="text" @click="confirmingOthers = true">
          下线其它所有设备
        </Button>
        <template v-else>
          <p class="page__confirm">
            除本机外的 {{ sessions.length - 1 }} 台设备都会被登出，你自己不受影响。
          </p>
          <Button :loading="revokingOthers" @click="revokeOthers">确认下线</Button>
          <Button variant="text" @click="confirmingOthers = false">取消</Button>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

.page__body {
  flex: 1;
  padding: var(--sv-space-page);
  padding-bottom: calc(var(--sv-safe-bottom) + var(--sv-space-lg));
}

.page__intro {
  margin: 0 0 var(--sv-space-md);
  color: var(--sv-text-secondary);
  font-size: var(--sv-font-caption);
  line-height: 1.5;
}

.list {
  margin: 0;
  padding: 0;
  list-style: none;
  border-radius: var(--sv-radius-card);
  background: var(--sv-bg-surface);
  overflow: hidden;
}

.row {
  display: flex;
  align-items: center;
  gap: var(--sv-space-md);
  padding: var(--sv-space-md);
}

.row + .row {
  border-top: 1px solid var(--sv-border-color);
}

.row__main {
  flex: 1;
  min-width: 0;
}

.row__title {
  display: flex;
  align-items: center;
  gap: var(--sv-space-xs);
  font-size: var(--sv-font-body);
}

/* 「本机」标签要一眼看见 —— 它是用户敢不敢点下线的全部依据 */
.row__badge {
  padding: 0 6px;
  border-radius: var(--sv-radius-pill);
  background: var(--sv-color-primary);
  color: #fff;
  font-size: var(--sv-font-footnote);
  line-height: 1.6;
}

.row__sub {
  margin-top: 2px;
  color: var(--sv-text-placeholder);
  font-size: var(--sv-font-footnote);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.page__actions {
  margin-top: var(--sv-space-lg);
  display: flex;
  flex-direction: column;
  gap: var(--sv-space-sm);
}

.page__confirm {
  margin: 0;
  color: var(--sv-text-secondary);
  font-size: var(--sv-font-caption);
  line-height: 1.5;
}
</style>
