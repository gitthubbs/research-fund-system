import request from './request'

export const userApi = {
  getList: () => request.get('/users/list'),
  add: (data) => request.post('/users/create', data),
  update: (data) => request.put('/users/update', data),
  delete: (id) => request.delete(`/users/delete/${id}`),
  switchRole: (id, role) => request.put(`/users/role/${id}`, { role })
}
