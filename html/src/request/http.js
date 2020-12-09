import axios from 'axios'
import store from '@/store'
import router from '@/router/index'
import {
  Notification
} from 'element-ui'

if (process.env.NODE_ENV === 'development') {
  axios.defaults.baseURL = '/api/'
} else if (process.env.NODE_ENV === 'test') {
  axios.defaults.baseURL = '/api/'
} else {
  axios.defaults.baseURL = '/api/'
}

// 创建axios实例
const service = axios.create({
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
  // timeout: 1000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 显示loading
    config.loading && store.dispatch('setLoading', true)
    return config
  },
  error => {
    Notification({
      message: '网络错误!',
      type: 'error'
    })
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 关闭loading
    response.config.loading && store.dispatch('setLoading', false)
    // 对响应数据处理
    const res = response.data
    if (res.code && res.code !== '0') {
      if (res.code === '0003') {
        router.push('/login')
      } else {
        Notification({
          message: res.message,
          type: 'error'
        })
      }
    }
    return res
  },
  error => {
    // 关闭loading
    error.config.loading && store.dispatch('setLoading', false)
    // 对响应错误处理
    return Promise.reject(error)
  }
)

export default service
