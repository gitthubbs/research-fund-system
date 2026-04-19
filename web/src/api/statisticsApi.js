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
  
  // ★ 新增：获取全局风险统计
  getGlobalRisk: () => request.get('/statistics/global-risk'),

  // ★ 修改：支持传 researcherId 查看特定人员
  getOverview: (researcherId) => request.get('/statistics/overview', { params: { researcherId } }),
  
  // ★ 修改：支持传 researcherId 查看特定人员
  getCategoryShare: (researcherId) => request.get('/statistics/category-ratios', { params: { researcherId } }),
  
  // ★ 修改：支持传 researcherId 查看特定人员
  getMonthly: (researcherId) => request.get('/statistics/monthly-trend', { params: { researcherId } }),
  
  // ★ 新增：获取各项目执行率列表
  getProjectExecution: (researcherId) => request.get('/statistics/project-execution', { params: { researcherId } }),
  
  // ★ 新增：获取助手建议
  getAssistantAdvice: (researcherId) => request.get('/statistics/assistant-advice', { params: { researcherId } }),

  // ★ 新增：标记已读
  markAdviceRead: (businessType) => request.post('/statistics/mark-advice-read', { businessType })
}
