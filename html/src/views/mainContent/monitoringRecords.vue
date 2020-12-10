<template>
  <div id="monitoringRecords">
    <div class="progressDetail">
      <div>
        <img v-if="detailOnff" :src="softwareDetail.base64Icon" class="icon" />
        <span class="name" v-if="detailOnff">{{softwareDetail.softwareName}}</span>
        <span class="status"
          :style="{color: softwareDetail.status === '已完成' ? '#f7d666' : '#0cab51'}">{{softwareDetail.status}}</span>
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
          <span class="tabName" :style="{color: currentTab === itemInTL.value ? '#333' : '#ccc'}"
            @click="handleTabClick(itemInTL.value)">
            {{itemInTL.label}}
          </span>
        </div>
      </div>

      <div v-if="tabContentOnff" class="records">
        <BaseSearchCom :comData="comData" :formLabel="searchLabels" :getData="getList" :labelWidth="labelWidth" />
        <BaseTableCom :hadIndex='true' :tableData="tableData" :tableHeader="tableLabels" :getTableData="getList"
          :totalItems="totalItems" :comData="comData" :hasOperationBtn="hasOperation"
          :style="{height: 'calc(100% - 100px)'}" :tableStyle="{ height: 'calc(100% - 49px)', 'padding-top': '0'}">
          <template v-slot:operationBtn="data">
            <el-button v-if="showBtn(data.scope.row)" size="mini" type="primary"
              @click="data.scope.row.readAndWriteType=='write'?downloadChangeFileDetail(data):downloadFile(data)">
              {{buttonText}}</el-button>
          </template>
        </BaseTableCom>
      </div>
    </div>
  </div>
</template>

