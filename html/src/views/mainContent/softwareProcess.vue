<template>
  <!-- 程序进程列表 -->
  <div id="softwareProcess">
    <div class="softwareDetail">
      <div class="softwareInfo">
        <div class="softwareName">
          <img v-if="system!=='linux'" :src="softwareData.base64Icon" alt="" />
          <span>{{ softwareData.softwareName }}</span>
          <span>{{ softwareData.monitoring ? "监听中" : "未监听" }}</span>
        </div>
        <p>
          <span>文件夹路径</span>
          <span>{{ softwareData.exePath }}</span>
        </p>
        <p>
          <span>文件大小</span>
          <span v-if="softwareData.fileSize">{{ softwareData.fileSize}}M</span>
        </p>
        <p>
          <span>创建时间</span>
          <span>{{ softwareData.fileCreationTime}}</span>
        </p>
      </div>
      <div class="softwareOperation">
        <el-button plain class="softwareBtn" @click="openSoftware(softwareData.id)" v-show="processList.length === 0 && !softwareData.monitoring">
          打开程序</el-button>
        <el-button plain class="softwareBtn" @click="startMonitor(softwareData.id)"
          v-show="processList.length>0 && !softwareData.monitoring">开始监听</el-button>
        <el-button plain class="softwareBtn" @click="stopMonitor" v-show="softwareData.monitoring">停止监听</el-button>
        <el-button plain class="softwareBtn" @click="goDetail" v-show="softwareData.monitoring">查看详情</el-button>
        <el-button plain class="softwareBtn" @click="goHistory">查看历史监控</el-button>
      </div>
    </div>
    <div class="processList">
      <BaseTableCom :tableData="processList" :tableHeader="tableLabels" :getTableData="searchProcessList"
        :totalItems="totalItems" :comData="comData" :operationWidth="150" :hasOperationBtn="false" :showPage="false"
        :itemKey="itemKey" :style="{ height: '100%' }" :tableStyle="{ height: '100%', 'padding-top': '0' }">
        <!-- <template v-slot:operationBtn="detail">
          <div>
            <el-button size="mini" @click="goDetail(detail.scope.row)"
              >查看详情</el-button
            >
          </div>
        </template> -->
      </BaseTableCom>
    </div>
  </div>
</template>

