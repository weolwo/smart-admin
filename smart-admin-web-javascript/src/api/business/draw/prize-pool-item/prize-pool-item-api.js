/**
 * 奖池奖项库 api 封装
 *
 * @Author:    weolwo
 * @Date:      2026-04-19 09:52:45
 * @Copyright  weolwo
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const prizePoolItemApi = {

  /**
   * 分页查询  @author  weolwo
   */
  queryPage : (param) => {
    return postRequest('/prizePoolItem/queryPage', param);
  },

  /**
   * 增加  @author  weolwo
   */
  add: (param) => {
      return postRequest('/prizePoolItem/add', param);
  },

  /**
   * 修改  @author  weolwo
   */
  update: (param) => {
      return postRequest('/prizePoolItem/update', param);
  },


  /**
   * 删除  @author  weolwo
   */
  delete: (id) => {
      return getRequest(`/prizePoolItem/delete/${id}`);
  },

  /**
   * 批量删除  @author  weolwo
   */
  batchDelete: (idList) => {
      return postRequest('/prizePoolItem/batchDelete', idList);
  },

};
