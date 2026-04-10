import request from './request'

export const dashboardApi = {
  getSummary: () => request.get('/dashboard/summary')
}
