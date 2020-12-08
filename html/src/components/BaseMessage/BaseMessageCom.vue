<template>
  <div class="base-message-com__wrapper">
    <transition name="fade">
      <div
        v-if="show"
        :key="currentNodeIndex"
        class="base-message-com__container"
        :class="[type,`key${currentNodeIndex}`]"
        :style="{ 'animation-duration': duration }"
      >
        <div class="base-message-com__headerIcon">
          <svg class="icon" aria-hidden="true">
            <use :xlink:href="icon"></use>
          </svg>
        </div>
        <div class="base-message-com__content">
          <div class="content-vNode" v-if="vNode" v-html="vNode"></div>
          <span v-else>{{ text }}</span>
        </div>
        <div class="base-message-com__CloseIcon">
          <svg
            v-if="showClose"
            class="close-icon"
            aria-hidden="true"
            @click="closeMessage"
          >
            <use xlink:href="#icon-guanbi1"></use>
          </svg>
        </div>
      </div>
    </transition>
  </div>
</template>
<script>
export default {
  name: "BaseMessageCom",
};
</script>
<style scoped>
.base-message-com__wrapper {
  position: absolute;
  top: -50px;
  left: 43%;
  transform: translate(-50%, 0);
  transition: all 2s linear;
  animation: fromUptoDown 4s;
  animation-fill-mode: forwards;
}
.base-message-com__wrapper .base-message-com__container {
  position: absolute;
  left: 0;
  min-width: 300px;
  padding: 10px 20px;
  line-height: 20px;
  vertical-align: text-top;
  border-radius: 6px;
  letter-spacing: 0.1em;
}
.base-message-com__wrapper .base-message-com__container>div{
  float: left;
}

.base-message-com__wrapper .base-message-com__container .base-message-com__headerIcon{
  min-width: 25px;
}
.base-message-com__wrapper .base-message-com__container .base-message-com__headerIcon .icon{
  margin-top: -3px;
}
.base-message-com__wrapper .base-message-com__container .base-message-com__content{
  max-width: 210px;
  padding-left: 6px;
  font-size: 14px;
}
.base-message-com__wrapper .base-message-com__container .base-message-com__content .content-vNode{
  width: 100%;
  word-break: break-all;
}
.base-message-com__wrapper .base-message-com__container .base-message-com__CloseIcon{
  float: right;
}

.base-message-com__wrapper .base-message-com__container .base-message-com__CloseIcon .close-icon {
  float: right;
  width: 1em;
  height: 1em;
  line-height: 1em;
  margin-top: 3px;
  margin-left: 10px;
  vertical-align: middle;
  overflow: hidden;
}
/* 成功提示 */
.base-message-com__wrapper .success {
  background: #b0c4de;
  color: #4682b4;
  border: 1px solid#4682B4;
}
/* 错误提示 */
.base-message-com__wrapper .error {
  background: #ffc0cb;
  color: #ff4500;
  border: 1px solid Coral;
}
/* 警告提示 */
.base-message-com__wrapper .warning {
  background: #ffdab9;
  color: #ff8c00;
  border: 1px solid #ff8c00;
}
/* 默认 */
.base-message-com__wrapper .default {
  background: #f3f3f3;
  color: #ccc;
  border: 1px solid #ccc;
}

.fade-leave-active {
  animation: leave .6s forwards;
}

/* 从上至下出来 */
@keyframes fromUptoDown {
  0% {
    top: -50px;
    opacity: 0;
  }
  15% {
    top: 25px;
    opacity: 1;
  }
  85% {
    top: 25px;
    opacity: 1;
  }
  100% {
    top: -50px;
    opacity: 0;
  }
}

/* 点击删除离开动画 */
@keyframes leave {
  0% {
    top: 0px;
    opacity: 1;
  }
  100% {
    top: -50px;
    opacity: 0;
  }
}
</style>