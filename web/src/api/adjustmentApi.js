import request from './request'

export const adjustmentApi = {
  apply(data) {
    return request({
      url: '/adjustments/apply',
      method: 'post',
      data
    })
  },
  
  getPending() {
    return request({
      url: '/adjustments/pending',
      method: 'get'
    })
  },
  
  audit(data) {
    return request({
      url: '/adjustments/audit',
      method: 'post',
      data
    })
  }
}
