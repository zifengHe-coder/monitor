import Vue from 'vue'
import Router from 'vue-router'
import index from '@/views/index'
import softwareList from '@/views/leftContent/softwareList'
import singleProcess from '@/views/leftContent/singleProcess'
import softwareProcess from '@/views/mainContent/softwareProcess'
import monitoringRecords from '@/views/mainContent/monitoringRecords'
import homePage from '@/views/mainContent/homePage'
import monitoringHistory from '@/views/mainContent/monitoringHistory'
import monitoringList from '@/views/mainContent/monitoringList'

const originalReplace = Router.prototype.replace
originalReplace.replace = function replace(location) {
  return originalReplace.call(this, location).catch(err => err)
}

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      component: index,
      redirect: '/index',
      children: [
        // 主页
        {
          path: 'index',
          name: 'index',
          components: {
            leftContent: softwareList,
            mainContent: homePage
          },
        },
        // 本机进程
        {
          path: 'softwareProcess',
          name: 'softwareProcess',
          components: {
            leftContent: softwareList,
            mainContent: softwareProcess
          }
        },
        // 程序进程 从监听历史跳转
        {
          path: 'programProgress/:programId/:historyId',
          name: 'programProgressFromHistory',
          components: {
            leftContent: singleProcess,
            mainContent: monitoringRecords
          }
        },
        // 程序进程 从本机进程跳转
        {
          path: 'programProgress/:programId',
          name: 'programProgressFromIndex',
          components: {
            leftContent: singleProcess,
            mainContent: monitoringRecords
          }
        },
        // 进程监听历史
        {
          path:'monitoringHistory/:programId',
          name:'monitoringHistory',
          components:{
            leftContent:singleProcess,
            mainContent:monitoringHistory
          }
        },
        // 正在监听的程序
        {
          path: 'monitoringList',
          name: 'monitoringList',
          components:{
            leftContent:softwareList,
            mainContent:monitoringList
          }
        },
      ]
    }
  ]
})
