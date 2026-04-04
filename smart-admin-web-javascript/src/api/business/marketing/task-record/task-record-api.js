/**
 * 运行时-会员任务进度实例表 api 封装
 *
 * @Author:    weolwo
 * @Date:      2026-04-03 17:03:33
 * @Copyright  weolwo
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const taskRecordApi = {

  /**
   * 分页查询  @author  weolwo
   */
  queryPage : (param) => {
    return postRequest('/taskRecord/queryPage', param);
  },

  /**
   * 增加  @author  weolwo
   */
  add: (param) => {
      return postRequest('/taskRecord/add', param);
  },

  /**
   * 修改  @author  weolwo
   */
  update: (param) => {
      return postRequest('/taskRecord/update', param);
  },


  /**
   * 删除  @author  weolwo
   */
  delete: (id) => {
      return getRequest(`/taskRecord/delete/${id}`);
  },

  /**
   * 批量删除  @author  weolwo
   */
  batchDelete: (idList) => {
      return postRequest('/taskRecord/batchDelete', idList);
  },

};
