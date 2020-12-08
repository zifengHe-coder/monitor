/**
 * 自动将 components 文件夹下的含有 "Base" 或者 "The" 的vue文件注册为全局组件
 * components 文件夹下同时满足以下三个条件的组件会被注册为全局组件：
 *    1. 文件名以 “Base” 为前缀 或者 “The”为前缀
 *    2. 文件名以 “vue” 为扩展名
 *    3. excludesComNameArr 中没有该组件名的组件
 */
import upperFirst from 'lodash/upperFirst'
import camelCase from 'lodash/camelCase'

const useVueComNameArr = ['BaseMessage/BaseMessageCom.js'];//存放需要使用vue.use方式注册组件的组件名数组
const excludesComNameArr = ['BaseMessageCom'];//存放不需要注册成全局组件的组件名数组，不需要vue后缀

export default {
    install(Vue){
        /* 注册基础组件 Base[A-Z]\w+\.vue 单例组件 The[A-Z]\w+\.vue */
        let requireComponent=require.context(
            // 其组件目录的相对路径
            './',
            // 是否查询其子目录
            true,
            // 匹配基础组件文件名的正则表达式
            /Base[A-Z]\w+\.vue$|The[A-Z]\w+\.vue$/
        )
        requireComponent.keys().forEach(fileName=>{
            // 获取组件配置
            const componentConfig = requireComponent(fileName)
            // 获取组件的 PascalCase 命名
            const componentName = upperFirst(
                camelCase(
                // 获取和目录深度无关的文件名
                fileName
                    .split('/')
                    .pop()
                    .replace(/\.\w+$/, '')
                )
            )
            if(!excludesComNameArr.includes(componentName)){
              // 全局注册组件
              Vue.component(
                componentName,
                // 如果这个组件选项是通过 `export default` 导出的，
                // 那么就会优先使用 `.default`，
                // 否则回退到使用模块的根。
                componentConfig.default || componentConfig
              )
            }
        })    
      
      //使用vue.use方式注册组件
      useVueComNameArr.forEach(path => {
        import(`@/components/${path}`).then(file => {
          Vue.use(file.default || file)
        })
      })

    }
}