<template>
  <el-dialog
    v-bind="$attrs"
    v-on="$listeners"
    :show-close="false"
    width="640px"
    class="fileList"
    :close-on-click-modal="false"
  >
    <div class="listContent">
      <div class="topTitle">
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item
            ><a href="javascript:;" @click="getDisk"
              >此电脑</a
            ></el-breadcrumb-item
          >
          <el-breadcrumb-item v-for="(item, index) in path" :key="index"
            ><a href="javascript:;" @click="goList(item.path, item.name)">{{
              item.name
            }}</a></el-breadcrumb-item
          >
        </el-breadcrumb>
      </div>
      <div class="fileShow">
        <div
          v-for="(item, index) in fileList"
          :key="index"
          :class="{ fileItem: true, selectItem: item.path == selectItem.path }"
          @click="select(item)"
          @dblclick="item.isDirectory ? getList(item.path, item.name) : ''"
        >
          <div>
            <img :src="item.isDirectory?iconUrl[0]:iconUrl[1]" width="40px" height="40px">
          </div>
          <div class="title">{{ item.name }}</div>
        </div>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button
        size="mini"
        type="primary"
        @click="$listeners.ensure(selectItem)"
        >确 定</el-button>
      <el-button size="mini" @click="$listeners.cancel">取 消</el-button>
    </span>
  </el-dialog>
</template>

<script>

export default {
  inheritAttrs: false,
  data() {
    return {
      path: [],
      selectItem: {},
      fileList: [],
      iconUrl:[
        require('../../assets/directory.png'),
        require('../../assets/application.png')
      ]
    };
  },
  created() {
    this.getDisk();
  },
  methods: {
    // 获取系统根目录【Windows对应盘符，Linux对应根目录】
    getDisk() {
      this.path = [];
      this.selectItem = {};
      this.$http({
        url: this.$api.apiFileOperationRootList,
        method: "GET",
      }).then((r) => {
        if (r.code == "0") {
          this.fileList = [];
          r.data.forEach((item) => {
            this.fileList.push({
              path: item,
              name: item,
              isDirectory: true,
            });
          });
        }
      });
    },
    // 选中文件
    select(item) {
      this.selectItem = {
        path: item.path,
        name: item.name,
        isDirectory: item.isDirectory,
      };
    },
    // 获取当前目录下的文件列表
    getList(path, name) {
      this.$http({
        url: this.$api.apiFileOperationFileList,
        method: "POST",
        data: {
          path: path,
        },
      }).then((r) => {
        if (r.code == "0") {
          this.path.push({
            path: path,
            name: name,
          });
          this.selectItem = {};
          this.fileList = [];
          r.data.forEach((item) => {
            this.fileList.push({
              path:item.path,
              name:item.name,
              isDirectory:item.isDirectory
            })
          });
        }
      });
    },
    // 面包屑跳转
    goList(path, name) {
      for (let i = this.path.length - 1; i >= 0; i--) {
        if (this.path[i].path !== path) {
          this.path.pop();
        } else {
          break;
        }
      }
      this.$http({
        url: this.$api.apiFileOperationFileList,
        method: "POST",
        data: {
          path: path,
        },
      }).then((r) => {
        if (r.code == "0") {
          this.fileList = [];
          r.data.forEach((item) => {
            this.fileList.push({
              path: item.path,
              name: item.name,
              isDirectory: item.isDirectory,
            });
          });
        }
      });
    },
  },
};
</script>

<style lang='less' scoped>
.fileList {
  /deep/ .el-dialog {
    /deep/ .el-dialog__header {
      font-size: 16px;
      color:#333333;
      font-weight: 700;
      border-bottom: 1px solid transparent;
    }
    /deep/ .el-dialog__footer {
      border-top: 1px solid #EEEEEE;
      padding:15px 24px;
    }
    /deep/ .el-dialog__body {
      padding: 0px;
    }
    .listContent {
      height: 345px;
      width: 100%;
      overflow: hidden;
      .topTitle {
        -moz-user-select:none; /*火狐*/
        -webkit-user-select:none; /*webkit浏览器*/
        -ms-user-select:none; /*IE10*/
        -khtml-user-select:none; /*早期浏览器*/
        user-select:none;
        .el-breadcrumb {
          height: 35px;
          line-height: 35px;
          padding:0px 20px;
          border: 1px solid #EEEEEE;
          display: flex;
          white-space: nowrap;
          overflow-x: scroll;
          /deep/ .el-breadcrumb__item {
            float: none;
          }
          /deep/ .el-breadcrumb__item:last-child{
            .el-breadcrumb__inner>a{
              color:#1677FF;
            }
          }
        }
        .el-breadcrumb::-webkit-scrollbar {
          width: 4px;
          height: 4px;
        }
        .el-breadcrumb::-webkit-scrollbar-track {
          border-radius: 5px;
          background-color: #ffffff;
        }
        .el-breadcrumb::-webkit-scrollbar-thumb {
          border-radius: 5px;
          background-color: rgb(173, 174, 177);
        }
      }
      .fileShow {
        display: flex;
        height: 309px;
        overflow-y: auto;
        flex-direction: column;
        justify-content: flex-start;
        align-items: flex-start;
        .fileItem {
          padding:5px 24px;
          box-sizing: border-box;
          border:1px solid transparent;
          border-bottom: 1px solid #EEEEEE;
          width: 100%;
          display: flex;
          align-items: center;
          -moz-user-select:none; /*火狐*/
          -webkit-user-select:none; /*webkit浏览器*/
          -ms-user-select:none; /*IE10*/
          -khtml-user-select:none; /*早期浏览器*/
          user-select:none;
          .title {
            margin-left: 10px;
            margin-right: 10px;
          }
        }
        .fileItem:hover {
          cursor: pointer;
        }
        .selectItem {
          box-sizing: border-box;
          border: 1px solid #eee;
          background-color:  #82b3f7;
          .title{
            color:#000;
          }
        }
      }
      .fileShow::-webkit-scrollbar {
        width: 0px;
        height: 0px;
      }
      .fileShow::-webkit-scrollbar-track {
        border-radius: 5px;
        background-color: #ffffff;
      }
      .fileShow::-webkit-scrollbar-thumb {
        border-radius: 5px;
        background-color: #bbb;
      }
    }
  }
}
</style>