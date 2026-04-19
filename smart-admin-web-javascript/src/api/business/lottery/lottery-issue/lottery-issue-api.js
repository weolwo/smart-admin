/**
 * 期号配置 api 封装
 *
 * @Author:    weolwo
 * @Date:      2026-04-19 11:23:43
 * @Copyright  weolwo
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const lotteryIssueApi = {

  /**
   * 分页查询  @author  weolwo
   */
  queryPage : (param) => {
    return postRequest('/lotteryIssue/queryPage', param);
  },

  /**
   * 增加  @author  weolwo
   */
  add: (param) => {
      return postRequest('/lotteryIssue/add', param);
  },

  /**
   * 修改  @author  weolwo
   */
  update: (param) => {
      return postRequest('/lotteryIssue/update', param);
  },


  /**
   * 删除  @author  weolwo
   */
  delete: (id) => {
      return getRequest(`/lotteryIssue/delete/${id}`);
  },

  /**
   * 批量删除  @author  weolwo
   */
  batchDelete: (idList) => {
      return postRequest('/lotteryIssue/batchDelete', idList);
  },

};
