<template>
  <div id="index">
    <el-container class="index">
      <el-aside width="309px" class="leftContent">
        <!-- 最左侧内容展示栏 -->
        <router-view name="leftContent"></router-view>
      </el-aside>
      <el-main class="mainContent">
        <div v-if="$route.name !== 'index'" class="goback">
          <el-button class="goBackBtn" size="mini" @click="goBack(true)">返回首页</el-button>
          <el-button class="goBackBtn" size="mini" @click="goBack()">返回上一页</el-button>
        </div>
        <!-- 主要内容展示栏 -->
        <router-view name="mainContent" :key="$route.path+$route.query.t"></router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  data(){
    return {
    }
  },
  methods: {
    goBack(goIndex=false){
      if(goIndex){
        this.$router.push({path: '/index'})
      }else{
        this.$router.go(-1)
      }
    }
  }
}
</script>

<style lang="less" scoped>
#index{
  width:100vw;
  height: 100vh;
  .index{
    height: 100%;
    width: 100%;
    .leftContent{
      height: 100%;
      background: #181827;
      color: #fff;
      display: inline-block;
    }
    .mainContent{
      height: 100%;
      padding: 0px;
    }
    .goback{
      padding: 10px 0 10px 30px;
      background: #C8CCD5;
      border-bottom: 1px solid #9FA8B4;
      .goBackBtn{
        color: #888;
        border: 1px solid #888;
        height: 24px;
        background: #fff;
        border-radius: 4px;
        outline: none;
        font-size: 12px;
        cursor: pointer;
        padding: 0 8px;
      }
    }

    
    // 左边滚动条样式
    .leftContent::-webkit-scrollbar  
    {  
      width: 8px;
      height: 10px;
    }
    /*定义滚动条轨道 内阴影+圆角*/  
    .leftContent::-webkit-scrollbar-track  
    {  
      border-radius: 5px;  
      background-color:#181827; 
    } 
    /*定义滑块 内阴影+圆角*/  
    .leftContent::-webkit-scrollbar-thumb  
    {  
      border-radius: 5px;  
      background-color: #2d2d3b;
    }
    // 右边滚动条样式
    .mainContent::-webkit-scrollbar  
    {  
      width: 8px;
      height: 10px;
    }
    /*定义滚动条轨道 内阴影+圆角*/  
    .mainContent::-webkit-scrollbar-track  
    {  
      border-radius: 5px;  
      background-color:#ffffff; 
    } 
    /*定义滑块 内阴影+圆角*/  
    .mainContent::-webkit-scrollbar-thumb  
    {  
      border-radius: 5px;  
      background-color: #d6cfcf;
    }
  }
}
</style>
