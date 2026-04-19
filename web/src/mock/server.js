import { getDb, nextId, setDb } from '@/mock/database'
import { getToken, getUser, saveAuthSession } from '@/utils/auth'

function ok(data = null, msg = 'success') {
  return { code: 200, msg, data }
}

function fail(msg = '请求失败', code = 400) {
  const error = new Error(msg)
  error.response = { status: code, data: { code, msg } }
  throw error
}

function parseBody(data) {
  if (!data) return {}
  return typeof data === 'string' ? JSON.parse(data) : data
}

function getProjectById(db, id) {
  return db.projects.find((item) => Number(item.id) === Number(id))
}

function enrichProject(db, project) {
  const principal = db.users.find((item) => item.id === project.principalId)
  return {
    ...project,
    principalName: principal?.name || '未知负责人'
  }
}

function enrichBudget(db, budget) {
  const project = getProjectById(db, budget.projectId)
  const category = db.categories.find((item) => item.id === budget.categoryId)
  const executionRate = budget.budgetAmount ? (budget.spentAmount / budget.budgetAmount) * 100 : 0
  return {
    ...budget,
    projectName: project?.projectName || '未知项目',
    categoryName: category?.categoryName || '未知科目',
    executionRate
  }
}

function enrichExpenditure(db, item) {
  const project = getProjectById(db, item.projectId)
  const category = db.categories.find((entry) => entry.id === item.categoryId)
  return {
    ...item,
    projectName: project?.projectName || '未知项目',
    categoryName: category?.categoryName || '未知科目'
  }
}

function buildDashboard(db, role, userId) {
  const projects = role === 'researcher'
    ? db.projects.filter((item) => item.principalId === userId)
    : db.projects
  const budgets = db.budgets.filter((item) => projects.some((project) => project.id === item.projectId))
  const expenditures = db.expenditures.filter((item) => projects.some((project) => project.id === item.projectId))
  const totalBudget = projects.reduce((sum, item) => sum + item.totalBudget, 0)
  const totalSpent = budgets.reduce((sum, item) => sum + item.spentAmount, 0)
  const pendingReimbursements = role === 'researcher' ? 5 : 12
  const warningProjects = budgets.filter((item) => (item.spentAmount / item.budgetAmount) * 100 >= db.warningThreshold)

  const overview = role === 'researcher'
    ? [
        { label: '在研项目数', value: projects.length, suffix: '个' },
        { label: '年度总可用额度', value: totalBudget, type: 'currency' },
        { label: '待处理报销', value: pendingReimbursements, suffix: '笔' },
        { label: '项目预警数', value: new Set(warningProjects.map((item) => item.projectId)).size, suffix: '项' }
      ]
    : [
        { label: '全校总拨款', value: totalBudget, type: 'currency' },
        { label: '经费执行率平均值', value: totalBudget ? (totalSpent / totalBudget) * 100 : 0, type: 'percent' },
        { label: '超支项目汇总', value: new Set(warningProjects.map((item) => item.projectId)).size, suffix: '项' },
        { label: '系统待办任务', value: 9, suffix: '条' }
      ]

  const monthlyTrend = Array.from({ length: 12 }).map((_, index) => ({
    month: `${index + 1}月`,
    amount: Math.round(20000 + (index + 1) * 8500 + (index % 3) * 6500)
  }))

  const categoryShare = db.categories
    .map((category) => {
      const value = budgets
        .filter((item) => item.categoryId === category.id)
        .reduce((sum, item) => sum + item.spentAmount, 0)
      return { name: category.categoryName, value }
    })
    .filter((item) => item.value > 0)

  return { overview, monthlyTrend, categoryShare }
}

