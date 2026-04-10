import request from './request'

export const settingsApi = {
  getWarningThreshold: () => request.get('/settings/warning-threshold'),
  updateWarningThreshold: (value) => request.put('/settings/warning-threshold', { value })
}
