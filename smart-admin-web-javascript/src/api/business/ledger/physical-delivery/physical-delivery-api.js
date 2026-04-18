/**
 * 资产域-实物发货物流表 api 封装
 *
 * @Author:    weolwo
 * @Date:      2026-04-04 16:12:19
 * @Copyright  weolwo
 */
import { postRequest, getRequest } from '/src/lib/axios';

export const physicalDeliveryApi = {
  /**
   * 分页查询  @author  weolwo
   */
  queryPage: (param) => {
    return postRequest('/physicalDelivery/queryPage', param);
  },

  /**
   * 增加  @author  weolwo
   */
  add: (param) => {
    return postRequest('/physicalDelivery/add', param);
  },

  /**
   * 修改  @author  weolwo
   */
  update: (param) => {
    return postRequest('/physicalDelivery/update', param);
  },

  /**
   * 删除  @author  weolwo
   */
  delete: (id) => {
    return getRequest(`/physicalDelivery/delete/${id}`);
  },

  /**
   * 批量删除  @author  weolwo
   */
  batchDelete: (idList) => {
    return postRequest('/physicalDelivery/batchDelete', idList);
  },
};