<script>
  export default {
    data() {
      return {
        itemKey: 0,
        index: 1,
        data: null,
        exeName: "",
        softwareData: {
          bytes: null,
          createTime: "",
          iconUrl: "",
          id: null,
          name: "",
          path: ""
        },
        comData: {
          id: "processList"
        },
        processList: [],
        tableLabels: [{
            type: "word",
            prop: "id",
            label: "序号"
          },
          {
            type: "image",
            prop: "processName",
            label: "进程名称"
          },
          {
            type: "word",
            prop: "pid",
            label: "pid"
          },
          {
            type: "word",
            prop: "user",
            label: "用户名",
          },
          {
            type: "word",
            prop: "cpu",
            label: "CPU(%)"
          },
          {
            type: "word",
            prop: "wsPrivateBytes",
            label: "内存(K)"
          },
          {
            type: "normalImage",
            prop: "status",
            label: "状态",
            icon: require('@/assets/warning.png'),
          }
        ],
        totalItems: 0,
        preProcess: [],
        preAllTime: 0,
        pidArr: [],
        processTimer: null,
        monitorStatus: ['未监听', '监听中', '监听失败'],
        systemLists: ['windows', 'linux']
      };
    },
    computed:{
      system(){
        return sessionStorage.getItem('system')
      }
    },
    created() {
      if (this.$route.params.data) {
        sessionStorage.setItem(
          this.$route.query.t,
          JSON.stringify(this.$route.params.data)
        );
      }
      // sessionStorage为防止刷新时无数据
      let res = JSON.parse(sessionStorage.getItem(this.$route.query.t));
      if (res) {
        let arr = res.exePath.split("\\");
        this.exeName = arr.pop();
        // 获取软件基本信息
        this.$http({
          url: this.$api.softwareDetailSoftware,
          method: "POST",
          data: {
            data: {
              id: res.id
            }
          }
        }).then(r => {
          if (r.code === '0') {
            for (let k in r.data) {
              this.$set(this.softwareData, k, r.data[k])
            }
          }
        })
      }
      this.getData();
    },
    methods: {
      //获取进程列表
      searchProcessList() {
        this.getData().then(r => {
          clearTimeout(this.processTimer);
          this.processTimer = null;
          this.processTimer = setTimeout(() => {
            this.searchProcessList();
          }, 2000);
        });
      },
      //根据程序名获取所有关联进程
      getData() {
        let res = JSON.parse(sessionStorage.getItem(this.$route.query.t));
        return new Promise((resolve, reject) => {
          if (this.exeName) {
            this.$http({
              url: this.$api.softwareDetailSoftware,
              method: "POST",
              data: {
                data: {
                  id: res.id
                }
              }
            }).then(r => {
              if (r.code === '0') {
                this.softwareData = {};
                for (let k in r.data) {
                  this.$set(this.softwareData, k, r.data[k])
                }
                this.processList = [];
                if (r.data.processes) {
                  r.data.processes.forEach((item,index) => {
                    this.processList.push({
                      id: Number(index) > 9 ? Number(index) + 1 : '0' + (Number(index)+1),
                      processName: this.system === 'linux' ? item.name : item.name + '/exe/' + r.data.base64Icon,
                      pid: item.pid,
                      wsPrivateBytes: Math.floor(item.memory),
                      user: item.user,
                      cpu: item.cpu,
                      status: this.monitorStatus[item.monitorStatus - 1],
                      showIcon: item.monitorStatus === 3 ? true : false
                    })
                  })
                  // this.processList = r.data.processes.map((item, index) => {
                  //   let obj = {};
                  //   obj.id = Number(index) > 9 ? Number(index) + 1 : '0' + (Number(index) + 1);
                  //   obj.processName = this.system === 'linux' ?  item.name : item.name + '/exe/' + r.data.base64Icon;
                  //   obj.pid = item.pid;
                  //   obj.wsPrivateBytes = Math.floor(item.memory);
                  //   obj.user = item.user;
                  //   obj.cpu = item.cpu;
                  //   obj.status = this.monitorStatus[item.monitorStatus - 1];
                  //   item.monitorStatus === 3 ? obj.showIcon = true : obj.showIcon = false;
                  //   return obj;
                  // })
                }
                resolve();
              }
            })
          }
        });
      },
      //跳转详情页
      goDetail() {
        this.$store
          .dispatch("getSoftwareDetail", this.softwareData.id)
          .then(res => {
            this.$router.push({
              path: `/programProgress/${this.softwareData.id}?isFromIndex=${true}`
            });
          });
      },
      //跳转历史监控页
      goHistory() {
        this.$router.push({
          path: `/monitoringHistory/${this.softwareData.id}`,
          params: {
            data: {
              id: this.softwareData.id,
            }
          }
        });
      },
      //打开程序
      openSoftware(id) {
        if (id) {
          this.$http({
            url: this.$api.monitorStartAndMonitor,
            method: "POST",
            data: {
              data: {
                id
              }
            }
          }).then(r => {
            if (r.code == "0") {
              this.$store.dispatch('resetSoftwareList')
            }
          });
          this.$http({
            url: this.$api.systemGetSystemOs,
            method: 'GET'
          }).then(r => {
            if (r.code === '0') {
              let system = this.systemLists[r.data - 1];
              if (!sessionStorage.getItem(`${this.softwareData.softwareName}`)) {
                sessionStorage.setItem(`${this.softwareData.softwareName}System`, system)
              }
            }
          })
        }
      },
      //开始监控程序所有进程
      startMonitor(id) {
        this.$http({
          url: this.$api.monitorStartMonitor,
          method: "POST",
          data: {
            data: {
              id
            }
          }
        }).then(r => {
          if (r.code && r.code == "0") {
            this.$message({
              message: "开启监听成功",
              type: "success"
            });
            this.$store.dispatch('resetSoftwareList')
          }
        });
      },
      //停止监控程序所有进程
      stopMonitor() {
        this.$confirm('停止监听时有可能引起应用异常退出，请先做好应用的数据保存再停止监听', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        }).then(() => {
          this.$http({
            url: this.$api.monitorStopMonitor,
            method: "POST",
            data: {
              data: {id: this.softwareData.id }
            }
          }).then(r => {
            if (r.code && r.code == "0") {
              this.$message({
                message: "关闭监听成功",
                type: "success"
              });
              this.$store.dispatch('resetSoftwareList')
            }
          });
        }).catch(()=>{})
      }
    },
    beforeDestroy() {
      clearTimeout(this.processTimer);
      this.processTimer = null;
    }
  };

</script>

<style lang="less" scoped>
  #softwareProcess {
    width: 100%;
    height: calc(~"100% - 45px");
    ;
    padding: 0;

    .softwareDetail {
      height: 250px;
      box-sizing: border-box;
      padding: 30px 30px;
      border-bottom: 1px solid #d8d8d8;

      .softwareInfo {
        float: left;

        .softwareName {
          display: flex;
          align-items: center;
          padding: 15px 0px 26px 0px;

          img {
            width: 56px;
            height: 56px;
          }

          span {
            font-size: 18px;
            color: #333333;
            letter-spacing: 0.34px;
            text-align: center;
            margin-left: 32px;

            &:nth-of-type(2) {
              font-size: 14px;
            }
          }
        }

        p {
          font-size: 12px;
          color: #999999;
          letter-spacing: 0.23px;
          line-height: 24px;
          margin: 6px 0;

          span {
            &:nth-of-type(1) {
              display: inline-block;
              width: 72px;
            }

            &:nth-of-type(2) {
              color: #666666;
            }
          }
        }
      }

      .softwareOperation {
        float: right;
        display: flex;
        flex-direction: column;

        .softwareBtn {
          width: 128px;
          height: 32px;
          padding: 0;
          border: 1px solid #2f77ff;
          border-radius: 4px;
          font-size: 12px;
          color: #2f77ff;
          letter-spacing: 0.23px;
          text-align: center;
          margin: 5px 0;

          &:hover {
            background: #2f77ff;
            color: #fff;
          }
        }
      }
    }

    .processList {
      margin: 32px 32px;
      height: calc(~"100% - 314px");
      overflow: hidden;
      overflow-y: auto;
    }
  }

</style>
