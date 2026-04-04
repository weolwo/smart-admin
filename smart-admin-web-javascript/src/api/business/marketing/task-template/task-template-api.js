/**
 * 系统级-任务模板表 api 封装
 *
 * @Author:    weolwo
 * @Date:      2026-04-03 17:07:43
 * @Copyright  weolwo
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const taskTemplateApi = {

  /**
   * 分页查询  @author  weolwo
   */
  queryPage : (param) => {
    return postRequest('/taskTemplate/queryPage', param);
  },

  /**
   * 增加  @author  weolwo
   */
  add: (param) => {
      return postRequest('/taskTemplate/add', param);
  },

  /**
   * 修改  @author  weolwo
   */
  update: (param) => {
      return postRequest('/taskTemplate/update', param);
  },


  /**
   * 删除  @author  weolwo
   */
  delete: (id) => {
      return getRequest(`/taskTemplate/delete/${id}`);
  },

  /**
   * 批量删除  @author  weolwo
   */
  batchDelete: (idList) => {
      return postRequest('/taskTemplate/batchDelete', idList);
  },

};
