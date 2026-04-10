// api/expenditureApi.js
import request from './request'

export const expenditureApi = {
  // ★ 修改
  getByProject: (projectId) => request.get(`/expenditures/list/project/${projectId}`),

  // ★ 新增
  search: (params) => request.get('/expenditures/search', { params }),

  // ★ 修改
  add: (data) => request.post('/expenditures/create', data),

  // ★ 修改
  delete: (id) => request.delete(`/expenditures/delete/${id}`)
}
