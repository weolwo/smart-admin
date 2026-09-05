import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createMemoryHistory, createRouter } from 'vue-router'

import type { CommodityQuery } from '@/api/mall'
import type { Address, AddressInput } from '@/api/address'
import { ADDRESSES } from '@/testing/fixtures'
import { toId, type Id } from '@/types/contract'

import RedeemView from '../RedeemView.vue'

/*
 * 资产接口 2026-09-05 已接通真实后端，jsdom 里发不出去。
 * 本用例验的是兑换页的算账与拦截逻辑，余额是它的<b>输入</b>，
 * 所以在这里把它钉死 —— 比让测试依赖某个桩里的数字更清楚，
 * 也不会因为后端改了样例数据就红。
 */
/**
 * 兑换返回值。桩里固定是 30-已完成，而「商品将寄往」那一行只在
 * 10-待履约 时画 —— 要验它就得能换掉这个返回。
 */
/** 让某条用例把地址请求挂住，用来验「详情先回来、地址还在路上」那一刻 */
const holdAddresses = vi.hoisted(() => ({ value: null as Promise<unknown> | null }))

/** 记下最后一次提交的 payload —— 「地址带没带上」只能从这里看 */
const redeemPayload = vi.hoisted(() => ({
  value: null as { addressId: string | null } | null,
}))

const redeemResult = vi.hoisted(() => ({
  value: null as { orderNo: string; status: number; message: string } | null,
}))

/* mock 工厂里不能写 import() 类型注解（eslint），先在这里起个别名 */
/* eslint-disable-next-line @typescript-eslint/consistent-type-imports */
type MallModule = typeof import('@/api/mall')
/* eslint-disable-next-line @typescript-eslint/consistent-type-imports */
type AddressModule = typeof import('@/api/address')

vi.mock('@/api/assets', () => ({
  fetchAssets: () =>
    Promise.resolve([
      { assetType: 'SCORE', label: '积分', amount: '12345.67', currency: true, frozen: false },
    ]),
}))

/*
 * 商城读路径 2026-09-05 已接通真实后端，jsdom 里发不出去。
 * 样例数据集中在 @/testing/fixtures —— 五个 spec 共用一份，
 * 免得「午夜蓝 38 库存是 8 还是 6」这种细节在文件之间漂。
 */
vi.mock('@/api/mall', async (importOriginal) => {
  const actual = await importOriginal<MallModule>()
  const fixtures = await import('@/testing/fixtures')
  const favorites = new Set(
    fixtures.COMMODITIES.filter((c) => c.favorite).map((c) => c.commodityId),
  )
  return {
    // 纯函数（groupSkuAttributes / findSku / isOptionAvailable / OrderStatus）保持真身
    ...actual,
    fetchCategories: () => Promise.resolve(fixtures.CATEGORIES),
    fetchCommodities: (query: CommodityQuery = {}) => {
      // 服务端筛选在测试里也要成立：断言的是「传了条件就只回那些」
      const list = fixtures.COMMODITIES.filter((c) => {
        if (
          query.categoryId !== null &&
          query.categoryId !== undefined &&
          c.categoryId !== query.categoryId
        )
          return false
        const kw = (query.keyword ?? '').trim()
        return kw === '' || c.commodityName.includes(kw)
      }).map((c) => ({ ...c, favorite: favorites.has(c.commodityId) }))
      return Promise.resolve({ list, total: list.length })
    },
    fetchCommodityDetail: (id: Id) => Promise.resolve(fixtures.detailOf(id)),
    fetchFavorites: () =>
      Promise.resolve(
        fixtures.COMMODITIES.filter((c) => favorites.has(c.commodityId)).map((c) => ({
          ...c,
          favorite: true,
        })),
      ),
    toggleFavorite: (id: Id, on: boolean) => {
      if (on) favorites.add(id)
      else favorites.delete(id)
      return Promise.resolve()
    },
    redeem: (payload: { addressId: string | null }) => {
      redeemPayload.value = payload
      return Promise.resolve(redeemResult.value ?? fixtures.REDEEM_RESULT)
    },
  }
})

/*
 * 地址簿 2026-09-05 已接通真实后端。这里保留一份可变的内存实现 ——
 * 「删除要点两次」那条用例要看到删除<b>确实</b>让列表少一行。
 */
vi.mock('@/api/address', async (importOriginal) => {
  const actual = await importOriginal<AddressModule>()
  const fixtures = await import('@/testing/fixtures')
  let list = [...fixtures.ADDRESSES]
  return {
    ...actual,
    fetchAddresses: () =>
      holdAddresses.value ??
      Promise.resolve([...list].sort((a, b) => Number(b.isDefault) - Number(a.isDefault))),
    fetchAddress: (id: Id) => {
      const found = list.find((a) => a.id === id)
      return found === undefined ? Promise.reject(new Error('不存在')) : Promise.resolve(found)
    },
    createAddress: (input: AddressInput) => {
      const created: Address = {
        ...fixtures.ADDRESSES[0]!,
        ...input,
        id: toId('8100'),
        isDefault: false,
      }
      list = [...list, created]
      return Promise.resolve(created)
    },
    updateAddress: (id: Id, input: AddressInput) => {
      const updated: Address = { ...list.find((a) => a.id === id)!, ...input }
      list = list.map((a) => (a.id === id ? updated : a))
      return Promise.resolve(updated)
    },
    deleteAddress: (id: Id) => {
      list = list.filter((a) => a.id !== id)
      return Promise.resolve()
    },
    setDefaultAddress: (id: Id) => {
      list = list.map((a) => ({ ...a, isDefault: a.id === id }))
      return Promise.resolve()
    },
  }
})

