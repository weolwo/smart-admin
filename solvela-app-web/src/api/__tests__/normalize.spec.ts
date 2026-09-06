import type { AxiosRequestConfig } from 'axios'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { fetchAddresses } from '../address'
import { fetchCategories, fetchCommodities, fetchCommodityDetail, fetchFavorites } from '../mall'
import { fetchPromoRecords } from '../records'
import { fetchTasks } from '../task'

/**
 * 反序列化边界：后端下发的数字型 ID 必须在这里收成字符串。
 *
 * <h3>这条测试对应一个线上事故</h3>
 * 2026-09-05：商品详情页选好规格点兑换，兑换页永远提示「请先回上一页选择规格」。
 *
 * 根因是<b>类型和运行时对不上</b>：`LongJsonSerializer` 对 |v| ≤ 2^53-1 的 Long
 * 下发 JSON <b>数字</b>，于是 `skuId` 在 TypeScript 里是 `Id`（字符串品牌类型）、
 * 运行时却是 `6`。兑换页拿 URL query 里的 `sku`（永远是字符串）去比，
 * `6 === '6'` 恒 false，永远找不到那个 SKU。
 *
 * <p>`types/contract.ts` 里 `toId` 的注释早就写了「所有反序列化入口都必须过这一层」，
 * 但八个 api 模块只有 auth.ts 做了。这个文件把剩下的都钉住。
 *
 * <h3>为什么不在页面级 spec 里测</h3>
 * 那些 spec 整个 mock 掉了 `@/api/mall`，桩数据里的 id 是<b>手写的字符串</b> ——
 * 正好绕过了出问题的那一层。所以这里 mock 的是 http，喂进去的是真实形状。
 */

const request = vi.hoisted(() => vi.fn())

vi.mock('../http', () => ({
  request,
  requestVoid: vi.fn(() => Promise.resolve()),
}))

beforeEach(() => {
  request.mockReset()
})

/** 后端真实会下发的形状：小值 Long 是数字 */
function reply(body: unknown): void {
  request.mockImplementation((_config: AxiosRequestConfig) => Promise.resolve(body))
}

describe('ID 归一', () => {
  it('🔴 商品详情：commodityId / categoryId / 嵌套的 skuId 都收成字符串', async () => {
    reply({
      commodityId: 2,
      commodityCode: 'DEMO2AAAA',
      categoryId: 1,
      commodityType: 'PHYSICAL',
      commodityName: 'Nova 16 Pro',
      commodityIntro: null,
      coverUrl: null,
      payType: 2,
      pointsPrice: 45000,
      cashPrice: '299.00',
      originalPrice: '3199.00',
      favorite: false,
      availableStock: 2,
      bannerUrls: [],
      detailContent: null,
      exchangeNotice: null,
      limitPeriod: 'LIFETIME',
      limitCount: 0,
      remainingCount: null,
      // 🔴 嵌套数组：Raw<T> 的映射管不到这一层，normalize 必须手动递归
      skus: [
        {
          skuId: 6,
          skuCode: 'S7DJ1FUUS5',
          skuAttrs: {},
          skuCoverUrl: null,
          pointsPrice: 45000,
          cashPrice: '299.00',
          availableStock: 2,
        },
      ],
    })

    const detail = await fetchCommodityDetail('2' as never)

    expect(detail.commodityId).toBe('2')
    expect(detail.categoryId).toBe('1')
    /*
     * 这一条就是那个事故本身。skuId 留成数字 6 的话，
     * 兑换页 `skus.find((s) => s.skuId === id)` 拿 query 里的 '6' 永远找不到。
     */
    expect(detail.skus[0]?.skuId).toBe('6')
    expect(typeof detail.skus[0]?.skuId).toBe('string')
  })

  it('商品列表：每一项的 id 都收', async () => {
    reply({ list: [{ commodityId: 7001, categoryId: 1, commodityName: 'x' }], total: 1 })
    const page = await fetchCommodities()
    expect(page.list[0]?.commodityId).toBe('7001')
    expect(page.list[0]?.categoryId).toBe('1')
  })

  it('分类：id 与 parentId 都收', async () => {
    reply([{ id: 8, parentId: 0, categoryName: '数码', iconUrl: null, sort: 1 }])
    const list = await fetchCategories()
    expect(list[0]?.id).toBe('8')
    // parentId 是 0 —— 假值，用 `raw.parentId || ...` 这类写法会在这里出错
    expect(list[0]?.parentId).toBe('0')
  })

  it('收藏列表：走的是和商品列表同一个归一函数', async () => {
    reply([{ commodityId: 2, categoryId: 1, commodityName: 'x' }])
    expect((await fetchFavorites())[0]?.commodityId).toBe('2')
  })

  it('🔴 地址簿的字段名是 addressId，不是 id', async () => {
    /*
     * 这条用例第一版把桩写成了 `{ id: 9 }` —— 照着**前端的声明**造数，
     * 而不是照着线上真发的样子。于是它绿着，而线上 address.id 恒为 undefined：
     * 页面上地址显示得好好的，提交时 addressId 却是 null，
     * 后端回「请选择收货地址」。
     *
     * 桩必须照抄线上形状。网关直接下发契约对象 MallAddressView，字段名就是 addressId。
     */
    reply([
      {
        addressId: 9,
        receiverName: '张三',
        receiverPhone: '138****8000',
        province: '广东省',
        city: '深圳市',
        district: '南山区',
        detailAddress: '科技园南路 1 号',
        isDefault: true,
      },
    ])
    const list = await fetchAddresses()
    expect(list[0]?.addressId).toBe('9')
    expect(typeof list[0]?.addressId).toBe('string')
  })

  it('🔴 字段名对不上时当场炸，不静默给 undefined', async () => {
    /*
     * 静默放行才是真正的坑：undefined 会一路飘到某个
     * `a.addressId === picked` 恒 false 的地方，那时已经完全看不出因果。
     * 反序列化这一层是唯一能发现「声明和线上对不上」的地方。
     */
    reply([{ id: 9, receiverName: '张三' }])
    await expect(fetchAddresses()).rejects.toThrow(TypeError)
  })

  it('优惠记录 / 任务：recordId 与 taskId 也收', async () => {
    reply([{ recordId: 9001, title: '新人礼包', status: 'DONE' }])
    expect((await fetchPromoRecords())[0]?.recordId).toBe('9001')

    reply([{ taskId: 51, taskName: '每日签到' }])
    expect((await fetchTasks())[0]?.taskId).toBe('51')
  })

  it('已经是字符串的（超出安全整数范围时后端就这么发）原样通过', async () => {
    reply([
      { id: '9007199254740993', parentId: '0', categoryName: '大 id', iconUrl: null, sort: 1 },
    ])
    expect((await fetchCategories())[0]?.id).toBe('9007199254740993')
  })

  it('🔴 超出 JS 安全整数的数字要炸，不能静默用一个已经丢精度的值', async () => {
    /*
     * 走到这里说明后端本该以字符串下发却发了数字，而精度在 JSON.parse 阶段就没了。
     * 用算出来的值而不是字面量：那个字面量本身就会被 eslint 的
     * no-loss-of-precision 拦下 —— 它说的正是这条测试要表达的事。
     */
    reply([
      { id: Number.MAX_SAFE_INTEGER + 2, parentId: 0, categoryName: 'x', iconUrl: null, sort: 1 },
    ])
    await expect(fetchCategories()).rejects.toThrow(RangeError)
  })
})
