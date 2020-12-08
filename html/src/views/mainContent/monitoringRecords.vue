<template>
  <div id="monitoringRecords">
    <div class="progressDetail">
      <div>
        <img v-if="detailOnff" :src="softwareDetail.iconUrl" class="icon"/>
        <span class="name" v-if="detailOnff">{{softwareDetail.name}}</span>
        <span class="status" :style="{color: historyDetail.status === 1 ? '#f7d666' : '#0cab51'}">{{historyDetail.statusWord}}</span>
      </div>

      <div v-if="showMonitorBtn">
        <input type="button" class="monitorBtn" v-if='historyDetail.status === 0' @click="stopMonitor" value="停止监听" />
        <input type="button" class="monitorBtn" v-else @click="starMonitor" value="开始监听" />
      </div>
    </div>
    <div class="softwareDetail">
      <div class="tabs">
        <div v-for="(itemInTL, indexInTL) in tabLabels" :key="indexInTL" class="tab">
          <span v-if="indexInTL !== 0" style="margin: 0 5px;">/</span>
          <span 
            class="tabName" 
            :style="{color: currentTab === itemInTL.value ? '#333' : '#ccc'}" 
            @click="handleTabClick(itemInTL.value)">
            {{itemInTL.label}}
          </span>
        </div>
      </div>
    
      <div v-if="tabContentOnff" class="records">
        <BaseSearchCom 
          :comData="comData"
          :formLabel="searchLabels"
          :getData="getList"
          :labelWidth="labelWidth"
        />
        <BaseTableCom
          :hadIndex='true'
          :tableData="tableData"
          :tableHeader="tableLabels"
          :getTableData="getList"
          :totalItems="totalItems"
          :comData="comData"
          :hasOperationBtn="hasOperation"
          :style="{height: 'calc(100% - 100px)'}"
          :tableStyle="{ height: 'calc(100% - 49px)', 'padding-top': '0'}">
          <template v-slot:operationBtn="data">
            <el-button v-if="showBtn(data.scope.row)" size="mini" type="primary" @click="data.scope.row.readAndWriteType=='write'?downloadChangeFileDetail(data):downloadFile(data)">{{data.scope.row.readAndWriteType=='write'?'写入详情':operationName}}</el-button>
          </template>
        </BaseTableCom>
      </div>
    </div>
  </div>
</template>

