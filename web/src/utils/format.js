export function formatCurrency(value) {
  const amount = Number(value || 0)
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount).replace('CN¥', '¥')
}

export function formatPercent(value) {
  return `${Number(value || 0).toFixed(1)}%`
}

export function roleLabel(role) {
  return role === 'admin' ? '管理员' : '科研人员'
}

export function roleType(role) {
  return role === 'admin' ? 'danger' : 'success'
}
