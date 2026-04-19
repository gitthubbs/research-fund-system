// api/expenditureApi.js
import request from './request'

export const expenditureApi = {
  // ★ 修改
  getByProject: (projectId) => request.get(`/expenditures/list/project/${projectId}`),

  // ★ 新增
  search: (params) => request.get('/expenditures/search', { params }),

  // ★ 修改
  add: (data) => request.post('/expenditures/create', data),

  // ★ 新增：提交报销，指向 POST /api/expenditures
  create: (data) => request.post('/expenditures', data),

  // ★ 修改
  delete: (id) => request.delete(`/expenditures/delete/${id}`),

  // ★ 新增：待审核列表
  getPending: () => request.get('/expenditures/pending'),

  // ★ 新增：审核操作
  audit: (data) => request.put('/expenditures/audit', data),

  // ★ 新增：获取用户预警支出
  getAlerts: () => request.get('/expenditures/alerts'),

  // ★ 新增：设为已读
  read: (id) => request.put(`/expenditures/${id}/read`)
}
