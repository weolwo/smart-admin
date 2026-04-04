/**
 * 业务级-发奖规则与奖品明细表 api 封装
 *
 * @Author:    weolwo
 * @Date:      2026-04-03 18:39:36
 * @Copyright  weolwo
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const prizeConfigApi = {

  /**
   * 分页查询  @author  weolwo
   */
  queryPage : (param) => {
    return postRequest('/prizeConfig/queryPage', param);
  },

  /**
   * 增加  @author  weolwo
   */
  add: (param) => {
      return postRequest('/prizeConfig/add', param);
  },

  /**
   * 修改  @author  weolwo
   */
  update: (param) => {
      return postRequest('/prizeConfig/update', param);
  },


  /**
   * 删除  @author  weolwo
   */
  delete: (id) => {
      return getRequest(`/prizeConfig/delete/${id}`);
  },

  /**
   * 批量删除  @author  weolwo
   */
  batchDelete: (idList) => {
      return postRequest('/prizeConfig/batchDelete', idList);
  },

};
