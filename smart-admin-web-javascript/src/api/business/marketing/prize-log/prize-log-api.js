/**
 * 资产域-奖励发放执行明细与快照表 api 封装
 *
 * @Author:    weolwo
 * @Date:      2026-04-03 18:42:42
 * @Copyright  weolwo
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const prizeLogApi = {

  /**
   * 分页查询  @author  weolwo
   */
  queryPage : (param) => {
    return postRequest('/prizeLog/queryPage', param);
  },

  /**
   * 增加  @author  weolwo
   */
  add: (param) => {
      return postRequest('/prizeLog/add', param);
  },

  /**
   * 修改  @author  weolwo
   */
  update: (param) => {
      return postRequest('/prizeLog/update', param);
  },


  /**
   * 删除  @author  weolwo
   */
  delete: (id) => {
      return getRequest(`/prizeLog/delete/${id}`);
  },

  /**
   * 批量删除  @author  weolwo
   */
  batchDelete: (idList) => {
      return postRequest('/prizeLog/batchDelete', idList);
  },

};
