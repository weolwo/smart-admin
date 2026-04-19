/**
 * 抽奖记录 api 封装
 *
 * @Author:    weolwo
 * @Date:      2026-04-19 09:21:26
 * @Copyright  weolwo
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const drawPrizeLogApi = {

  /**
   * 分页查询  @author  weolwo
   */
  queryPage : (param) => {
    return postRequest('/drawPrizeLog/queryPage', param);
  },

  /**
   * 增加  @author  weolwo
   */
  add: (param) => {
      return postRequest('/drawPrizeLog/add', param);
  },

  /**
   * 修改  @author  weolwo
   */
  update: (param) => {
      return postRequest('/drawPrizeLog/update', param);
  },


  /**
   * 删除  @author  weolwo
   */
  delete: (id) => {
      return getRequest(`/drawPrizeLog/delete/${id}`);
  },

  /**
   * 批量删除  @author  weolwo
   */
  batchDelete: (idList) => {
      return postRequest('/drawPrizeLog/batchDelete', idList);
  },

};
