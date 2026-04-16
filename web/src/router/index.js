import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/components/common/Layout.vue'
import { getRole, isAuthenticated } from '@/utils/auth'

const LoginView = () => import('@/views/LoginView.vue')
const DashboardView = () => import('@/views/DashboardView.vue')
const ProjectsView = () => import('@/views/ProjectsView.vue')
const ProjectDetailView = () => import('@/views/ProjectDetailView.vue')
const BudgetsView = () => import('@/views/BudgetsView.vue')
const ExpendituresView = () => import('@/views/ExpendituresView.vue')
const ProjectManage = () => import('@/views/admin/ProjectManageView.vue')
const ProjectList = () => import('@/views/researcher/ProjectListView.vue')
const ProjectAdd = () => import('@/views/researcher/ProjectAddView.vue')
const ProjectAudit = () => import('@/views/admin/ProjectAuditView.vue')
const ExpenditureAdd = () => import('@/views/researcher/ExpenditureAddView.vue') // ★ 新增
const ExpenditureAudit = () => import('@/views/admin/ExpenditureAuditView.vue') // ★ 新增
const UsersView = () => import('@/views/admin/UsersView.vue')
const LogsView = () => import('@/views/admin/LogsView.vue')
const WarningSettingsView = () => import('@/views/admin/WarningSettingsView.vue')
const CategoryManage = () => import('@/views/admin/CategoryManageView.vue')
const AdjustmentApply = () => import('@/views/researcher/AdjustmentApplyView.vue') // ★ 新增
const AdjustmentAudit = () => import('@/views/admin/AdjustmentAuditView.vue') // ★ 新增
const ForbiddenView = () => import('@/views/ForbiddenView.vue')
const ProfileView = () => import('@/views/ProfileView.vue')
const NotFoundView = () => import('@/views/NotFoundView.vue')

const routes = [
  { path: '/login', name: 'login', component: LoginView, meta: { title: '登录' } },
  { path: '/403', name: 'forbidden', component: ForbiddenView, meta: { title: '无权限' } },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: DashboardView,
        meta: { title: '工作台', roles: ['admin', 'researcher'], icon: 'DataBoard' }
      },
      {
        path: 'projects',
        name: 'projects',
        component: ProjectsView,
        meta: { title: '项目管理', roles: ['admin', 'researcher'], icon: 'FolderOpened' }
      },
      {
        path: 'projects/:id',
        name: 'project-detail',
        component: ProjectDetailView,
        meta: { title: '项目详情', roles: ['admin', 'researcher'], hiddenInMenu: true, parent: 'projects' } // ★ 修改
      },
      {
        path: 'projects/add',
        name: 'project-add',
        component: ProjectAdd,
        meta: { title: '新增项目', roles: ['researcher'], hiddenInMenu: true, parent: 'projects' } // ★ 修改
      },
      {
        path: 'projects/audit',
        name: 'project-audit',
        component: ProjectAudit,
        meta: { title: '项目审核', roles: ['admin'], parent: 'projects' } // ★ 修改
      },
      {
        path: 'categories',
        name: 'categories',
        component: CategoryManage,
        meta: { title: '科目维护', roles: ['admin'], icon: 'Grid' }
      },
      {
        path: 'budgets',
        name: 'budgets',
        component: BudgetsView,
        meta: { title: '预算管理', roles: ['admin', 'researcher'], icon: 'Money' }
      },
      {
        path: 'expenditures',
        name: 'expenditures',
        component: ExpendituresView,
        meta: { title: '报销记录', roles: ['admin', 'researcher'], icon: 'Histogram' }
      },
      {
        path: 'expenditures/audit',
        name: 'expenditure-audit',
        component: ExpenditureAudit,
        meta: { title: '报销审核', roles: ['admin'], icon: 'Stamp', parent: 'expenditures' } // ★ 修改
      },
      {
        path: 'expenditures/add',
        name: 'expenditure-add',
        component: ExpenditureAdd,
        meta: { title: '报销录入', roles: ['researcher'], icon: 'Edit', parent: 'expenditures' } // ★ 修改
      },
      {
        path: 'warning-settings',
        name: 'warning-settings',
        component: WarningSettingsView,
        meta: { title: '预警配置', roles: ['admin'], icon: 'Warning' }
      },
      {
        path: 'users',
        name: 'users',
        component: UsersView,
        meta: { title: '用户权限', roles: ['admin'], icon: 'UserFilled' }
      },
      {
        path: 'logs',
        name: 'logs',
        component: LogsView,
        meta: { title: '系统日志', roles: ['admin'], icon: 'Document' }
      },
      {
        path: 'profile',
        name: 'profile',
        component: ProfileView,
        meta: { title: '个人中心', roles: ['admin', 'researcher'], icon: 'User' }
      },
      {
        path: 'adjustments/apply',
        name: 'adjustment-apply',
        component: AdjustmentApply,
        meta: { title: '预算调剂', roles: ['researcher'], icon: 'Switch' }
      },
      {
        path: 'adjustments/audit',
        name: 'adjustment-audit',
        component: AdjustmentAudit,
        meta: { title: '调剂审核', roles: ['admin'], icon: 'Stamp' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: NotFoundView,
    meta: { title: '页面不存在' }
  }
]

function getAllowedRoles(to) {
  if (to.meta?.roles) return to.meta.roles
  for (let index = to.matched.length - 1; index >= 0; index -= 1) {
    const record = to.matched[index]
    if (record.meta?.roles) {
      return record.meta.roles
    }
  }
  return null
}

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta?.title ? `${to.meta.title} - 科研经费管理系统` : '科研经费管理系统'

  if (!to.meta?.requiresAuth && !to.matched.some((record) => record.meta.requiresAuth)) {
    if (to.path === '/login' && isAuthenticated()) {
      next('/dashboard')
      return
    }
    next()
    return
  }

  if (!isAuthenticated()) {
    next('/login')
    return
  }

  const allowedRoles = getAllowedRoles(to)
  if (allowedRoles && !allowedRoles.includes(getRole())) {
    next('/403')
    return
  }

  next()
})

export default router