function buildStatistics(db, projectId) {
  const project = getProjectById(db, projectId)
  const budgets = db.budgets.filter((item) => Number(item.projectId) === Number(projectId))
  const totalBudget = budgets.reduce((sum, item) => sum + item.budgetAmount, 0)
  const totalExpenditure = budgets.reduce((sum, item) => sum + item.spentAmount, 0)
  return {
    executionRate: {
      rate: totalBudget ? ((totalExpenditure / totalBudget) * 100).toFixed(1) : '0.0',
      totalExpenditure,
      remainingBudget: totalBudget - totalExpenditure
    },
    budgetByCategory: budgets.map((item) => ({
      name: db.categories.find((category) => category.id === item.categoryId)?.categoryName || '未知科目',
      value: item.budgetAmount
    })),
    expenditureByCategory: budgets.map((item) => ({
      name: db.categories.find((category) => category.id === item.categoryId)?.categoryName || '未知科目',
      value: item.spentAmount
    })),
    monthlyTrend: [
      { month: '1月', amount: Math.round(project.spentAmount * 0.08) },
      { month: '2月', amount: Math.round(project.spentAmount * 0.11) },
      { month: '3月', amount: Math.round(project.spentAmount * 0.12) },
      { month: '4月', amount: Math.round(project.spentAmount * 0.13) },
      { month: '5月', amount: Math.round(project.spentAmount * 0.09) },
      { month: '6月', amount: Math.round(project.spentAmount * 0.15) }
    ]
  }
}

