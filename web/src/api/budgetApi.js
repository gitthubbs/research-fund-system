// api/budgetApi.js
import request from './request';

export const budgetApi = {
  // 根据项目ID获取预算列表
  getByProject: (projectId) => request.get(`/budgets/list/project/${projectId}`),
  
  // 新增预算
  add: (data) => request.post('/budgets/create', data),
  
  // 更新预算
  update: (data) => request.put('/budgets/update', data),
  
  // 删除预算
  delete: (id) => request.delete(`/budgets/delete/${id}`),

  // ★ 新增：获取可用余额
  getAvailableBalance: (projectId, categoryId) => 
    request.get('/budgets/available', { params: { projectId, categoryId } })
};