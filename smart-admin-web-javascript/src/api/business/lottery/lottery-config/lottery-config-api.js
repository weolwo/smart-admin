/**
 * 彩票配置 api 封装
 *
 * @Author:    weolwo
 * @Date:      2026-04-19 11:16:39
 * @Copyright  weolwo
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const lotteryConfigApi = {

  /**
   * 分页查询  @author  weolwo
   */
  queryPage : (param) => {
    return postRequest('/lotteryConfig/queryPage', param);
  },

  /**
   * 增加  @author  weolwo
   */
  add: (param) => {
      return postRequest('/lotteryConfig/add', param);
  },

  /**
   * 修改  @author  weolwo
   */
  update: (param) => {
      return postRequest('/lotteryConfig/update', param);
  },


  /**
   * 删除  @author  weolwo
   */
  delete: (id) => {
      return getRequest(`/lotteryConfig/delete/${id}`);
  },

  /**
   * 批量删除  @author  weolwo
   */
  batchDelete: (idList) => {
      return postRequest('/lotteryConfig/batchDelete', idList);
  },

};
