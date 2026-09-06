/**
 * 提案表 api 封装
 *
 * @Author:    weolwo
 * @Date:      2026-04-18 23:13:50
 * @Copyright  weolwo
 */
import { postRequest, getRequest } from '/src/lib/axios';

export const proposalRecordApi = {

  /**
   * 分页查询  @author  weolwo
   */
  queryPage : (param) => {
    return postRequest('/proposalRecord/queryPage', param);
  },

  /**
   * 提案漏斗：到账率、审批积压、下发卡单、资产/来源分布与流程体检。
   * ⚠️ 服务端刻意忽略 status 筛选项 —— 它是漏斗要拆解的维度，跟着筛会让到账率恒为 100%；
   * assetType / sourceType / 时间范围这些切片维度照常生效  @author  alaric
   */
  funnel: (param) => {
    return postRequest('/proposalRecord/funnel', param);
  },

  /**
   * 审批通过（财务视角：这笔钱该不该出）。
   *
   * 一审通过后落到哪里由 t_promotion_config.review_level 决定：单审直接转 30-待执行
   * 并立即下发，双审转 11-待二审、此刻还不出钱。所以这个按钮点下去到底发不发钱，
   * 看的是配置不是按钮 —— 成功提示因此只说「已通过」，不说「已发放」。
   *
   * 服务端用条件更新（WHERE status = 当前状态）做并发闸门，
   * 两个人同时点，第二个会拿到「该提案已被处理，请刷新后重试」  @author  alaric
   */
  approve: (id, comment) => {
    return getRequest(`/proposalRecord/approve/${id}`, { comment });
  },

  /**
   * 审批驳回：一审/二审都可以驳，落 20-驳回（终态），不再下发。
   *
   * comment 在服务端是可选的，但前端强制填 —— 驳回是「这笔钱不给了」，
   * 事后客诉或审计追过来，t_proposal_record.review_comment 是唯一能回答
   * 「为什么不给」的地方，空着等于没留痕  @author  alaric
   */
  reject: (id, comment) => {
    return getRequest(`/proposalRecord/reject/${id}`, { comment });
  },

  /*
   * 不再封装增删改 —— 后端对应的四个接口已整组移除（v3.69.0）。
   *
   * 提案表是「钱出去的必经之路」，也是账务对账的主线：
   *   · 「新建」会造出 status=0 的提案。正常链路只落 10-待一审 / 30-待执行 / 80-风控拦截，
   *     0 是绕过风控与预算直接插库的产物，不会被任何流程推进，也没人会发现 ——
   *     漏斗里那条「有 N 条提案停在等待中」的告警，来源就是这个按钮；
   *   · delete 是物理删除，而 t_physical_delivery.proposal_id 与
   *     t_member_asset_transaction.biz_ref_id 都指着它，删掉提案，
   *     下游的履约单和资金流水就成了无源之水，出账查不到依据。
   *
   * 提案只能由发奖链路创建，由审批（approve / reject）与下发推进状态。
   */
};
