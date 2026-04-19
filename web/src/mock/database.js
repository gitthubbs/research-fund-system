const DB_KEY = 'research-fund-db'

const initialData = {
  categories: [
    { id: 1, categoryName: '设备费' },
    { id: 2, categoryName: '劳务费' },
    { id: 3, categoryName: '差旅费' },
    { id: 4, categoryName: '材料费' }
  ],
  warningThreshold: 90,
  users: [
    { id: 1, username: 'admin', password: '123456', name: '系统管理员', role: 'admin', department: '科研处', phone: '13800000001', email: 'admin@univ.edu' },
    { id: 2, username: 'zhangsan', password: '123456', name: '张三', role: 'researcher', department: '计算机学院', phone: '13800000002', email: 'zhangsan@univ.edu' },
    { id: 3, username: 'lisi', password: '123456', name: '李四', role: 'researcher', department: '材料学院', phone: '13800000003', email: 'lisi@univ.edu' }
  ],
  projects: [
    {
      id: 101,
      projectName: '多模态科研经费分析平台',
      projectCode: 'RF-2026-001',
      principalId: 2,
      startDate: '2026-01-10',
      endDate: '2027-12-31',
      totalBudget: 820000,
      spentAmount: 612000,
      status: '执行中',

      milestones: [
        { id: 1, stage: '立项', date: '2026-01-10', content: '项目完成立项审批并下达首批预算。' },
        { id: 2, stage: '中期检查', date: '2026-08-15', content: '通过中期检查，追加设备采购额度。' },
        { id: 3, stage: '验收', date: '2027-12-20', content: '计划于 2027 年底组织结题验收。' }
      ]
    },
    {
      id: 102,
      projectName: '新材料实验室升级计划',
      projectCode: 'RF-2026-002',
      principalId: 3,
      startDate: '2025-09-01',
      endDate: '2026-10-31',
      totalBudget: 560000,
      spentAmount: 541000,
      status: '临近结题',

      milestones: [
        { id: 1, stage: '立项', date: '2025-09-01', content: '完成实验平台升级立项。' },
        { id: 2, stage: '中期检查', date: '2026-03-20', content: '设备安装完成，进入试运行。' },
        { id: 3, stage: '验收', date: '2026-10-20', content: '准备提交结题与验收材料。' }
      ]
    },
    {
      id: 103,
      projectName: '青年教师创新基金',
      projectCode: 'RF-2026-003',
      principalId: 2,
      startDate: '2026-02-18',
      endDate: '2028-02-18',
      totalBudget: 300000,
      spentAmount: 96000,
      status: '执行中',

      milestones: [
        { id: 1, stage: '立项', date: '2026-02-18', content: '青年基金正式立项。' },
        { id: 2, stage: '中期检查', date: '2027-02-28', content: '计划开展中期检查。' },
        { id: 3, stage: '验收', date: '2028-02-10', content: '计划完成全部结题材料。' }
      ]
    }
  ],
  budgets: [
    { id: 201, projectId: 101, categoryId: 1, budgetAmount: 320000, spentAmount: 285000 },
    { id: 202, projectId: 101, categoryId: 2, budgetAmount: 180000, spentAmount: 166000 },
    { id: 203, projectId: 101, categoryId: 3, budgetAmount: 120000, spentAmount: 109000 },
    { id: 204, projectId: 101, categoryId: 4, budgetAmount: 200000, spentAmount: 52000 },
    { id: 205, projectId: 102, categoryId: 1, budgetAmount: 260000, spentAmount: 255000 },
    { id: 206, projectId: 102, categoryId: 2, budgetAmount: 120000, spentAmount: 118000 },
    { id: 207, projectId: 102, categoryId: 3, budgetAmount: 80000, spentAmount: 84000 },
    { id: 208, projectId: 102, categoryId: 4, budgetAmount: 100000, spentAmount: 84000 },
    { id: 209, projectId: 103, categoryId: 1, budgetAmount: 90000, spentAmount: 15000 },
    { id: 210, projectId: 103, categoryId: 2, budgetAmount: 130000, spentAmount: 52000 },
    { id: 211, projectId: 103, categoryId: 3, budgetAmount: 40000, spentAmount: 11000 },
    { id: 212, projectId: 103, categoryId: 4, budgetAmount: 40000, spentAmount: 18000 }
  ],
  expenditures: [
    { id: 301, projectId: 101, categoryId: 1, amount: 92000, expenditureDate: '2026-01-20', remark: 'GPU 服务器采购' },
    { id: 302, projectId: 101, categoryId: 2, amount: 36000, expenditureDate: '2026-02-12', remark: '研究助理劳务' },
    { id: 303, projectId: 101, categoryId: 3, amount: 18000, expenditureDate: '2026-03-09', remark: '学术会议差旅' },
    { id: 304, projectId: 102, categoryId: 1, amount: 120000, expenditureDate: '2026-01-05', remark: '实验设备更新' },
    { id: 305, projectId: 102, categoryId: 3, amount: 24000, expenditureDate: '2026-02-26', remark: '外场测试差旅' },
    { id: 306, projectId: 103, categoryId: 2, amount: 26000, expenditureDate: '2026-03-15', remark: '课题助研津贴' }
  ],
  logs: [
    { id: 401, time: '2026-04-04 09:12:00', operator: '科研人员张三', module: '报销管理', content: '提交了经费报销申请' },
    { id: 402, time: '2026-04-04 14:30:00', operator: '系统管理员', module: '用户管理', content: '更新了用户李四的角色信息' },
    { id: 403, time: '2026-04-05 08:25:00', operator: '科研人员李四', module: '项目管理', content: '补充了项目中期检查材料' }
  ]
}

function clone(value) {
  return JSON.parse(JSON.stringify(value))
}

export function getDb() {
  const raw = localStorage.getItem(DB_KEY)
  if (raw) {
    return JSON.parse(raw)
  }
  const seeded = clone(initialData)
  localStorage.setItem(DB_KEY, JSON.stringify(seeded))
  return seeded
}

export function setDb(data) {
  localStorage.setItem(DB_KEY, JSON.stringify(data))
}

export function resetDb() {
  setDb(clone(initialData))
}

export function nextId(list) {
  return list.length ? Math.max(...list.map((item) => Number(item.id))) + 1 : 1
}
