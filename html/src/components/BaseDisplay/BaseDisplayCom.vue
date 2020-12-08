<template>
  <div id="base-display-com__wrapper">
    <div class="base-display-com__header">
      <div class="base-display-com__header-left">
        <svg v-if="showHeadIcon" class="icon" aria-hidden="true">
          <use :xlink:href="titleIcon"></use>
        </svg>
        <div class="title">基本信息</div>
      </div>
      <div class="base-display-com__header-slot">
        <slot name="headerSlot"></slot>
      </div>
      <div v-if="showPackUp" class="base-display-com__header-right" @click="packUp">
        <slot name="pickUp">
          <svg class="icon pack-up" aria-hidden="true">
            <use xlink:href="#icon-xiala"></use>
          </svg>
        </slot>
      </div>
    </div>
    <transition @leave="tableLeave" @enter="tableEnter">
      <div class="base-display-com__body" v-show="showTable">
        <div v-for="(item,index) in listTable" :key="index" class="base-display-com__body-container" :style="{'flex-basis': flexBasis}">
          <div class="container-label">{{item.label}}</div>
          <div class="container-value hover" v-if="showOperator">
            <div class="value-text">{{listData[item.prop]}}</div>
            <transition name="operator">
              <div class="value-operator">
                <div @click="edit" v-show="showEdit">编辑</div>
                <div @click="save" v-show="!showEdit">保存</div>
              </div>
            </transition>
          </div>
          <div class="container-value" v-else>
            <div class="value-text">{{listData[item.prop]}}</div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>
<script>
export default {
  props: {
    listTable: {
      type: Array,
      default: function() {
        return []
      },
      required: true
    },
    listData: {
      type: Object,
      default: function() {
        return {}
      },
      required: true
    },
    showOperator: {
      type: Boolean,
      default: false,
    },
    showHeadIcon: {
      type: Boolean,
      default: true
    },
    titleIcon: {
      type: String,
      default: '#icon-message'
    },
    column: {
      type: Number,
      default: 2
    },
    showPackUp: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      clickTimes: 1,
      showTable: true,
      showEdit: true
    }
  },
  computed: {
    flexBasis(){
      if([2,4].includes(this.column)){
        return (100 / this.column) + '%'
      }else{
        return '50%';
      }
    }
  },
  methods: {
    packUp() {
      let node = document.querySelector('.pack-up');
      if(node){
        let table = document.querySelector('.base-display-com__body');
        let tableHeight = getComputedStyle(table, null)['height'];
        let angle = 180;
        node.style.transform=`rotate(${angle*this.clickTimes}deg)`
        node.style.transformOrigin="center"
        node.style.transition="all .4s ease"
        this.clickTimes += 1;
      }

      this.showTable = !this.showTable
    },
    tableLeave(el, done) {
      let tableHeight = parseFloat(getComputedStyle(el, null)['height'])
      el.style.transform=`translateY(-${tableHeight}px)`
      el.style.opacity=0;
      el.style.transition="all .4s ease"
    },
    tableEnter(el, done){
      let tableHeight = parseFloat(getComputedStyle(el, null)['height'])
      el.style.transform=`translateY(0)`
      el.style.opacity=1;
      el.style.transition="all .4s ease"
    },
    edit(){
      this.showEdit = false;
    },
    save(){
      this.showEdit = true;
    }
  }
}
</script>
<style scoped>
#base-display-com__wrapper{
  width: 100%;
  font-size: 14px;
}
#base-display-com__wrapper .base-display-com__header{
  position: relative;
  z-index: 1000;
  box-sizing: border-box;
  overflow: hidden;
  width: 100%;
  height: 45px;
  padding: 0 15px;
  border: 1px solid #000;
  border-bottom: hidden;
  background: teal;
  color: #fff;
}
#base-display-com__wrapper .base-display-com__header .base-display-com__header-left{
  display: flex;
  align-items: center;
  float:left;
  height: 100%;
  margin-right: 20px;
}
#base-display-com__wrapper .base-display-com__header .base-display-com__header-left .icon{
  margin-right: 8px;
}
#base-display-com__wrapper .base-display-com__header .base-display-com__header-left .title{}
#base-display-com__wrapper .base-display-com__header .base-display-com__header-slot{
  display: flex;
  align-items: center;
  float: left;
  height: 100%;
  cursor: pointer;
}
#base-display-com__wrapper .base-display-com__header .base-display-com__header-slot .button{
  background: lightblue;
  color: #000;
  border-radius: 5px;
  padding: 3px 7px;
}
#base-display-com__wrapper .base-display-com__header .base-display-com__header-right{
  display: flex;
  align-items: center;
  float: right;
  height: 100%;
  cursor: pointer;
  font-size: 16px;
}
#base-display-com__wrapper .base-display-com__body{
  position: relative;
  z-index: 999;
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}
#base-display-com__wrapper .base-display-com__body .base-display-com__body-container{
  display: flex;
  flex-basis: 50%;
  height: 35px;
  line-height: 35px;
}
#base-display-com__wrapper .base-display-com__body .base-display-com__body-container .container-label{
  box-sizing: border-box;
  flex-basis: 21%;
  height: 100%;
  padding: 0 10px;
  border: 1px solid #ccc;
  border-right:hidden;
  border-top: hidden;
  background:		CadetBlue;
}
#base-display-com__wrapper .base-display-com__body .base-display-com__body-container .container-value{
  box-sizing: border-box;
  flex-grow: 7;
  height: 100%;
  padding: 0 10px;
  border: 1px solid #ccc;
  border-top: hidden;

  display: flex;
  justify-content: space-between;
}
#base-display-com__wrapper .base-display-com__body .base-display-com__body-container .hover:hover .value-operator{
display: flex; 
}
#base-display-com__wrapper .base-display-com__body .base-display-com__body-container .hover .value-operator{
  display: none;
  transition: all .3s ease;
}
#base-display-com__wrapper .base-display-com__body .base-display-com__body-container .container-value .value-operator>div{
  margin-right: 5px;
  cursor: pointer;
  color: teal;
}
#base-display-com__wrapper .base-display-com__body .base-display-com__body-container:nth-child(2n) .container-value{
  border-right: 1px solid #ccc;
}

/* vue过度动画 */
.operator-enter-active,
.operator-leave-active{
  transition: all .3s ease;
}
.operator-enter{
  display: flex;
}
.operator-leave-to{
  display: none;
}
</style>