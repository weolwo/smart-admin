/**
 * 奖池奖项映射 api 封装
 *
 * @Author:    weolwo
 * @Date:      2026-04-19 10:07:03
 * @Copyright  weolwo
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const poolPrizeMappingApi = {

  /**
   * 分页查询  @author  weolwo
   */
  queryPage : (param) => {
    return postRequest('/poolPrizeMapping/queryPage', param);
  },

  /**
   * 增加  @author  weolwo
   */
  add: (param) => {
      return postRequest('/poolPrizeMapping/add', param);
  },

  /**
   * 修改  @author  weolwo
   */
  update: (param) => {
      return postRequest('/poolPrizeMapping/update', param);
  },


  /**
   * 删除  @author  weolwo
   */
  delete: (id) => {
      return getRequest(`/poolPrizeMapping/delete/${id}`);
  },

  /**
   * 批量删除  @author  weolwo
   */
  batchDelete: (idList) => {
      return postRequest('/poolPrizeMapping/batchDelete', idList);
  },

};
