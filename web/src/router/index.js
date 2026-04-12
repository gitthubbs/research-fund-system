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
const UsersView = () => import('@/views/admin/UsersView.vue')
const LogsView = () => import('@/views/admin/LogsView.vue')
const WarningSettingsView = () => import('@/views/admin/WarningSettingsView.vue')
const CategoryManage = () => import('@/views/admin/CategoryManageView.vue')
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
        meta: { title: '项目详情', roles: ['admin', 'researcher'], hiddenInMenu: true }
      },
      {
        path: 'projects/add',
        name: 'project-add',
        component: ProjectAdd,
        meta: { title: '新增项目', roles: ['researcher'], hiddenInMenu: true }
      },
      {
        path: 'projects/audit',
        name: 'project-audit',
        component: ProjectAudit,
        meta: { title: '项目审核', roles: ['admin'] }
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
        meta: { title: '支出监控', roles: ['admin', 'researcher'], icon: 'Histogram' }
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
