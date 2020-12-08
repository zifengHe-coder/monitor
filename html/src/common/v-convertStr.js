/**
 * 文本特殊格式的显示
 * 使用方法： v-convertStr:[模式名]="<变量名>"
 * 现有转换：
 * timestamp： 
 */

export default {
  install: (Vue) => {
    Vue.directive('convertStr', {
      update: function(el, binding, vnode){
        if(!binding.value){
          el.innerText = '';
          return;
        }
        let result;
        switch(binding.arg){
          case 'timestamp': 
            let date = new Date(binding.value);
            result = `${date.getFullYear()}-${date.getMonth()+1}-${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`;
            break;
          default: 
            result = binding.value;
        }
        el.innerText = result;
      }
    })
  }
}

