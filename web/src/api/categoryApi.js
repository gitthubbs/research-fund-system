// api/categoryApi.js
import request from './request';

export const categoryApi = {
  // 获取分类列表
  getList: () => request.get('/categories/list'),
  
  // 新增分类
  add: (data) => request.post('/categories/create', data),
  
  // 修改分类
  update: (data) => request.put('/categories/update', data),
  
  // 删除分类
  delete: (id) => request.delete(`/categories/delete/${id}`)
};