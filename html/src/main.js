// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import store from '@/store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import 'default-passive-events'
import axios from 'axios'
import http from '@/request/http'
import api from '@/request/api'
import components from '@/components/index'
import utils from '@/common/index'

Vue.config.productionTip = false

import '@/assets/iconfont/iconfont.css'
import '@/assets/iconfont/iconfont.js'

Vue.use(ElementUI)
Vue.use(components)
Vue.use(utils)

Vue.prototype.$store = store
Vue.prototype.$axios = axios;
Vue.prototype.$http = http
Vue.prototype.$api = api
Vue.prototype.$utils = utils

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  components: { App },
  template: '<App/>'
})
