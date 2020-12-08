<template>
  <div id="app"
    v-loading="requestLoading"
    element-loading-text="拼命加载中"
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.8)">
    <router-view/>
  </div>
</template>

<script>
export default {
  name: 'App',
  created(){
  },
  computed: {
    requestLoading: function(){
      return this.$store.state.moduleLoading.requestLoading
    }
  },
}
</script>
<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
.icon {
  width: 1em;
  height: 1em;
  line-height: 1em;
  /* vertical-align: -0.15em; */
  vertical-align: middle;
  fill: currentColor;
  overflow: hidden;
}
</style>
