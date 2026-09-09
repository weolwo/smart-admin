/**
 * 设备身份的本地存储。
 *
 * <h3>🔴 它和令牌是两回事，三条规矩都不一样</h3>
 * <table>
 *   <tr><td></td><td>登录令牌</td><td>设备令牌</td></tr>
 *   <tr><td>存哪</td><td>看「记住我」</td><td><b>永远 localStorage</b></td></tr>
 *   <tr><td>退出登录</td><td>清掉</td><td><b>不动</b></td></tr>
 *   <tr><td>过期</td><td>30 天</td><td><b>不过期</b></td></tr>
 * </table>
 *
 * 每一条都有理由：
 * <ul>
 *   <li><b>永远持久化</b>：一个标签页一关就没的设备身份，等于每次访问都是新设备 ——
 *       而防刷的全部意义就是「认出这是同一台」。存 sessionStorage 就是把它作废；</li>
 *   <li><b>退出登录不清</b>：设备是设备，账号是账号。退出时清掉，等于
 *       「换个号登录 = 换一台机器」，而那正是刷子最想要的效果；</li>
 *   <li><b>不设过期</b>：令牌里的时间戳由服务端解释（见 DeviceTokenCodec），
 *       客户端自作主张地判过期只会凭空造出一批新设备。</li>
 * </ul>
 *
 * <h3>它不是身份凭证，泄露了也拿不到账号</h3>
 * 设备令牌只说明「这是哪台机器」，不代表任何一个人。所以它不需要令牌那套
 * 「共用设备上不留痕」的处理 —— 反过来，留痕才是它的用途。
 */

const DEVICE_TOKEN_KEY = 'solvela.app.device.token'
const DEVICE_ID_KEY = 'solvela.app.device.id'

export interface StoredDevice {
  /** 每次请求放进 X-Device-Id 头的那个串，形如 dv_1.xxx.yyy */
  token: string
  /** 32 位 hex。只用于展示与排障，请求头里放的是 token */
  deviceId: string
}

/**
 * 隐私模式 / 禁用站点数据时，访问 storage 会直接抛（不是返回 null）。
 * 与 token-storage 同一个处理：不能让它掀翻整个应用。
 *
 * <p>那种浏览器下设备身份存不住，每次访问都会领一个新的 —— 是**已知且可接受**的：
 * 服务端本来就无从判断「这是不是同一台设备」（见 DeviceController.register 的注释），
 * 兜底靠的是 IP 限频。
 */
function safe<T>(fn: () => T, fallback: T): T {
  try {
    return fn()
  } catch {
    return fallback
  }
}

export function readDevice(): StoredDevice | null {
  const token = safe(() => localStorage.getItem(DEVICE_TOKEN_KEY), null)
  const deviceId = safe(() => localStorage.getItem(DEVICE_ID_KEY), null)
  if (token === null || token === '' || deviceId === null || deviceId === '') {
    return null
  }
  return { token, deviceId }
}

export function writeDevice(token: string, deviceId: string): void {
  safe(() => localStorage.setItem(DEVICE_TOKEN_KEY, token), undefined)
  safe(() => localStorage.setItem(DEVICE_ID_KEY, deviceId), undefined)
}

/**
 * 丢掉本地设备身份。
 *
 * <p>⚠️ **退出登录时不要调它**，见本文件顶部。真正的用途只有一个：
 * 服务端换了签名密钥、旧令牌一律验不过时，让客户端能重新领一个。
 * 目前没有调用点 —— 留着是因为「需要它的时候写不出来」比「多一个未用函数」糟得多。
 */
export function clearDevice(): void {
  safe(() => localStorage.removeItem(DEVICE_TOKEN_KEY), undefined)
  safe(() => localStorage.removeItem(DEVICE_ID_KEY), undefined)
}
