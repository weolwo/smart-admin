/**
 * 提案表 api 封装
 *
 * @Author:    weolwo
 * @Date:      2026-04-18 23:13:50
 * @Copyright  weolwo
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const proposalRecordApi = {

  /**
   * 分页查询  @author  weolwo
   */
  queryPage : (param) => {
    return postRequest('/proposalRecord/queryPage', param);
  },

  /**
   * 增加  @author  weolwo
   */
  add: (param) => {
      return postRequest('/proposalRecord/add', param);
  },

  /**
   * 修改  @author  weolwo
   */
  update: (param) => {
      return postRequest('/proposalRecord/update', param);
  },


  /**
   * 删除  @author  weolwo
   */
  delete: (id) => {
      return getRequest(`/proposalRecord/delete/${id}`);
  },

  /**
   * 批量删除  @author  weolwo
   */
  batchDelete: (idList) => {
      return postRequest('/proposalRecord/batchDelete', idList);
  },

};
