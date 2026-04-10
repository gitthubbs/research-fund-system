import request from './request'

export const profileApi = {
  getMine: () => request.get('/profile/me'),
  update: (data) => request.put('/profile/update', data)
}
