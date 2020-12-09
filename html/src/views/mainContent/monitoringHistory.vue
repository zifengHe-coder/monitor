<template>
  <!-- 监听历史 -->
  <div id="monitoringHistory">
    <el-container>
      <el-header height='90px'>
        <el-form :inline="true" :model="search" size='mini'>
          <el-form-item label="进程pid:">
            <el-input v-model="search.keyword" placeholder="请输入内容"></el-input>
          </el-form-item>
          <el-form-item label="操作时间:">
            <el-date-picker
              v-model="search.dateTime"
              type="datetimerange"
              range-separator="-"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              :default-time="['00:00:00', '23:59:59']">
            </el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchHistoryList({data:{},pageNo:0})">查询</el-button>
            <el-button type="primary" plain @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-header>
      <el-main>
        <BaseTableCom
          :tableData="historyList"
          :tableHeader="tableLabels"
          :getTableData="searchHistoryList"
          :totalItems="totalItems"
          :comData="comData"
          :operationWidth="100"
          :hasOperationBtn="true">
          <template v-slot:operationBtn="detail">
            <div> 
              <el-button size='mini' type="primary" @click="goDetail(detail.scope.row)">查看</el-button>
            </div>
          </template>
        </BaseTableCom>
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  data(){
    return {
      search:{
        keyword:null,
        dateTime:null
      },
      historyList:[],
      comData: {
        id: 'historyList'
      },
      tableLabels: [{
        type:'word',
        prop:'order',
        label:'序号',
        width: '50'
      },{
        type: 'word',
        prop: 'softwareName',
        label: '软件名称'
      },{
        type: 'word',
        prop: 'pids',
        label: 'PID'
      },{
        type: 'word',
        columnOperable: 'none',
        prop: 'startTime',
        label: '开始时间'
      },{
        type: 'word',
        columnOperable: 'none',
        prop: 'endTime',
        label: '结束时间'
      },{
        type: 'word',
        columnOperable: 'none',
        prop: 'status',
        label: '监控状态',
      },{
        type: 'word',
        prop: 'exePath',
        label: '路径'
      }],
      softwareId: null,
      totalItems: 0
    }
  },
  created(){
    if(this.$route.params.programId){
      this.softwareId = this.$route.params.programId
    }
    //在页面加载时读取sessionStorage里的状态信息
    // if (sessionStorage.getItem(`historyProgramDetail${this.$route.params.programId}`)) {
    //     this.$store.dispatch('replaceState',JSON.parse(sessionStorage.getItem(`historyProgramDetail${this.$route.params.programId}`)))
    // } 

    // //在页面刷新时将vuex里的信息保存到sessionStorage里
    // window.addEventListener("beforeunload",e => this.beforeunloadFun(e))
  },
  // destroyed(){
  //   window.removeEventListener('beforeunload', e => this.beforeunloadFun(e))
  // },
  methods:{
    // 刷新事件
    beforeunloadFun(e){
      sessionStorage.setItem(`historyProgramDetail${this.$route.params.programId}`,JSON.stringify(this.programData))
    },
    // 搜索记录
    searchHistoryList(params){
      params.pageSize=JSON.parse(sessionStorage.getItem(`${this.comData.id}Page`)).pageSize
      if(this.search.dateTime){
        params.data.startTime=this.$utils.funcData.formatTime(this.search.dateTime[0],'yyyy-MM-dd hh:mm:ss')
        params.data.endTime=this.$utils.funcData.formatTime(this.search.dateTime[1],'yyyy-MM-dd hh:mm:ss')
      }
      params.data.softwareId = this.softwareId;
      this.$http({
        url: this.$api.monitorListTask,
        method: 'POST',
        data: params
      }).then((r) => {
        if (r.code === '0') {
          this.historyList = r.data;
          this.historyList.forEach((item,index)=>{
            item.order=r.pageNo*r.pageSize+index+1
            item.startTime=this.$utils.funcData.formDateGMT(item.startTime)
            item.endTime=this.$utils.funcData.formDateGMT(item.endTime)
            item.status = item.complete? '已完成':'未完成'
          })
          this.totalItems = r.totalItems;
        }
      })
    },
    // 重置查询条件
    reset(){
      this.search ={ 
        keyword:null,
        dataTime:null
      }
      this.searchHistoryList({data:{},pageNo:0})
    },
    // 跳转历史记录详情
    goDetail(data){
      this.$router.push({path: `/programProgress/${this.programData.id}/${data.id}`})
    }
  }
}
</script>

<style lang='less' scoped>
#monitoringHistory{
  width:100%;
  height: calc(~"100% - 45px");;
  .el-container{
    width: 100%;
    height: 100%;
    .el-header{
      padding:52px 32px 0px;
      .el-form{
        /deep/ .el-form-item{
          margin-bottom: 0px;
        }
      }
    }
    .el-main{
      padding:0px 32px;
      height: 100%;
      #baseTableCom{
        height: 100%;
        /deep/ .tableDiv{
          padding-top: 0px;
          height: calc(~"100% - 50px");
          /* .el-table{
            /deep/ td p{
              margin: 0px;
            }
          } */
          // 右边滚动条样式
          .el-table__body-wrapper::-webkit-scrollbar  
          {  
            width: 8px;
            height: 10px;
          }
          /*定义滚动条轨道 内阴影+圆角*/  
          .el-table__body-wrapper::-webkit-scrollbar-track  
          {  
            border-radius: 5px;  
            background-color:#ffffff; 
          } 
          /*定义滑块 内阴影+圆角*/  
          .el-table__body-wrapper::-webkit-scrollbar-thumb  
          {  
            border-radius: 5px;  
            background-color: #d6cfcf;
          }
        }
      }
    }
  }
}
</style>