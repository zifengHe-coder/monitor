/**
 * 文本特殊格式的显示
 * 使用方法： v-delimitStr:[模式名]="<变量名>"
 * 配置模式的属性： 
 *    digits： 每几位进行一次分隔
 *    direction： 处理方向，从字符串左边到右边，设为'right'，否则为'left'
 *    delimiter： 分隔符
 */

//分隔模式
const delimitModelObj = {
  amount: {
    digits: 3,
    direction: 'left',
    delimiter: ','
  },//从最右侧开始，每3位用“，”进行分隔
  binary: {
    digits: 2,
    direction: 'right',
    delimiter: ' '
  },//从最左侧开始，每2位用“ ”进行分隔
  cardNumber: {
    digits: 4,
    direction: 'left',
    delimiter: ' '
  },//从最右侧开始，每4位用“ ”进行分隔
}


export default {
  install: (Vue) => {
    Vue.directive('delimitStr', {
      bind: function(el, binding, vnode){
        let delimitModel = delimitModelObj[binding.arg];
        let resultArr = [];
        for(let i=0; i < binding.value.length; ){
          if(i === 0 && delimitModel.direction === 'left'){
            let extra = binding.value.length % delimitModelObj[binding.arg]['digits'];
            if(extra === 0) 
              extra = delimitModelObj[binding.arg]['digits'];
            resultArr.push(binding.value.substring(0, extra));
            i += extra;
          }else{
            resultArr.push(binding.value.substring(i, i + delimitModelObj[binding.arg]['digits']));
            i += delimitModelObj[binding.arg]['digits'];
          }
        }
        el.innerText = resultArr.join(delimitModelObj[binding.arg]['delimiter']);
      }
    })
  }
}

