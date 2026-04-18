/**
 * 业务级-任务阶段与奖励包映射表 api 封装
 *
 * @Author:    weolwo
 * @Date:      2026-04-03 17:01:05
 * @Copyright  weolwo
 */
import { postRequest, getRequest } from '/src/lib/axios';

export const taskPrizeMappingApi = {
  /**
   * 分页查询  @author  weolwo
   */
  queryPage: (param) => {
    return postRequest('/taskPrizeMapping/queryPage', param);
  },

  /**
   * 增加  @author  weolwo
   */
  add: (param) => {
    return postRequest('/taskPrizeMapping/add', param);
  },

  /**
   * 修改  @author  weolwo
   */
  update: (param) => {
    return postRequest('/taskPrizeMapping/update', param);
  },

  /**
   * 删除  @author  weolwo
   */
  delete: (id) => {
    return getRequest(`/taskPrizeMapping/delete/${id}`);
  },

  /**
   * 批量删除  @author  weolwo
   */
  batchDelete: (idList) => {
    return postRequest('/taskPrizeMapping/batchDelete', idList);
  },
};
