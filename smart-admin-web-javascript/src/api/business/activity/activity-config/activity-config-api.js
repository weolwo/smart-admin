/**
 * 活动配置 api 封装
 *
 * @Author:    weolwo
 * @Date:      2026-04-18 19:31:49
 * @Copyright  weolwo
 */
import { postRequest, getRequest } from '/src/lib/axios';

export const activityConfigApi = {
  /**
   * 分页查询  @author  weolwo
   */
  queryPage: (param) => {
    return postRequest('/activityConfig/queryPage', param);
  },

  /**
   * 增加  @author  weolwo
   */
  add: (param) => {
    return postRequest('/activityConfig/add', param);
  },

  /**
   * 修改  @author  weolwo
   */
  update: (param) => {
    return postRequest('/activityConfig/update', param);
  },

  /**
   * 删除  @author  weolwo
   */
  delete: (id) => {
    return getRequest(`/activityConfig/delete/${id}`);
  },

  /**
   * 批量删除  @author  weolwo
   */
  batchDelete: (idList) => {
    return postRequest('/activityConfig/batchDelete', idList);
  },
};