<script>
let timer = null;//定时器
export default {
  data(){
    return {
      showMonitorBtn: false,
      index: 1,
      detailOnff: false,
      historyDetail: {},
      comData: {
        id: 'softwareDetail'
      },
      searchLabels: [],
      tableLabels: [],
      tableData: [],
      totalItems: 0,
      hasOperation:null,
      operationName:null,
      dataTypes: [{
        label: '二进制',
        value: 'REG_BINARY'
      },{
        label: 'DWORD',
        value: 'REG_DWORD'
      },{
        label: 'QWORD',
        value: 'REG_QWORD'
      },{
        label: '可扩展字符串',
        value: 'REG_EXPAND_SZ'
      },{
        label: '多字符串',
        value: 'REG_MULTI_SZ'
      },{
        label: '字符串',
        value: 'REG_SZ'
      }],
      tabLabels: [{
        label: '文件读写',
        value: '2'
      },{
        label: '注册表',
        value: '3'
      },{
        label: '进程调用',
        value: '4'
      },{
        label: '设备控制',
        value: '5'
      },{
        label: '网络访问',
        value: '1'
      }],
      currentTab: '2',
      tabContentOnff: false,
      progressDetail: null,
      progressDetailKey: 'progressDetail',
      softwareDetail: {},
      processList: []
    }
  },
  created(){
    this.changeConfig(this.currentTab);
    this.initData();
  },
  watch: {
    "$store.state.progressDetail": {
      handler(to, from){
        this.initData();
      },
      deep: true
    }
  },
  methods:{
    showBtn(row){
      switch(row.mode){
        case 5:
        case 10:
          if(!row.bytes||row.bytes<=0){
            return false
          }
          break;
      }
      return true
    },
    async initData(){
      this.$store.dispatch('getSoftwareDetail', +this.$route.params.programId).then((res) => {
        this.softwareDetail = res;
        this.$store.dispatch('getProcessList', this.softwareDetail).then((res) => {
          this.processList = res;
          if(this.processList.length > 0)
            this.showMonitorBtn = true;
        });
      });
      if(this.$route.name === 'programProgressFromIndex'){
        // detail.pid = +detail.pid;
        // this.progressDetail = detail;
        this.detailOnff = true;
        this.tabContentOnff = true;
      }else{
        this.getHistoryLatestOne().then(res => {
          // if(!this.showMonitorBtn && res.status === 0){
          //   this.changeStateInHistory(res.id);
          // }
        //TODO: 获取历史
          // this.progressDetail = { 
          //   pid: res.pid,
          //   imageName: res.name
          // }
          this.detailOnff = true;
          this.tabContentOnff = true;
        })
      }
      this.updateList();
    },
    changeConfig(type){
      type = Number(type);
      this.comData.id = `softwareDetail_${type}`;
      this.tableData = [];
      switch(type){
        case 1:
          this.searchLabels=[{
            type: 'input',
            prop: 'protocol',
            label: '协议类型'
          },{
            type: 'input',
            prop: 'targetIp',
            label: '目标IP'
          },{
            type: 'input',
            prop: 'sensitiveDataField',
            label: '敏感数据'
          },{
            type: 'datePicker',
            prop: 'operatingTime',
            label: '操作时间',
            itemType: 'datetime',
            format: 'yyyy-MM-dd HH:mm:ss'
          }]
          this.tableLabels=[{
            type: 'timestamp',
            prop: 'timestamp',
            columnOperable: 'none',
            label: '访问时间'
          },{
            type:'word',
            prop:'linkType',
            label:'链接类型'
          },{
            type: 'word',
            prop: 'targetIp',
            label: '目的IP地址'
          },{
            type: 'word',
            prop: 'protocol',
            label: '协议类型'
          },{
            type: 'word',
            prop: 'sensitiveDataField',
            label: '敏感数据字段'
          },{
            type: 'word',
            prop: 'bytes',
            label: '网络流量'
          }]
          this.hasOperation=true
          this.operationName='下载网络包'
          this.labelWidth = 60;
          break;
        case 2:
          this.searchLabels=[{
            type: 'input',
            prop: 'readAndWriteType',
            label: '读写类型'
          },{
            type: 'input',
            prop: 'fileName',
            label: '文件名称'
          },{
            type: 'input',
            prop: 'documentSensitivity',
            label: '文件敏感度'
          },{
            type: 'datePicker',
            prop: 'operatingTime',
            label: '操作时间',
            itemType: 'datetime',
            format: 'yyyy-MM-dd HH:mm:ss'
          }]
          this.tableLabels=[{
            type: 'timestamp',
            prop: 'timeEnd',
            columnOperable: 'none',
            label: '读写时间'
          },{
            type: 'word',
            prop: 'fileName',
            label: '文件名称'
          },{
            type: 'word',
            prop: 'fileLocation',
            label: '文件位置'
          },{
            type: 'word',
            prop: 'readAndWriteType',
            label: '读写类型'
          },{
            type: 'word',
            prop: 'documentSensitivity',
            label: '文件敏感度'
          }]
          this.hasOperation = true;
          this.operationName = '打开文件';
          this.labelWidth = 70;
          break;
        case 3:
          this.searchLabels=[{
            type: 'input',
            prop: 'key',
            label: '目标键'
          },{
            type: 'input',
            prop: 'valueName',
            label: '值键'
          },{
            type: 'select',
            prop: 'valueType',
            label: '值键类型',
            options: this.dataTypes
          },{
            type: 'datePicker',
            prop: 'operatingTime',
            label: '操作时间',
            itemType: 'datetime',
            format: 'yyyy-MM-dd HH:mm:ss'
          }]
          this.tableLabels=[{
            type: 'timestamp',
            prop: 'timeEnd',
            columnOperable: 'none',
            label: '操作时间'
          },{
            type: 'word',
            prop: 'typeDescription',
            label: '操作类型'
          },{
            type: 'word',
            prop: 'key',
            label: '目标键'
          },{
            type: 'word',
            prop: 'valueName',
            label: '值键'
          },{
            type: 'select',
            columnOperable: 'none',
            prop: 'valueType',
            label: '值键类型',
            options: this.dataTypes
          },{
            type: 'word',
            prop: 'data',
            label: '值键值'
          },{
            type: 'select',
            columnOperable: 'none',
            prop: 'oldValueType',
            label: '原有键值类型',
            options: this.dataTypes
          },{
            type: 'word',
            prop: 'oldData',
            label: '原值键值'
          }];
          this.hasOperation = false;
          this.labelWidth = 60;
          break;
        case 4:
          this.searchLabels=[{
            type: 'input',
            prop: 'cmdLine',
            label: '命令行'
          },{
            type: 'datePicker',
            prop: 'operatingTime',
            label: '操作时间',
            itemType: 'datetime',
            format: 'yyyy-MM-dd HH:mm:ss'
          }]
          this.tableLabels=[{
            type: 'timestamp',
            prop: 'timestamp',
            columnOperable: 'none',
            label: '调用时间'
          },{
            type: 'word',
            prop: 'cmdLine',
            label: '命令行'
          }]
          this.hasOperation=false
          this.labelWidth = 60;
          break;
          break;
        case 5:
          this.searchLabels=[{
            type: 'input',
            prop: 'fileName',
            label: '设备名称'
          },{
            type: 'datePicker',
            prop: 'operatingTime',
            label: '操作时间',
            itemType: 'datetime',
            format: 'yyyy-MM-dd HH:mm:ss'
          }]
          this.tableLabels=[{
            type: 'timestamp',
            prop: 'timestamp',
            columnOperable: 'none',
            label: '操作时间'
          },{
            type: 'word',
            prop: 'fileName',
            label: '设备名称'
          },{
            type: 'word',
            prop: 'fileLocation',
            label: '设备ID'
          }]
          this.hasOperation=false
          this.labelWidth = 60;
          break;
        default:
          this.$router.push('/')
          break;
      }
    },
    async handleTabClick(value){
      this.tabContentOnff = false;
      this.currentTab = value;
      await this.changeConfig(value);
      this.tabContentOnff = true;
    },
    getHistoryLatestOne(){
      let params = {};
      //TODO: 获取历史
      // if(this.$route.name === 'programProgressFromHistory'){
      //   params.id = +this.$route.params.historyId;
      // }else{
      //   params.pid = this.progressDetail.pid;
      //   params.name = this.progressDetail.imageName;
      // }
      return new Promise((res, rej) => {
        this.$http({
          url: this.$api.apiHistoryLatestOne,
          method: 'POST',
          data: params
        }).then((r) => {
          if (r.code === '0' && r.data) {
            this.historyDetail = r.data;
            switch(this.historyDetail.status){
              case 0: 
                this.historyDetail['statusWord'] = '监控中';
                break;
              case 1: 
                this.historyDetail['statusWord'] = '已完成';
                break;
            }
            res(r.data);
          }else{
            rej({})
          }
        }).catch((err) => {
          rej(err)
        })
      }) 
    },
    updateList(){
      this.getHistoryLatestOne().then((res) => {
        if(res.status === 0 || res.writeStatus === 0){
          let params = JSON.parse(
            sessionStorage.getItem(`${this.comData.id}Page`)
          );
          this.getList(params).then((res) => {
            clearTimeout(timer)
            timer = setTimeout(() => {
              this.updateList();
            }, 2000)
          })
        }
      })
    },
    getList(params){
      params.data['type'] = +this.currentTab;
      params.data['softwareHistoryId'] = +this.$route.params.historyId;
      if(params.data.operatingTimeStart){
        delete params.data.operatingTimeStart
      }
      if(params.data.operatingTime_date){
        params.data.operatingTimeStart=this.$utils.funcData.formDateGMT(params.data.operatingTime_date[0],'yyyy-MM-dd hh:mm:ss')
        params.data.operatingTimeEnd=this.$utils.funcData.formDateGMT(params.data.operatingTime_date[1],'yyyy-MM-dd hh:mm:ss')
        delete params.data.operatingTime_date
      }
      console.log(params);
      return new Promise((res, rej) => {
        this.$http({
          url: this.$api.apiMonitorRecordSearch,
          method: 'POST',
          data: params
        }).then((r) => {
          if (r.code === '0') {
            this.tableData = r.data;
            this.totalItems = r.totalItems;
            res(r.data);
          }
        }).catch((err) => {
          rej(err);
        })
      }) 
    },
    downloadFile(data){
      let config = {
        type: +this.currentTab
      };
      let fileName = '';
      if(config.type === 1){
        // config.pid = data.scope.row.pid;
        // config.fileName = data.scope.row.fd;
        fileName = String(data.scope.row.fd);
      }else{
        fileName = data.scope.row.fileLocation.split('\\').pop();
        // config.pid = data.scope.row.path.split('/').shift();
        // config.fileName=fileName
      }
      config = {
        type: config.type,
        name: fileName,
        fdPath:data.scope.row.fdPath,
        path:data.scope.row.fileLocation,
        mode:data.scope.row.mode
      }
      this.$http({
        url: this.$api.apiFileOperationDownload,
        method: 'POST',
        headers:  {
          'Content-Type': 'application/json;charset=utf-8'
        },
        data: config,
        responseType: "blob"
      }).then((r) => {
        // 创建a标签并点击， 即触发下载
        let url = window.URL.createObjectURL(r);
        let link = document.createElement("a");
        link.href = url;
        link.download = fileName;
        link.click();
        window.URL.revokeObjectURL(link.href);
      })
    },
    downloadChangeFileDetail(data){
      console.log(data);
      let config = {
        type: +this.currentTab
      };
      let fileName = '';
      if(config.type === 1){
        // config.pid = data.scope.row.pid;
        // config.fileName = data.scope.row.fd;
        fileName = String(data.scope.row.fd);
      }else{
        fileName = data.scope.row.fileLocation.split('\\').pop();
        // config.pid = data.scope.row.path.split('/').shift();
        // config.fileName=fileName
      }
      config = {
        type: config.type,
        name: fileName,
        fdPath:data.scope.row.fdPath,
        relateWrite:data.scope.row.relateWrite,
        path:data.scope.row.fileLocation,
        mode:data.scope.row.mode
      }
      this.$http({
        url: this.$api.apiFileOperationDownloadWriteDetail,
        method: 'POST',
        headers:  {
          'Content-Type': 'application/json;charset=utf-8'
        },
        data: config,
        responseType: "blob"
      }).then((r) => {
        // 创建a标签并点击， 即触发下载
        // let blob = new Blob([r], {type: 'application/zip'})
        let url = window.URL.createObjectURL(r);
        let link = document.createElement("a");
        link.href = url;
        link.download = fileName+'.zip';
        link.click();
        window.URL.revokeObjectURL(link.href);
      })
    },
    starMonitor(){
      this.$store.dispatch('getSoftwareDetail', +this.$route.params.programId).then((res) => {
        let programDetail = res;
        this.$http({
          url: this.$api.apiProcessMonitorSoftware,
          method: 'POST',
          data: {
            path: this.softwareDetail.path,
            name: this.softwareDetail.name,
            pidArr: JSON.stringify(this.processList.map(item => item.pid)) 
          }
        }).then((r) => {
          if (r.code === '0') {
            this.$message.success('开启监控成功');
            this.updateList();
          }
        })
      });
    },
    stopMonitor(){
      this.$http({
        url: this.$api.apiProcessStopMonitorSoftware,
        method: 'POST',
        data: {
          name: this.softwareDetail.name,
          pidArr: JSON.stringify(this.processList.map(item => item.pid))
        }
      }).then((r) => {
        if (r.code === '0') {
          this.$message.success('关闭监控成功');
          this.getHistoryLatestOne();
          let params = JSON.parse(
            sessionStorage.getItem(`${this.comData.id}Page`)
          );
          this.getList(params);
        }
      })
    },
    changeStateInHistory(id){
      this.$http({
        url: this.$api.apiHistoryChangeState,
        method: 'POST',
        data: {
          id,
        }
      }).then((r) => {
        this.getProgressDetail();
      })
    },
  },
  beforeDestroy(){
    clearTimeout(timer);
    timer = null;
  }
}
</script>
<style lang="less" scoped>
#monitoringRecords{
  height: calc(~"100% - 45px");;
  .progressDetail{
    padding: 30px 32px 34px 32px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid #d8d8d8;
    .icon{
      width: 58px;
      height: 51px;
    }
    .name{
      font-size: 18px;
      font-weight: 600;
      margin-left: 20px;
    }
    .status{
      font-size: 14px;
      margin-left: 20px;
    }
    .monitorBtn{
      color: #1677ff;
      border: 1.5px solid #1677ff;
      width: 108px;
      height: 32px;
      background: #fff;
      border-radius: 4px;
      outline: none;
      cursor: pointer;
    }
    .monitorBtn:hover{
      color: #fff;
      background: #1677ff;
    }
  }
  .softwareDetail{
    padding: 0 32px;
    margin-top: 32px;
    height: calc(~"100% - 190px");
    .tabs{
      margin-bottom: 22px;
      .tab{
        font-size: 0;
        display: inline-block;
        span{
          font-size: 14px;
          color: #ccc;
        }
        .tabName{
          cursor: pointer;
        }
      }
    }
    .records{
      height: 100%;
    }
  }
}
</style>