export async function handleMockRequest(config) {
  const db = getDb()
  const method = (config.method || 'get').toLowerCase()
  const url = config.url || ''
  const body = parseBody(config.data)

  if (method === 'post' && url === '/auth/login') {
    const user = db.users.find(
      (item) => item.username === body.username && item.password === body.password
    )
    if (!user) fail('用户名或密码错误', 401)
    const payload = {
      token: `mock-token-${user.id}`,
      role: user.role,
      user: {
        id: user.id,
        username: user.username,
        name: user.name,
        role: user.role,
        department: user.department,
        phone: user.phone,
        email: user.email
      }
    }
    saveAuthSession(payload)
    return ok(payload, '登录成功')
  }

  if (method === 'get' && url === '/categories/list') return ok(db.categories)
  if (method === 'get' && url === '/projects/list') return ok(db.projects.map((item) => enrichProject(db, item)))

  if (method === 'get' && /^\/projects\/detail\/\d+$/.test(url)) {
    const id = url.split('/').pop()
    const project = getProjectById(db, id)
    if (!project) fail('项目不存在', 404)
    const budgets = db.budgets.filter((item) => Number(item.projectId) === Number(id)).map((item) => enrichBudget(db, item))
    return ok({ ...enrichProject(db, project), budgets })
  }

  if (method === 'post' && url === '/projects/create') {
    const item = { ...body, id: nextId(db.projects), spentAmount: 0, status: '执行中', milestones: [] }
    db.projects.push(item)
    setDb(db)
    return ok(item, '新增成功')
  }

  if (method === 'put' && url === '/projects/update') {
    const index = db.projects.findIndex((item) => Number(item.id) === Number(body.id))
    if (index < 0) fail('项目不存在', 404)
    db.projects[index] = { ...db.projects[index], ...body }
    setDb(db)
    return ok(db.projects[index], '更新成功')
  }

  if (method === 'delete' && /^\/projects\/delete\/\d+$/.test(url)) {
    const id = Number(url.split('/').pop())
    db.projects = db.projects.filter((item) => Number(item.id) !== id)
    db.budgets = db.budgets.filter((item) => Number(item.projectId) !== id)
    db.expenditures = db.expenditures.filter((item) => Number(item.projectId) !== id)
    setDb(db)
    return ok(null, '删除成功')
  }

  if (method === 'get' && /^\/budgets\/list\/project\/\d+$/.test(url)) {
    const projectId = Number(url.split('/').pop())
    return ok(db.budgets.filter((item) => item.projectId === projectId).map((item) => enrichBudget(db, item)))
  }

  if (method === 'post' && url === '/budgets/create') {
    const item = { ...body, id: nextId(db.budgets) }
    db.budgets.push(item)
    setDb(db)
    return ok(item, '新增成功')
  }

  if (method === 'put' && url === '/budgets/update') {
    const index = db.budgets.findIndex((item) => Number(item.id) === Number(body.id))
    if (index < 0) fail('预算不存在', 404)
    db.budgets[index] = { ...db.budgets[index], ...body }
    setDb(db)
    return ok(db.budgets[index], '更新成功')
  }

  if (method === 'delete' && /^\/budgets\/delete\/\d+$/.test(url)) {
    const id = Number(url.split('/').pop())
    db.budgets = db.budgets.filter((item) => Number(item.id) !== id)
    setDb(db)
    return ok(null, '删除成功')
  }

  if (method === 'get' && /^\/expenditures\/list\/project\/\d+$/.test(url)) {
    const projectId = Number(url.split('/').pop())
    return ok(db.expenditures.filter((item) => item.projectId === projectId).map((item) => enrichExpenditure(db, item)))
  }

  if (method === 'post' && url === '/expenditures/create') {
    const item = { ...body, id: nextId(db.expenditures) }
    db.expenditures.push(item)
    setDb(db)
    return ok(item, '新增成功')
  }

  if (method === 'delete' && /^\/expenditures\/delete\/\d+$/.test(url)) {
    const id = Number(url.split('/').pop())
    db.expenditures = db.expenditures.filter((item) => Number(item.id) !== id)
    setDb(db)
    return ok(null, '删除成功')
  }

  if (method === 'get' && /^\/statistics\/execution-rate\/\d+$/.test(url)) {
    return ok(buildStatistics(db, Number(url.split('/').pop())).executionRate)
  }

  if (method === 'get' && /^\/statistics\/budget\/category\/\d+$/.test(url)) {
    return ok(buildStatistics(db, Number(url.split('/').pop())).budgetByCategory)
  }

  if (method === 'get' && /^\/statistics\/expenditure\/category\/\d+$/.test(url)) {
    return ok(buildStatistics(db, Number(url.split('/').pop())).expenditureByCategory)
  }

  if (method === 'get' && /^\/statistics\/expenditure\/trend\/\d+$/.test(url)) {
    return ok(buildStatistics(db, Number(url.split('/').pop())).monthlyTrend)
  }

  if (method === 'post' && /^\/statistics\/snapshot\/create\/\d+$/.test(url)) {
    return ok(null, '快照保存成功')
  }

  if (method === 'get' && url === '/dashboard/summary') {
    const user = getUser()
    return ok(buildDashboard(db, user?.role || 'researcher', user?.id))
  }

  if (method === 'get' && url === '/users/list') {
    return ok(db.users.map(({ password, ...item }) => item))
  }

  if (method === 'post' && url === '/users/create') {
    const item = { ...body, id: nextId(db.users), password: body.password || '123456' }
    db.users.push(item)
    setDb(db)
    return ok(item, '新增成功')
  }

  if (method === 'put' && url === '/users/update') {
    const index = db.users.findIndex((item) => Number(item.id) === Number(body.id))
    if (index < 0) fail('用户不存在', 404)
    db.users[index] = { ...db.users[index], ...body }
    setDb(db)
    return ok(db.users[index], '更新成功')
  }

  if (method === 'delete' && /^\/users\/delete\/\d+$/.test(url)) {
    const id = Number(url.split('/').pop())
    db.users = db.users.filter((item) => Number(item.id) !== id)
    setDb(db)
    return ok(null, '删除成功')
  }

  if (method === 'put' && /^\/users\/role\/\d+$/.test(url)) {
    const id = Number(url.split('/').pop())
    const target = db.users.find((item) => Number(item.id) === id)
    if (!target) fail('用户不存在', 404)
    target.role = body.role
    setDb(db)
    return ok(null, '角色已更新')
  }

  if (method === 'get' && url === '/logs/list') return ok(db.logs)

  if (method === 'get' && url === '/profile/me') {
    const current = getUser()
    const user = db.users.find((item) => item.id === current?.id)
    if (!user) fail('用户不存在', 404)
    const { password, ...rest } = user
    return ok(rest)
  }

  if (method === 'put' && url === '/profile/update') {
    const current = getUser()
    const index = db.users.findIndex((item) => item.id === current?.id)
    if (index < 0) fail('用户不存在', 404)
    db.users[index] = { ...db.users[index], ...body, password: body.password || db.users[index].password }
    setDb(db)
    saveAuthSession({
      token: getToken(),
      user: {
        id: db.users[index].id,
        username: db.users[index].username,
        name: db.users[index].name,
        role: db.users[index].role,
        department: db.users[index].department,
        phone: db.users[index].phone,
        email: db.users[index].email
      }
    })
    return ok(null, '个人信息已更新')
  }

  if (method === 'get' && url === '/settings/warning-threshold') return ok({ value: db.warningThreshold })

  if (method === 'put' && url === '/settings/warning-threshold') {
    db.warningThreshold = Number(body.value)
    setDb(db)
    return ok({ value: db.warningThreshold }, '阈值已更新')
  }

  fail(`未匹配的接口: ${method.toUpperCase()} ${url}`, 404)
}
