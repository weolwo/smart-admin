/**
 * 业务级-奖励包(大礼包)外壳表 api 封装
 *
 * @Author:    weolwo
 * @Date:      2026-04-03 18:41:40
 * @Copyright  weolwo
 */
import { postRequest, getRequest } from '/src/lib/axios';

export const prizeGroupApi = {
  /**
   * 分页查询  @author  weolwo
   */
  queryPage: (param) => {
    return postRequest('/prizeGroup/queryPage', param);
  },

  /**
   * 增加  @author  weolwo
   */
  add: (param) => {
    return postRequest('/prizeGroup/add', param);
  },

  /**
   * 修改  @author  weolwo
   */
  update: (param) => {
    return postRequest('/prizeGroup/update', param);
  },

  /**
   * 删除  @author  weolwo
   */
  delete: (id) => {
    return getRequest(`/prizeGroup/delete/${id}`);
  },

  /**
   * 批量删除  @author  weolwo
   */
  batchDelete: (idList) => {
    return postRequest('/prizeGroup/batchDelete', idList);
  },
};
