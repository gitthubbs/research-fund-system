// api/projectApi.js
import request from './request';

export const projectApi = {
  // 获取项目列表
  getList: () => request.get('/projects/list'),
  
  // 根据ID获取项目详情
  getById: (id) => request.get(`/projects/detail/${id}`),
  
  // 新增项目
  add: (data) => request.post('/projects/create', data),
  
  // 更新项目
  update: (data) => request.put('/projects/update', data),
  
  // 删除项目
  delete: (id) => request.delete(`/projects/delete/${id}`)
};