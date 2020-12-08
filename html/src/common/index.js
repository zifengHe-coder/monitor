import dataArea from './dataArea.js'
import funcData from './funcData.js'
import delimitStr from '@/common/v-delimitStr'
import convertStr from '@/common/v-convertStr'

export default {
  install(Vue){
    Vue.use(delimitStr)
    Vue.use(convertStr)
  },
  dataArea: dataArea.area,
  funcData
}