<script>
  let timer = null; //定时器
  export default {
    data() {
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
        hasOperation: null,
        operationName: null,
        dataTypes: [{
          label: '二进制',
          value: 'REG_BINARY'
        }, {
          label: 'DWORD',
          value: 'REG_DWORD'
        }, {
          label: 'QWORD',
          value: 'REG_QWORD'
        }, {
          label: '可扩展字符串',
          value: 'REG_EXPAND_SZ'
        }, {
          label: '多字符串',
          value: 'REG_MULTI_SZ'
        }, {
          label: '字符串',
          value: 'REG_SZ'
        }],
        tabLabels: [{
          label: '文件读写',
          value: '2'
        }, {
          label: '注册表',
          value: '3'
        }, {
          label: '进程调用',
          value: '4'
        }, {
          label: '设备控制',
          value: '5'
        }, {
          label: '网络访问',
          value: '1'
        }],
        currentTab: '2',
        tabContentOnff: false,
        progressDetail: null,
        progressDetailKey: 'progressDetail',
        softwareDetail: {},
        processList: [],
        opTypeLists: ['读', '写'],
        sensitivityLists: ['低', '中', '高'],
        funcApi: null, // 对应的tabs的列表的接口
        typeLists:{
          0: '监控开始',
          4095: '监控结束',
          4096: '发起网络连接',
          4097: 'TCP数据发送',
          4098: 'TCP数据接收',
          4099: 'UDP数据发送',
          4100: 'UDP数据接收',
          8192: '文件打开',
          8193: '文件写入',
          12288: '注册表打开或创建键',
          12290: '注册表删除键',
          12291: '注册表删除值键',
          12294: '注册表设置值键',
          16384: '启动进程',
          20480: '创建远程线程',
          20481: 'WM_COPYDATA发送',
          24576: '修改对象安全描述符'
        },// 操作类型code---text
        buttonText: '', // 按钮文案
      }
    },
    created() {
      this.changeConfig(this.currentTab);
      this.initData();
    },
    watch: {
      "$store.state.progressDetail": {
        handler(to, from) {
          this.initData();
        },
        deep: true
      }
    },
    methods: {
      showBtn(row) {
        const arr = ['1','2','4']
        if(arr.includes(this.currentTab)){
          if(this.currentTab === '4' && row.type !== this.typeLists[20481]){
            return false;
          }else{
            return true;
          }
        }else{
          return false;
        }
      },
      async initData() {
        await this.$store.dispatch('getSoftwareDetail', this.$route.params.programId).then((res) => {
          this.softwareDetail = res;
          this.$route.query.data && (this.softwareDetail.status = JSON.parse(this.$route.query.data).status);
        });
        if (this.$route.name === 'programProgressFromIndex' || this.$route.name === 'programProgressFromHistory') {
          this.detailOnff = true;
          this.tabContentOnff = true;
        } else {

        }
        this.updateList();
      },
      changeConfig(type) {
        type = Number(type);
        this.comData.id = `softwareDetail_${type}`;
        this.tableData = [];
        switch (type) {
          case 1:
            this.funcApi = this.$api.actionListByNetworkType;
            this.buttonText = '下载网络包'
            this.searchLabels = [{
              type: 'input',
              prop: 'host',
              label: '目标IP'
            }, {
              type: 'input',
              prop: 'port',
              label: '目标端口'
            }, {
              type: 'select',
              prop: 'type',
              label: '类型',
              options: [{
                label: '发送网络连接',
                value: 4096
              },{
                label: 'TCP数据发送',
                value: 4097
              },{
                label: 'TCP数据接收',
                value: 4098
              }]
            }, {
              type: 'datePicker',
              prop: 'operatingTime',
              label: '操作时间',
              itemType: 'datetime',
              format: 'yyyy-MM-dd HH:mm:ss'
            }]
            this.tableLabels = [{
              type: 'timestamp',
              prop: 'timestamp',
              columnOperable: 'none',
              label: '访问时间'
            }, {
              type: 'word',
              prop: 'type',
              label: '链接类型'
            }, {
              type: 'word',
              prop: 'host',
              label: '目的IP地址'
            }, {
              type: 'word',
              prop: 'protocol',
              label: '协议类型'
            }, {
              type: 'word',
              prop: 'warningParams ',
              label: '敏感数据字段'
            }, {
              type: 'word',
              prop: 'bytes',
              label: '网络流量'
            }]
            this.hasOperation = true
            this.operationName = '下载网络包'
            this.labelWidth = 60;
            break;
          case 2:
            this.funcApi = this.$api.actionListByFileType;
            this.buttonText = '打开文件'
            this.searchLabels = [{
              type: 'input',
              prop: 'opType',
              label: '读写类型'
            }, {
              type: 'input',
              prop: 'fileName',
              label: '文件名称'
            }, {
              type: 'input',
              prop: 'sensitivity',
              label: '文件敏感度'
            }, {
              type: 'datePicker',
              prop: 'operatingTime',
              label: '操作时间',
              itemType: 'datetime',
              format: 'yyyy-MM-dd HH:mm:ss'
            }]
            this.tableLabels = [{
              type: 'timestamp',
              prop: 'timestamp',
              columnOperable: 'none',
              label: '读写时间'
            }, {
              type: 'word',
              prop: 'fileName',
              label: '文件名称'
            }, {
              type: 'word',
              prop: 'path',
              label: '文件位置'
            }, {
              type: 'word',
              prop: 'opType',
              label: '读写类型'
            }, {
              type: 'word',
              prop: 'sensitivity',
              label: '文件敏感度'
            }]
            this.hasOperation = true;
            this.operationName = '打开文件';
            this.labelWidth = 70;
            break;
          case 3:
            this.funcApi = this.$api.actionListByRegistryType;
            this.searchLabels = [{
              type: 'input',
              prop: 'key',
              label: '目标键'
            }, {
              type: 'input',
              prop: 'valueName',
              label: '值键'
            }, {
              type: 'select',
              prop: 'valueType',
              label: '值键类型',
              options: this.dataTypes
            }, {
              type: 'datePicker',
              prop: 'operatingTime',
              label: '操作时间',
              itemType: 'datetime',
              format: 'yyyy-MM-dd HH:mm:ss'
            }]
            this.tableLabels = [{
              type: 'timestamp',
              prop: 'timestamp',
              columnOperable: 'none',
              label: '操作时间'
            }, {
              type: 'word',
              prop: 'type',
              label: '操作类型'
            }, {
              type: 'word',
              prop: 'key',
              label: '目标键'
            }, {
              type: 'word',
              prop: 'valueName',
              label: '值键'
            }, {
              type: 'select',
              columnOperable: 'none',
              prop: 'valueType',
              label: '值键类型',
              options: this.dataTypes
            }, {
              type: 'word',
              prop: 'data',
              label: '值键值'
            }, {
              type: 'select',
              columnOperable: 'none',
              prop: 'oldValueType',
              label: '原有键值类型',
              options: this.dataTypes
            }, {
              type: 'word',
              prop: 'oldData',
              label: '原值键值'
            }];
            this.hasOperation = false;
            this.labelWidth = 60;
            break;
          case 4:
            this.funcApi = this.$api.actionListByProcessType;
            this.buttonText = '查看';
            this.searchLabels = [{
              type: 'input',
              prop: 'commandLine',
              label: '命令行'
            }, {
              type: 'select',
              prop: 'type',
              label: '调用类型',
              options: [{
                label: '启动进程',
                value: 16384
              },{
                label: '进程注入',
                value: 20480
              },{
                label: '消息通讯',
                value: 20481
              }]
            }, {
              type: 'datePicker',
              prop: 'operatingTime',
              label: '操作时间',
              itemType: 'datetime',
              format: 'yyyy-MM-dd HH:mm:ss'
            }]
            this.tableLabels = [{
              type: 'timestamp',
              prop: 'timestamp',
              columnOperable: 'none',
              label: '调用时间'
            }, {
              type: 'word',
              prop: 'type',
              label: '调用类型'
            }, {
              type: 'word',
              prop: 'threadEntryAddress',
              label: '函数地址'
            }, {
              type: 'word',
              prop: 'path',
              label: '注入dll'
            }, {
              type: 'word',
              prop: 'destHwnd',
              label: '消息目标句柄'
            }, {
              type: 'word',
              prop: 'srcHwnd',
              label: '消息源句柄'
            }, {
              type: 'word',
              prop: 'cmdLine',
              label: '命令行'
            }]
            this.hasOperation = true
            this.labelWidth = 60;
            break;
            break;
          case 5:
            this.searchLabels = [{
              type: 'input',
              prop: 'fileName',
              label: '设备名称'
            }, {
              type: 'datePicker',
              prop: 'operatingTime',
              label: '操作时间',
              itemType: 'datetime',
              format: 'yyyy-MM-dd HH:mm:ss'
            }]
            this.tableLabels = [{
              type: 'timestamp',
              prop: 'timestamp',
              columnOperable: 'none',
              label: '操作时间'
            }, {
              type: 'word',
              prop: 'fileName',
              label: '设备名称'
            }, {
              type: 'word',
              prop: 'fileLocation',
              label: '设备ID'
            }]
            this.hasOperation = false
            this.labelWidth = 60;
            break;
          default:
            this.$router.push('/')
            break;
        }
      },
      async handleTabClick(value) {
        this.tabContentOnff = false;
        this.currentTab = value;
        await this.changeConfig(value);
        this.tabContentOnff = true;
      },
      getHistoryLatestOne() {
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
              switch (this.historyDetail.status) {
                case 0:
                  this.historyDetail['statusWord'] = '监控中';
                  break;
                case 1:
                  this.historyDetail['statusWord'] = '已完成';
                  break;
              }
              res(r.data);
            } else {
              rej({})
            }
          }).catch((err) => {
            rej(err)
          })
        })
      },
      updateList() {
        let params = JSON.parse(
          sessionStorage.getItem(`${this.comData.id}Page`)
        );
        this.getList(params).then((res) => {
          clearTimeout(timer)
          timer = setTimeout(() => {
            this.updateList();
          }, 2000)
        })
      },
      // 处理列表搜索条件
      handleParams(params) {
        switch (this.comData.id) {
          case 'softwareDetail_1':
            return params;
            // 网络请求
          case 'softwareDetail_2':
            // 文件读写
            if (params.data.opType) {
              if (params.data.opType === '读') {
                params.data.opType = 1
              } else if (params.data.opType === '写') {
                params.data.opType = 2
              } else {
                this.$message.error('请输入正确的读写类型');
                return
              }
            }
            if (params.data.sensitivity) {
              if (params.data.sensitivity === '低') {
                params.data.sensitivity = 1;
              } else if (params.data.sensitivity === '中') {
                params.data.sensitivity = 2;
              } else if (params.data.sensitivity === '高') {
                params.data.sensitivity = 3;
              } else {
                this.$message.error('请输入正确的文件敏感度');
                return;
              }
            }
            return params;
          case 'softwareDetail_3':
            // 注册表
            return params;
          case 'softwareDetail_4':
            // 进程调用
            return params;
          case 'softwareDetail_5':
            // 命令行
            return params;
        }
      },
      // 处理列表返回的结果
      handleResult(data) {
        switch (this.comData.id) {
          case 'softwareDetail_1':
            data.forEach(item => {
              item.type = item.type ? this.typeLists[item.type] : '--';
            })
            // 网络访问
            return data;
          case 'softwareDetail_2':
            // 文件读写
            data.forEach(item => {
              item.opType = item.opType ? this.opTypeLists[item.opType - 1] : '--';
              item.sensitivity = item.sensitivity ? this.sensitivityLists[item.sensitivity - 1] : '--';
            })
            return data;
          case 'softwareDetail_3':
            // 注册表
            data.forEach(item => {
              item.type = item.type ? this.typeLists[item.type] : '--';
            })
            return data;
          case 'softwareDetail_4':
            // 进程调用
            data.forEach(item => {
              item.type = this.typeLists[item.type];
            })
            return data;
          case 'softwareDetail_5':
        }
      },
      getList(params) {
        let postParams = this.handleParams(params);
        // 处理时间
        if (params.data.operatingTime_date) {
          postParams.data.startTime = this.$utils.funcData.formDateGMT(params.data.operatingTime_date[0]);
          postParams.data.endTime = this.$utils.funcData.formDateGMT(params.data.operatingTime_date[1]);
          delete postParams.data.operatingTimeStart;
          delete postParams.data.operatingTime_date;
        }
        // 添加taskId
        if(this.$route.params.historyId){
          // 从历史进去，taskId是historyId
          postParams.data.taskId = Number(this.$route.params.historyId)
        }else{
          // 如果没有historyId，就用软件详情的taskId
          postParams.data.taskId = this.softwareDetail.taskId
        }
        return new Promise((res, rej) => {
          this.$http({
            url: this.funcApi,
            method: 'POST',
            data: postParams
          }).then((r) => {
            if (r.code === '0') {
              this.tableData = this.handleResult(r.data);
              this.totalItems = r.totalItems;
              res(r.data);
            }
          }).catch((err) => {
            rej(err);
          })
        })
      },
      downloadFile(data) {
        let config = {
          type: +this.currentTab
        };
        let fileName = '';
        if (config.type === 1) {
          // config.pid = data.scope.row.pid;
          // config.fileName = data.scope.row.fd;
          fileName = String(data.scope.row.fd);
        } else {
          fileName = data.scope.row.fileLocation.split('\\').pop();
          // config.pid = data.scope.row.path.split('/').shift();
          // config.fileName=fileName
        }
        config = {
          type: config.type,
          name: fileName,
          fdPath: data.scope.row.fdPath,
          path: data.scope.row.fileLocation,
          mode: data.scope.row.mode
        }
        this.$http({
          url: this.$api.apiFileOperationDownload,
          method: 'POST',
          headers: {
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
      downloadChangeFileDetail(data) {
        // console.log(data);
        let config = {
          type: +this.currentTab
        };
        let fileName = '';
        if (config.type === 1) {
          // config.pid = data.scope.row.pid;
          // config.fileName = data.scope.row.fd;
          fileName = String(data.scope.row.fd);
        } else {
          fileName = data.scope.row.fileLocation.split('\\').pop();
          // config.pid = data.scope.row.path.split('/').shift();
          // config.fileName=fileName
        }
        config = {
          type: config.type,
          name: fileName,
          fdPath: data.scope.row.fdPath,
          relateWrite: data.scope.row.relateWrite,
          path: data.scope.row.fileLocation,
          mode: data.scope.row.mode
        }
        this.$http({
          url: this.$api.apiFileOperationDownloadWriteDetail,
          method: 'POST',
          headers: {
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
          link.download = fileName + '.zip';
          link.click();
          window.URL.revokeObjectURL(link.href);
        })
      },
      starMonitor() {
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
      stopMonitor() {
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
      changeStateInHistory(id) {
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
    beforeDestroy() {
      clearTimeout(timer);
      timer = null;
    }
  }

</script>
<style lang="less" scoped>
  #monitoringRecords {
    height: calc(~"100% - 45px");
    ;

    .progressDetail {
      padding: 30px 32px 34px 32px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      border-bottom: 1px solid #d8d8d8;

      .icon {
        width: 58px;
        height: 51px;
      }

      .name {
        font-size: 18px;
        font-weight: 600;
        margin-left: 20px;
      }

      .status {
        font-size: 14px;
        margin-left: 20px;
      }

      .monitorBtn {
        color: #1677ff;
        border: 1.5px solid #1677ff;
        width: 108px;
        height: 32px;
        background: #fff;
        border-radius: 4px;
        outline: none;
        cursor: pointer;
      }

      .monitorBtn:hover {
        color: #fff;
        background: #1677ff;
      }
    }

    .softwareDetail {
      padding: 0 32px;
      margin-top: 32px;
      height: calc(~"100% - 190px");

      .tabs {
        margin-bottom: 22px;

        .tab {
          font-size: 0;
          display: inline-block;

          span {
            font-size: 14px;
            color: #ccc;
          }

          .tabName {
            cursor: pointer;
          }
        }
      }

      .records {
        height: 100%;
      }
    }
  }

</style>
