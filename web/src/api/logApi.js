import request from './request'

export const logApi = {
  getList: () => request.get('/logs/list')
}
