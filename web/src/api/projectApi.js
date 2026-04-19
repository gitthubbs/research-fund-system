// api/projectApi.js
import request from './request';

export const projectApi = {
  // 获取项目列表
  getList: () => request.get('/projects/list'),
  
  // 根据ID获取项目详情
  getById: (id) => request.get(`/projects/detail/${id}`),
  
  // 新增项目 (自动关联负责人)
  create: (data) => request.post('/projects', data),
  
  // 新增项目 (手动指定)
  add: (data) => request.post('/projects/create', data),
  
  // 更新项目
  update: (data) => request.put('/projects/update', data),
  
  // 删除项目
  delete: (id) => request.delete(`/projects/delete/${id}`),

  // 提交申请
  submit: (id) => request.put(`/projects/submit/${id}`),

  // 审核项目
  audit: (data) => request.put('/projects/audit', data),

  // 确认预算
  confirmBudget: (id) => request.put(`/projects/confirm-budget/${id}`),

  // 结题验收
  finish: (id) => request.put(`/projects/finish/${id}`)
};