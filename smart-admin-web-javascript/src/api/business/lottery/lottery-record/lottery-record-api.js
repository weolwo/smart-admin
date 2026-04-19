/**
 * 用户号码记录 api 封装
 *
 * @Author:    weolwo
 * @Date:      2026-04-19 11:57:08
 * @Copyright  weolwo
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const lotteryRecordApi = {

  /**
   * 分页查询  @author  weolwo
   */
  queryPage : (param) => {
    return postRequest('/lotteryRecord/queryPage', param);
  },

  /**
   * 增加  @author  weolwo
   */
  add: (param) => {
      return postRequest('/lotteryRecord/add', param);
  },

  /**
   * 修改  @author  weolwo
   */
  update: (param) => {
      return postRequest('/lotteryRecord/update', param);
  },



};
