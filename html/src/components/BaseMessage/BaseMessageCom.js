import Vue from 'vue';
import BaseMessageCom from '@/components/BaseMessage/BaseMessageCom.vue';

// 创建组件构造器
const BaseMessageComConstructor = Vue.extend(BaseMessageCom);
let timer = null;

/**
 * 组件说明：
 *    参数：
 *        1、必传参数：
 *            无
 *        2、可选参数：
 *            参数      数据类型  参数说明          默认值
 * 
 *            text      String   提示内容          '这是一个提示'
 *            type      String   主题              'default'
 *            icon      String   左侧图标          '#icon-message'
 *            showClose Boolean  是否显示关闭图标   false
 *            duration  Number   动画时间           4
 * 
 */
function showMessage(options) {
  // 创建组件实例
  const messageDOM = new BaseMessageComConstructor({
    el: document.createElement('div'), // 该div最终会被组件替换
    data() {
      return {
        show: true,
        text: options.text || '这是一个提示',
        type: options.type || 'default',
        icon: options.icon || '#icon-message',
        showClose: options.showClose || false,
        timeout: parseInt(options.duration)*1000 || 4000,
        duration: parseInt(options.duration) + 's' || '4s',
        currentNodeIndex: null, // 当前节点索引
        vNode: options.vNode || null,
      }
    },
    created() {
      this.changeIcon(this.type);
    },
    mounted(){
      this.$nextTick(function(){
        let node = document.querySelectorAll('.base-message-com__wrapper');
        this.currentNodeIndex = node.length - 1;
      })
    },
    methods: {
      changeIcon(type) {
        // 当用户没有使用icon属性，就使用默认的icon
        switch (type) {
          case 'success':
            if(!options.icon){
              this.icon = '#icon-correct';
            }
            break;
          case 'warning':
            if(!options.icon){
              this.icon = '#icon-warning';
            }
            break;
          case 'error':
            if(!options.icon){
              this.icon = '#icon-error';
            }
            break;
        }
      },
      // 手动关闭message提示
      closeMessage() {
        this.show = false
      }
    }
  })
  // 获取已挂载的组件节点
  let comNode = document.querySelectorAll('.base-message-com__wrapper'),
  currentNode = document.querySelector(`.key${comNode.length - 1}`),
  prevComNodeHeight = 0;
  if(currentNode){
    prevComNodeHeight = Number(parseFloat(getComputedStyle(currentNode, null)['height']).toFixed(2));
  }
  // 根据已挂载的组件数量，来设置即将挂载的组件的margin-top
  let comNodeLength = comNode.length;
  if(comNodeLength === 0){
    ++comNodeLength;
  }
  if(comNode.length !== 0){
    messageDOM.$el.style.marginTop = (prevComNodeHeight + 35) * (comNodeLength) + 'px';
  }
  document.body.appendChild(messageDOM.$el);

  clearTimeout(timer)
  timer = setTimeout(()=>{
    // 重新获取已挂载的组件节点
    comNode = document.querySelectorAll('.base-message-com__wrapper')
    comNode.forEach(item => {
      // 删除节点（为了下次组件挂载的时候，组件位置从头开始）
      document.body.removeChild(item)
    })
  },options.duration*1000 || 4000)
}

// 注册方法
function messageRegistry(Vue) {
  Vue.prototype.$selfMessage = showMessage
}

export default messageRegistry;