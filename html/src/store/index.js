import Vue from 'vue'
import Vuex from 'vuex'
import moduleLoading from '@/store/modules/loading'
import softwareDetail from '@/store/modules/softwareDetail'
import softwareProcess from '@/store/modules/softwareProcess'

Vue.use(Vuex)


const store = new Vuex.Store({
  state: {
    testWord: '测试vuex'
  },
  modules: {
    moduleLoading,
    softwareDetail,
    softwareProcess
  }})
export default store