/**
 * 彩票号码池 api 封装
 *
 * @Author:    weolwo
 * @Date:      2026-04-19 11:31:09
 * @Copyright  weolwo
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const lotteryNumberPoolApi = {

  /**
   * 分页查询  @author  weolwo
   */
  queryPage : (param) => {
    return postRequest('/lotteryNumberPool/queryPage', param);
  },

  /**
   * 增加  @author  weolwo
   */
  add: (param) => {
      return postRequest('/lotteryNumberPool/add', param);
  },

  /**
   * 修改  @author  weolwo
   */
  update: (param) => {
      return postRequest('/lotteryNumberPool/update', param);
  },


  /**
   * 删除  @author  weolwo
   */
  delete: (id) => {
      return getRequest(`/lotteryNumberPool/delete/${id}`);
  },

  /**
   * 批量删除  @author  weolwo
   */
  batchDelete: (idList) => {
      return postRequest('/lotteryNumberPool/batchDelete', idList);
  },

};
