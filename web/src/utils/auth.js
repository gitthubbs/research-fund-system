const USER_KEY = 'research-fund-user'
const TOKEN_KEY = 'research-fund-token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function getUser() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    clearAuthSession()
    return null
  }
}

export function getRole() {
  return getUser()?.role || ''
}

export function isAuthenticated() {
  return Boolean(getToken() && getUser())
}

export function saveAuthSession(payload) {
  localStorage.setItem(TOKEN_KEY, payload.token)
  localStorage.setItem(USER_KEY, JSON.stringify(payload.user))
}

export function clearAuthSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function requireRelogin() {
  clearAuthSession()
  localStorage.setItem('research-fund-force-login', '1')
}

export function shouldShowReloginNotice() {
  return localStorage.getItem('research-fund-force-login') === '1'
}

export function clearReloginNotice() {
  localStorage.removeItem('research-fund-force-login')
}
