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

  // ★ 修改：改为从快照获取全局概览
  getOverview: () => request.get('/statistics/overview'),

  // ★ 修改：改为从快照获取分类占比
  getCategoryShare: () => request.get('/statistics/category-ratios'),

  // ★ 修改：改为从快照获取月度趋势
  getMonthly: () => request.get('/statistics/monthly-trend'),

  // ★ 新增：获取各项目执行率列表
  getProjectExecution: () => request.get('/statistics/project-execution'),

  // ★ 新增：获取助手建议
  getAssistantAdvice: () => request.get('/statistics/assistant-advice'),

  // ★ 新增：标记已读
  markAdviceRead: (businessType) => request.post('/statistics/mark-advice-read', { businessType })
}
