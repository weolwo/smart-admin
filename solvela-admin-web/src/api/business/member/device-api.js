/**
 * 设备 api 封装。
 *
 * 设备表是**服务端签发**的，没有新增入口 —— 一台设备的诞生只可能来自
 * 客户端调 /device/register，后台能做的只有查和处置。
 *
 * @Date  2026-09-10
 */
import { getRequest, postRequest } from '/@/lib/axios';

export const deviceApi = {
  /**
   * 分页查询
   */
  queryPage: (param) => {
    return postRequest('/member/device/queryPage', param);
  },

  /**
   * 这台设备碰过哪些账号（最近 50 个）。
   *
   * 整套设备防刷方案最终要产出的就是这张表：一台设备下挂着二十个账号，
   * 和二十台设备各挂一个账号，是完全不同的两件事。
   */
  listMembers: (deviceId) => {
    return getRequest(`/member/device/members/${deviceId}`);
  },

  /**
   * 人工处置：封禁 / 解封 / 推进观察档。
   *
   * ⚠️ 不要传 operator —— 服务端从当前登录员工取，前端传的会被忽略。
   * 那一列存在的唯一理由是事后追得到人，可被伪造就没有价值。
   */
  dispose: (param) => {
    return postRequest('/member/device/dispose', param);
  },
};
