/**
 * 业务级-任务规则配置表 api 封装
 *
 * @Author:    weolwo
 * @Date:      2026-04-03 16:51:54
 * @Copyright  weolwo
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const taskConfigApi = {

  /**
   * 分页查询  @author  weolwo
   */
  queryPage : (param) => {
    return postRequest('/taskConfig/queryPage', param);
  },

  /**
   * 增加  @author  weolwo
   */
  add: (param) => {
      return postRequest('/taskConfig/add', param);
  },

  /**
   * 修改  @author  weolwo
   */
  update: (param) => {
      return postRequest('/taskConfig/update', param);
  },


  /**
   * 删除  @author  weolwo
   */
  delete: (id) => {
      return getRequest(`/taskConfig/delete/${id}`);
  },

  /**
   * 批量删除  @author  weolwo
   */
  batchDelete: (idList) => {
      return postRequest('/taskConfig/batchDelete', idList);
  },

};