setActivePinia(createPinia())

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/', component: { template: '<div/>' } },
    { path: '/me', name: 'mine', component: { template: '<div/>' } },
    { path: '/address', name: 'address-list', component: { template: '<div/>' } },
    // 页面是下面 mount() 挂的，这条路由只负责提供 params 与 query
    { path: '/redeem/:id', name: 'redeem', component: { template: '<div/>' } },
  ],
})

const global = { plugins: [router] }

/**
 * 等一次微任务队列就够 —— 2026-09-05 起这些接口在测试里是 vi.mock 的，
 * 同步 resolve，不再有当初那个 300~450ms 的桩延迟。
 * 还留一个 0ms 的 setTimeout 是因为 useAsync 里那条链有一层 await。
 */
async function settle(): Promise<void> {
  await new Promise((r) => setTimeout(r, 0))
  await flushPromises()
}

async function mountRedeem(path: string) {
  await router.push(path)
  await router.isReady()
  const w = mount(RedeemView, { global })
  await settle()
  return w
}

/**
 * 7002：实物、积分 45,000 + 现金 ¥299、两组规格。
 * SKU 80021 = 午夜蓝 38，库存 8。桩里积分余额是 12,345.67。
 */
const FULL = '/redeem/7002?sku=80021&qty=1'

