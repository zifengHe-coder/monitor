<template>
  <!-- 程序进程列表 -->
  <div id="softwareProcess">
    <div class="softwareDetail">
      <div class="softwareInfo">
        <div class="softwareName">
          <img :src="softwareData.iconUrl" alt="" />
          <span>{{ softwareData.softwareName }}</span>
          <span>{{ softwareStatus === 1 ? "未监听" : "监听中" }}</span>
        </div>
        <p>
          <span>文件夹路径</span>
          <span>{{ softwareData.exePath }}</span>
        </p>
        <p>
          <span>文件大小</span>
          <span>{{ softwareData.bytes | bytesMb }}</span>
        </p>
        <p>
          <span>创建时间</span>
          <span>{{ softwareData.createTime | renderTime }}</span>
        </p>
      </div>
      <div class="softwareOperation">
        <el-button
          plain
          class="softwareBtn"
          @click="openSoftware"
          v-show="processList.length === 0 && btnStatus"
          >打开程序</el-button
        >
        <el-button
          plain
          class="softwareBtn"
          @click="startMonitor"
          v-show="processList.length >= 1 && softwareStatus == 1"
          >开始监听</el-button
        >
        <el-button
          plain
          class="softwareBtn"
          @click="stopMonitor"
          v-show="processList.length >= 1 && softwareStatus == 0"
          >停止监听</el-button
        >
        <el-button
          plain
          class="softwareBtn"
          @click="goDetail"
          v-show="processList.length > 1"
          >查看详情</el-button
        >
        <el-button plain class="softwareBtn" @click="goHistory"
          >查看历史监控</el-button
        >
      </div>
    </div>
    <div class="processList">
      <BaseTableCom
        :tableData="processList"
        :tableHeader="tableLabels"
        :getTableData="searchProcessList"
        :totalItems="totalItems"
        :comData="comData"
        :operationWidth="150"
        :hasOperationBtn="false"
        :showPage="false"
        :itemKey="itemKey"
        :style="{ height: '100%' }"
        :tableStyle="{ height: '100%', 'padding-top': '0' }"
      >
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
      softwareStatus: 1,
      btnStatus: true,
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
      tableLabels: [
        {
          type: "word",
          prop: "id",
          label: "序号"
        },
        {
          type: "image",
          prop: "iconUrl",
          label: "进程名称"
        },
        {
          type: "word",
          prop: "pid",
          label: "pid"
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
          type: "word",
          prop: "status",
          label: "状态"
        }
      ],
      totalItems: 0,
      preProcess: [],
      preAllTime: 0,
      pidArr: [],
      processTimer: null
    };
  },
  filters: {
    //字节大小转换M
    bytesMb(num) {
      return (Number(num) / 1024 / 1024).toFixed(2) + "M";
    },
    //时间格式处理
    renderTime(date) {
      var dateee = new Date(date).toJSON();
      return new Date(+new Date(dateee) + 8 * 3600 * 1000)
        .toISOString()
        .replace(/T/g, " ")
        .replace(/\.[\d]{3}Z/, "");
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
    console.log(res)
    if (res) {
      let arr = res.exePath.split("\\");
      this.exeName = arr.pop();
      for (let key in res) {
        this.softwareData[key] = res[key];
      }
    }
    // this.getData();
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
      return new Promise((res, rej) => {
        if (this.exeName) {
          this.$http({
            url: this.$api.apiFileOperationGetProcessList,
            method: "POST",
            data: {
              data: {
                imageName: this.exeName,
                programName: this.softwareData.name
              }
            }
          }).then(r => {
            let cputime = 0;
            let pidAll = [];
            this.processList = r.data.filter((v, index) => {
              pidAll.push(v.pid);
              v.wsPrivateBytes = Number(v.wsPrivateBytes) / 1024;
              v.id =
                Number(index) > 8
                  ? Number(index) + 1
                  : "0" + (Number(index) + 1);
              if (this.preProcess.length > 0) {
                v.cpu = (0).toFixed(2);
                this.preProcess.forEach(el => {
                  if (v.pid == el.pid) {
                    v.cpu = (
                      ((v.cpuTime - el.cpuTime) /
                        (r.allTime - this.preAllTime)) *
                      100
                    ).toFixed(2);
                    cputime = Number(cputime) + Number(v.cpu);
                  }
                });
              } else {
                v.cpu = (0).toFixed(2);
              }
              return v;
            });
            console.log(cputime.toFixed(2));
            this.timeStamp++;
            this.preProcess = this.processList;
            this.preAllTime = r.allTime;
            this.pidArr = pidAll;
            this.softwareStatus = r.status;
            if (this.processList.length == 0) {
              this.btnStatus = true;
            }
            res(true);
          });
        }
      });
    },
    //跳转详情页
    goDetail() {
      this.$store
        .dispatch("getSoftwareDetail", +this.softwareData.id)
        .then(res => {
          this.$router.push({
            path: `/programProgress/${this.softwareData.id}/${this.processList[0].softwareHistoryId}`
          });
        });
    },
    //跳转历史监控页
    goHistory() {
      this.$store
        .dispatch("getSoftwareDetail", +this.softwareData.id)
        .then(res => {
          this.$router.push({
            path: `/monitoringHistory/${this.softwareData.id}`,
            params: {
              data: {
                name: this.softwareData.name,
                id: this.softwareData.id,
                path: this.softwareData.path,
                groupName: this.softwareData.groupName,
                bytes: this.softwareData.bytes,
                iconUrl: this.softwareData.iconUrl,
                createTime: this.softwareData.createTime
              }
            }
          });
        });
    },
    //打开程序
    openSoftware() {
      if (this.softwareData.path) {
        this.$http({
          url: this.$api.apiActionsLogOpenSoftwareMonitor,
          method: "POST",
          data: {
            path: this.softwareData.path,
            name: this.softwareData.name
          }
        }).then(r => {
          console.log(r);
          if (r.code == "0") {
            this.$store.dispatch('resetSoftwareList')
            this.btnStatus = false;
          }
        });
      }
    },
    //开始监控程序所有进程
    startMonitor() {
      console.log(this.pidArr);
      this.$http({
        url: this.$api.apiProcessMonitorSoftware,
        method: "POST",
        data: {
          path: this.softwareData.path,
          name: this.softwareData.name,
          pidArr: JSON.stringify(this.pidArr)
        }
      }).then(r => {
        console.log(r);
        if (r.code && r.code == "0") {
          this.$message({
            message: "开启监听成功",
            type: "success"
          });
          this.$store.dispatch('resetSoftwareList')
          this.softwareStatus = 0;
        }
      });
    },
    //停止监控程序所有进程
    stopMonitor() {
      console.log(this.pidArr);
      this.$http({
        url: this.$api.apiProcessStopMonitorSoftware,
        method: "POST",
        data: {
          name: this.softwareData.name,
          pidArr: JSON.stringify(this.pidArr)
        }
      }).then(r => {
        console.log(r);
        if (r.code && r.code == "0") {
          this.$message({
            message: "关闭监听成功",
            type: "success"
          });
          this.$store.dispatch('resetSoftwareList')
          this.softwareStatus = 1;
        }
      });
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
  height: calc(~"100% - 45px");;
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
