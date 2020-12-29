<template>
  <div style="padding: 20px 20px;height: calc(100% - 100px)">
    <header>监控中程序</header>
    <BaseTableCom
      :tableData="tableData"
      :tableHeader="tableLabels"
      :totalItems="totalItems"
      :comData="comData" 
      :hasOperationBtn="true"
      style="height: 100%;">
        <template v-slot:operationBtn="detail">
          <el-button size="mini" type="primary" @click="checkout(detail.scope.row)"
              >查看</el-button
            >
        </template>
      </BaseTableCom>
  </div>
</template>

<script>
export default {
  data(){
    return{
      tableData: [],
      tableLabels: [
        {
          label: '序号',
          prop: 'sortKey',
          type: 'word'
        },
        {
          label: '名称',
          prop: 'processName',
          type: 'image',
        },
        {
          label: '文件路径',
          prop: 'exePath',
          type: 'word'
        },
        {
          label: '启动命令行',
          prop: 'commandLine',
          type: 'word'
        },
        {
          label: '状态',
          prop: 'status',
          type: 'word'
        }
      ],
      totalItems: 0,
      comData:{
        id: 'monitoringList'
      },
      system: '',
    }
  },
  created(){
    this.getOriginList();
    this.getSystem();
  },
  methods: {
    getOriginList(){
      this.tableData = [];
      for(let k in this.$store.state.softwareProcess.monitoringSoftware){
        this.tableData = this.tableData.concat(this.$store.state.softwareProcess.monitoringSoftware[k])
      }
      this.tableData.forEach((item,index) => {
        item.sortKey = index+1 < 10 ? `0${index+1}` : index+1;
        item.processName = this.system === 'linux' ?  item.exeName : item.exeName + '/exe/' + item.base64Icon;
        item.status = '监控中';
      })
    },
    getSystem(){
      this.system = sessionStorage.getItem('system');
    },
    // 跳到程序详情
    checkout(data){
      this.$router.push({
        path: '/softwareProcess',
        name: 'softwareProcess',
        params:{
          data
        },
        query:{
          t: Date.now()
        }
      })
    },
  }
}
</script>

<style scoped lang="less">
  /deep/ #baseTableCom{
    .tableDiv{
      height: calc(100% - 70px);
    }
  }
</style>