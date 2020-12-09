<template>
  <div id="softwareList">
    <div class="header">
      <span class="title">本机程序</span>
      <el-button round class="addExeBtn" @click="dialogVisible = true">
        <i size="mini" class="el-icon-circle-plus-outline" />
        添加程序
      </el-button>
    </div>
    <!-- <BaseFileList 
      title="选择程序" 
      :visible.sync="dialogVisible" 
      @ensure='chooseExe' 
      @cancel='dialogVisible = false'>
    </BaseFileList> -->
    <div class="softwareList" v-infinite-scroll="getList">
      <div v-for="(itemInSL, key) in $store.state.softwareProcess.softwareList" :key="key">
        <p class="groupName">{{key === '1' ? '常用程序' : key}}</p>
        <div class="group">
          <div class="groupItem" v-for="(itemInSLL, index) in itemInSL" :key="index">
            <span @click="goOther(itemInSLL)" class="software">
              <img :src="itemInSLL.iconUrl" style="width: 20px;height: 20px;vertical-align: -webkit-baseline-middle;" />
              <!-- 软件名称长度超出就显示提示框 -->
              <el-tooltip :disabled="itemInSLL.softwareName.length<29" :content="itemInSLL.name" placement="top"
                effect="light">
                <span class="softwareName">{{itemInSLL.softwareName}}</span>
              </el-tooltip>
            </span>
            <span class="star">
              <!-- TODO: 判断是否是监控中 -->
              <!-- <img src="../../assets/monitor.png" style="width: 14px;" v-if="itemInSLL.monitor" /> -->
              <img src="../../assets/starOn.png" style="width: 14px;" v-if="itemInSLL.favorite"
                @click="removeFavorite(itemInSLL.id)" />
              <img src="../../assets/starOff.png" style="width: 14px;" v-else @click="addFavorite(itemInSLL.id)" />
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
  export default {
    data() {
      return {
        comData: {
          id: 'softwareList'
        },
        dialogVisible: false,
        tableData: {}
      }
    },
    created() {
      sessionStorage.setItem(`${this.comData.id}Page`, JSON.stringify({
        data: {},
        pageNo: 0,
        pageSize: 20
      }))
    },
    watch: {
      "$store.state.softwareProcess.softwareList": {
        handler(newVal) {
          this.tableData = newVal;
        },
        deep: true
      }
    },
    methods: {
      addFavorite(id) {
        this.$http({
          url: this.$api.softwareAddFavorite,
          method: 'POST',
          data: {
            data: {
              id
            }
          }
        }).then(r => {
          if (r.code === '0') {
            this.$store.dispatch('resetSoftwareList')
          }
        })
      },
      removeFavorite(id) {
        this.$http({
          url: this.$api.softwareRemoveFavorite,
          method: 'POST',
          data: {
            data: {
              id
            }
          }
        }).then(r => {
          if (r.code === '0') {
            this.$store.dispatch('resetSoftwareList')
          }
        })
      },
      goOther(data) {
        let date = new Date()
        this.$router.replace({
          path: '/softwareProcess',
          name: "softwareProcess",
          params: {
            data: data
          },
          query: {
            t: Date.now()
          }
        });
      },
      chooseExe(item) {
        if (!item.isDirectory) {
          this.$http({
            url: this.$api.apiSoftwareAdd,
            method: 'POST',
            data: {
              path: item.path
            }
          }).then((r) => {
            if (r.code === '0') {
              this.$store.dispatch('resetSoftwareList')
            }
            this.dialogVisible = false;
          })
        } else {
          this.$notify({
            message: '当前选中内容为文件夹，请重新选择',
            type: 'warning'
          })
        }
      },
      getList() {
        this.$store.dispatch('getSoftwareList')
      }
    }
  }

</script>
<style lang="less" scoped>
  #softwareList {
    padding-top: 32px;

    .header {
      border-bottom: 1px solid #0c0c15;
      height: 63px;
      padding: 0 20px;
    }

    .title {
      font-size: 18px;
      font-weight: 600;
      line-height: 33px;
    }

    .addExeBtn {
      background: #414163;
      font-size: 14px;
      color: #fff;
      border: 0;
      float: right;
      padding-top: 10px;
      padding-bottom: 10px;
    }

    .softwareList {
      border-top: 1px solid #222234;
      padding: 0 20px;

      .groupName {
        font-size: 14px;
        margin-top: 20px;
      }

      .group {
        .groupItem {
          display: block;
          height: 28px;
          margin: 9px 0;

          .software {
            cursor: pointer;
          }

          .software:hover {
            color: #2f77ff;
          }

          .softwareName,
          .star {
            display: inline-block;
            height: 28px;
            line-height: 28px;
          }

          .softwareName {
            font-size: 14px;
            margin-left: 10px;
            width: 190px;
            vertical-align: top;
            white-space: nowrap;
            text-overflow: ellipsis;
            overflow: hidden;
            word-break: break-all;
          }

          .star {
            float: right;
            color: #f7d666;
            cursor: pointer;
          }
        }
      }
    }
  }

</style>
