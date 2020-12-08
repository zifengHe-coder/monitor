<template>
  <div id="singleProcess">
    <div class="programDetail">
      <span class="title">{{title}}</span>
      <div style="margin-top: 32px;">
        <div class="detailList">
          <div style="align-items: center;">
            <img :src="detail.iconUrl" class="logo"/>
            <span class="exeName">{{detail.name}}</span>
          </div>
          <div>
            <span class="label">文件夹路径</span>
            <span class="value">{{detail.path | urlHandle}}</span>
          </div>
          <div>
            <span class="label">文件大小</span>
            <span class="value">{{(detail.bytes / (1024 * 1024)).toFixed(2)}}M</span>
          </div>
          <div>
            <span class="label">创建时间</span>
            <span v-convertStr:timestamp="detail.createTime" class="value"></span>
          </div>
        </div>
      </div>
    </div>

    <div class="processList" v-if="processListOnff">
      <p style="margin-top: 0;">相关进程名称</p>
      <div>
        <div v-for="itemInPL in processList" :key="itemInPL.id" class='processItem'>
          <img :src="itemInPL.iconUrl" />
          <span>{{itemInPL.imageName}} ({{itemInPL.pid}})</span>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  data(){
    return {
      detail: {},
      processList: [],
      processListOnff: false,
      title:null
    }
  },
  filters:{
    //把exe路径的\左右添加空格，让字符能换行
    urlHandle(val){
      if(val){
        val = val.replace(/\\/g,' \\ ')
      }
      return val
    }
  },
  created(){
    this.initCom()
  },
  watch: {
    $route(to, from){
      this.initCom()
    }
  },
  methods:{
    initCom(){
      this.$store.dispatch('getSoftwareDetail', +this.$route.params.programId).then((res) => {
        this.detail = res;
        this.$store.dispatch('getProcessList', res).then((res) => {
          this.processList = res;
        });
        if(this.$route.name !== 'monitoringHistory'){
          this.title='程序进程'
        }else{
          this.title='监听历史'
        }
      });
      this.processListOnff = this.$route.path.includes('/programProgress') ? true : false;
    },
    goPage(detail){
      this.$router.replace({ 
        path: `/programProgress/${this.$route.params.programId}/${detail.pid}`
      })
    },
  }
}
</script>
<style lang="less" scoped>
#singleProcess{
  padding-top: 32px;
  .programDetail{
    padding: 0 20px;
    padding-bottom: 32px;
    border-bottom: 1px solid #0c0c15;
    .title{
      font-size: 18px;
      font-weight: 600;
      line-height: 33px;
    }
    .detailList{
      div{
        display: flex;
        align-items: top;
        justify-items: left;
        line-height: 20px;
        .logo{
          width: 56px;
          margin-right: 14px;
          margin-bottom: 12px;
        }
        .exeName{
          color: #ffffff;
          font-size: 14px;
          font-weight: 600;
        }
        .label{
          color: #606079;
          font-size: 12px;
          display: inline-block;
          width: 70px;
        }
        .value{
          color: #ffffff;
          font-size: 12px;
          display: inline-block;
          width: calc(~"100% - 80px");
          word-wrap: break-word;
        }
      }
    }
  }
  .processList{
    border-top: 1px solid #222234;
    padding: 0 20px;
    padding-top: 32px;
    .processItem{
      margin-left: 5px;
      display: flex;
      align-items: center;
      font-size: 12px;
      padding: 10px 0;
      // cursor: pointer;
      img{
        width: 18px;
        margin-right: 10px;
      }
    }
    // .processItem:hover{
    //   color: #f7d666;
    // }
  }
}
</style>