describe('RedeemView', () => {
  /*
   * 每条用例前复位这两个开关。少了这一步，一条用例失败（没走到收尾那行）
   * 就会把「地址永远加载中」漏给后面所有用例 —— 一处红变成一片红，
   * 而真正坏掉的那条淹没在里面。
   */
  beforeEach(() => {
    holdAddresses.value = null
    redeemResult.value = null
    redeemPayload.value = null
  })

  it('把 query 里的 SKU 还原成可读摘要，并显示件数', async () => {
    const w = await mountRedeem(FULL)
    const html = w.html()
    expect(html).toContain('样例·Apple Watch Series 6')
    expect(html).toContain('颜色 午夜蓝 · 尺码 38')
    expect(html).toContain('× 1')
    // 账单按件数算：45,000 × 1 积分 + ¥299.00 × 1
    expect(html).toContain('45,000 积分')
    expect(html).toContain('¥299.00')
  })

  it('件数按库存与限兑收窄 —— query 里写 999 也不算数', async () => {
    // 午夜蓝38 库存 8，但每日限兑还剩 2
    const w = await mountRedeem('/redeem/7002?sku=80021&qty=999')
    expect(w.html()).toContain('× 2')
    expect(w.html()).toContain('90,000 积分')
  })

  it('query 里的 SKU 是假的或已无货，就当没选并拦住兑换', async () => {
    // 80024 = 曜石黑 42，库存 0
    const w = await mountRedeem('/redeem/7002?sku=80024&qty=1')
    expect(w.find('.bar__hint').text()).toContain('请先回上一页选择规格')
    expect(w.find('.sv-btn').attributes('disabled')).toBeDefined()

    const bogus = await mountRedeem('/redeem/7002?sku=NOPE&qty=1')
    expect(bogus.find('.bar__hint').text()).toContain('请先回上一页选择规格')
  })

  it('积分不够时说清还差多少', async () => {
    const w = await mountRedeem(FULL)
    // 余额 12,345.67，要 45,000 —— 差 32,654.33，取整显示 32,654
    expect(w.find('.bar__hint').text()).toContain('积分不足，还差 32,654 积分')
    expect(w.find('.sv-btn').attributes('disabled')).toBeDefined()
  })

  it('实物带默认地址，虚拟商品根本没有地址那一段', async () => {
    const physical = await mountRedeem(FULL)
    expect(physical.html()).toContain('收货地址')
    // fetchAddresses 把默认地址排在最前，页面取第 0 条
    expect(physical.html()).toContain('张三')
    expect(physical.html()).toContain('138****8000')

    // 7008 是 COUPON，无规格商品的 SKU id 是 '9' + commodityId
    const virtual = await mountRedeem('/redeem/7008?sku=97008&qty=1')
    expect(virtual.html()).not.toContain('收货地址')
    expect(virtual.html()).toContain('1,000 积分')
  })

  it('🔴 兑券的成功页不许出现收货地址 —— 券不走快递', async () => {
    redeemResult.value = { orderNo: 'DEMO1', status: 10, message: '兑换成功' }
    // 7008 是 COUPON，1000 积分，余额够
    const w = await mountRedeem('/redeem/7008?sku=97008&qty=1')
    await w.find('.sv-btn').trigger('click')
    await settle()

    /*
     * address 原先不看商品类型，照样算出用户的默认地址，于是兑一张券
     * 也显示「商品将寄往：××」—— 用户以为一张券要寄快递。
     * 提交的 addressId 另有 needsAddress 把关，所以是纯展示问题，
     * 但展示错了一样是错。
     */
    const text = w.text()
    expect(text).not.toContain('寄往')
    expect(text).not.toContain('张三')
    // 该说的是「去哪看」，不是留一行「—」
    expect(text).toContain('兑换记录')
    redeemResult.value = null
  })

  it('🔴 地址还在路上时说「加载中」，不是「请选择收货地址」', async () => {
    /*
     * 详情和地址是两个并行请求，详情先回来是常事。那一刻 address 还是 null，
     * 页面却对着一个有默认地址的账号说「请选择收货地址」——
     * 用户看到的就是「明明有地址，却一直让我选」。
     *
     * 拦是对的（不能让 null 地址溜出去），但话说错了。
     */
    let release!: (v: Address[]) => void
    holdAddresses.value = new Promise<Address[]>((r) => {
      release = r
    })

    await router.push('/redeem/7005?sku=97005&qty=1')
    await router.isReady()
    const w = mount(RedeemView, { global })
    await settle()

    expect(w.find('.sv-btn').attributes('disabled')).toBeDefined()
    expect(w.find('.bar__hint').text()).toContain('加载中')
    expect(w.find('.bar__hint').text()).not.toContain('请选择收货地址')

    release(ADDRESSES)
    holdAddresses.value = null
    await settle()
    expect(w.find('.sv-btn').attributes('disabled')).toBeUndefined()
  })

  it('🔴 有地址就带上，不看商品类型 —— 少带的代价远大于多带', async () => {
    /*
     * 这一条对着一个线上现象：兑手机时 payload 里 addressId 是 null，
     * 后端回「请选择收货地址」，而页面上地址那一栏明明显示着地址。
     *
     * 根子上是失败方向指错了：曾经 address 会先判一次 commodityType，
     * 于是 commodityType 只要有任何一刻不是预期值，实物兑换就整个失效。
     * 而反过来「给券多带一个地址」是无害的 —— 后端只在实物分支读它。
     */
    const physical = await mountRedeem('/redeem/7005?sku=97005&qty=1')
    await physical.find('.sv-btn').trigger('click')
    await settle()
    expect(redeemPayload.value?.addressId).toBe('8001')

    // 券也带上，无害：后端不读，订单的 address_id 照样是 NULL
    const coupon = await mountRedeem('/redeem/7008?sku=97008&qty=1')
    await coupon.find('.sv-btn').trigger('click')
    await settle()
    expect(redeemPayload.value?.addressId).toBe('8001')
  })

  it('实物的成功页要说清寄到哪', async () => {
    redeemResult.value = { orderNo: 'DEMO2', status: 10, message: '兑换成功' }
    // 7005 是 PHYSICAL，8900 积分，余额 12345 够
    const w = await mountRedeem('/redeem/7005?sku=97005&qty=1')
    await w.find('.sv-btn').trigger('click')
    await settle()
    expect(w.text()).toContain('寄往')
    redeemResult.value = null
  })

  it('🔴 挑过地址之后还认得出来 —— query 是字符串，而 id 曾经是数字', async () => {
    /*
     * 地址簿把 address=8001 塞进 query，而后端的 Long 小值下发的是 JSON 数字。
     * 归一少做一步，`8001 === '8001'` 恒 false：挑过一次地址之后页面永远找不到它，
     * 于是一直提示「请选择收货地址」，而用户明明选了。
     * api/address.ts 的反序列化边界把 id 收成字符串，这条盯着它。
     */
    const w = await mountRedeem('/redeem/7005?sku=97005&qty=1&address=8001')
    expect(w.html()).toContain('张三')
    // 拦不住是重点：不是「没选地址」，而是选了却认不出来
    expect(w.text()).not.toContain('请选择收货地址')
    expect(w.find('.sv-btn').attributes('disabled')).toBeUndefined()
  })

  it('虚拟商品积分够，能兑，兑完显示订单号与到账', async () => {
    const w = await mountRedeem('/redeem/7008?sku=97008&qty=1')
    expect(w.find('.bar__hint').exists()).toBe(false)
    expect(w.find('.sv-btn').attributes('disabled')).toBeUndefined()

    await w.find('.sv-btn').trigger('click')
    await settle()
    expect(w.find('.done__title').text()).toBe('兑换成功，权益已到账')
    expect(w.find('.done__no').text()).toContain('DEMO')
  })

  it('去地址簿挑地址时，商品与 SKU、件数都带过去', async () => {
    const w = await mountRedeem(FULL)
    await w.find('.addr').trigger('click')
    await flushPromises()
    const query = router.currentRoute.value.query
    expect(router.currentRoute.value.name).toBe('address-list')
    expect(query.pick).toBe('1')
    expect(query.commodity).toBe('7002')
    // 选好的 SKU 与件数不能在挑地址的路上丢掉
    expect(query.sku).toBe('80021')
    expect(query.qty).toBe('1')
  })
})
