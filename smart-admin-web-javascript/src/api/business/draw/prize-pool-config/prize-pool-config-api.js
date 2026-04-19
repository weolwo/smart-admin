/**
 * 奖池配置 api 封装
 *
 * @Author:    weolwo
 * @Date:      2026-04-19 09:42:12
 * @Copyright  weolwo
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const prizePoolConfigApi = {

  /**
   * 分页查询  @author  weolwo
   */
  queryPage : (param) => {
    return postRequest('/prizePoolConfig/queryPage', param);
  },

  /**
   * 增加  @author  weolwo
   */
  add: (param) => {
      return postRequest('/prizePoolConfig/add', param);
  },

  /**
   * 修改  @author  weolwo
   */
  update: (param) => {
      return postRequest('/prizePoolConfig/update', param);
  },


  /**
   * 删除  @author  weolwo
   */
  delete: (id) => {
      return getRequest(`/prizePoolConfig/delete/${id}`);
  },

  /**
   * 批量删除  @author  weolwo
   */
  batchDelete: (idList) => {
      return postRequest('/prizePoolConfig/batchDelete', idList);
  },

};
