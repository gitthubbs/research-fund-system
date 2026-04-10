// api/statisticsApi.js
import request from './request'

export const statisticsApi = {
  // ★ 修改
  getExecutionRate: (projectId) => request.get(`/statistics/execution-rate/${projectId}`),

  // ★ 修改
  getExpenditureByCategory: (projectId) => request.get(`/statistics/expenditure/category/${projectId}`),

  // ★ 修改
  getMonthlyTrend: (projectId) => request.get(`/statistics/expenditure/trend/${projectId}`),

  // ★ 修改
  getBudgetByCategory: (projectId) => request.get(`/statistics/budget/category/${projectId}`),

  // ★ 修改
  saveSnapshot: (projectId) => request.post(`/statistics/snapshot/create/${projectId}`),

  // ★ 新增
  getProjectExecution: () => request.get('/statistics/project-execution'),

  // ★ 新增
  getCategoryShare: () => request.get('/statistics/category'),

  // ★ 新增
  getMonthly: () => request.get('/statistics/monthly')
}
