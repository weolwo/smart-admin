/**
 * 资产域-统一发奖提案控制表 api 封装
 *
 * @Author:    weolwo
 * @Date:      2026-04-03 18:46:32
 * @Copyright  weolwo
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const promotionProposalApi = {

  /**
   * 分页查询  @author  weolwo
   */
  queryPage : (param) => {
    return postRequest('/promotionProposal/queryPage', param);
  },

  /**
   * 增加  @author  weolwo
   */
  add: (param) => {
      return postRequest('/promotionProposal/add', param);
  },

  /**
   * 修改  @author  weolwo
   */
  update: (param) => {
      return postRequest('/promotionProposal/update', param);
  },


  /**
   * 删除  @author  weolwo
   */
  delete: (id) => {
      return getRequest(`/promotionProposal/delete/${id}`);
  },

  /**
   * 批量删除  @author  weolwo
   */
  batchDelete: (idList) => {
      return postRequest('/promotionProposal/batchDelete', idList);
  },

};
