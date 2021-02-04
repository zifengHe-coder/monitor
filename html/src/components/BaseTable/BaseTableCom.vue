<template>
  <div id="baseTableCom">
    <div class="tableTop">
      <slot name="headLeft"></slot>
      <div style="float: right;display: inline-block;">
        <slot name="headRight"></slot>
      </div>
    </div>
    <div class="tableDiv" :style="tableStyle">
      <el-table
        header-row-class-name="tableHeader"
        :data="listData"
        fit
        height="100%"
        :border="tableBorder"
        v-if="tableOnff"
        @selection-change="handleSelect"
        stripe
        :key="itemKey"
        :cell-style="handleCellColor"
        size="mini"
      >
        <!-- 勾选数据的列 -->
        <el-table-column
          v-if="canSelect"
          type="selection"
          :class-name="hasRowHeaderExpand ? 'hasRowHeaderExpandPaddingTop' : ''"
          :selectable="checkSelectable"
          min-width="55"
        >
        </el-table-column>
        <el-table-column
          v-else-if="hadIndex"
          label="序号"
          type="index"
          :index="1"
          width="55"
        >
        </el-table-column>
        <el-table-column
          type="expand"
          v-if="isExpand"
          :class-name="hasRowHeaderExpand ? 'hasRowHeaderExpandPaddingTop' : ''"
        >
          <template v-slot="data">
            <slot name="rowExpand" :scope="data"></slot>
          </template>
        </el-table-column>

        <el-table-column
          v-for="(item, index) in listHeader"
          :sort-method="(...param) => sortList(item, ...param)"
          :class-name="hasRowHeaderExpand ? 'hasRowHeaderExpandPaddingTop' : ''"
          :key="index"
          :prop="item.prop"
          :label="item.label"
          :min-width="item.minWidth"
          :width="item.width"
          :align="item.textRight || 'left'"
          :show-overflow-tooltip="item.type === 'word' && !hasRowHeaderExpand"
        >
          <template v-slot="scope">
            <div>
              <!-- 行的头部 -->
              <div
                class="rowHeader"
                v-if="hasRowHeaderExpand && index === 0"
                :style="{ left: rowHeaderStyleLeft }"
              >
                <slot name="rowHeader" :scope="scope"></slot>
              </div>

              <!-- 文本 -->
              <p
                v-if="item.type === 'word' || item.columnOperable === 'none'"
                slot="reference"
                v-text="getCellWord(scope, item)"
                class="word"
              >
                
              </p>

              <!-- 特殊元素 -->
              <slot
                v-else-if="item.isSpecial"
                :name="item.prop"
                :scope="scope"
              ></slot>

              <!-- 图片 -->
              <div v-else-if="item.type === 'image'">
                <img
                  v-if="handleRowImage(scope.row,item.prop)"
                  :src="handleRowImage(scope.row,item.prop)"
                  style="vertical-align: middle;height: 18px;width: 18px;"
                />
                <span
                  style="margin-left:5px"
                >{{handleRowText(scope.row,item.prop)}}</span>
              </div>

              <!-- 普通icon图片 -->
              <div v-else-if="item.type === 'normalImage'">
                <span
                  style="margin-left:5px"
                >{{getCellWord(scope, item)}}</span>
                <el-popover
                  placement="top-start"
                  width="300"
                  trigger="hover"
                  v-if="scope.row.showIcon"
                  >
                  <p>可能原因1：进程做了自我注入，这是一种防止外部注入的防御措施，该措施会导致进程无法注入监听。</p>
                  <p>可能原因2：进程完整性级别为不可信级或应用容器级，无法加载监听功能。完整性级别是Windows系统的安全机制。运行于不可信级或应用容器级的进程，Windows系统将限制其访问资源能力，因而无法正常加载监控功能。</p>
                  <img
                    slot="reference"
                    v-if="scope.row.showIcon"
                    :src="item.icon"
                    style="vertical-align: middle;height: 18px;width: 18px;"
                  />
                </el-popover>
              </div>

              <!-- 其他表单元素 -->
              <el-popover
                v-else-if="item.columnOperable === 'part'"
                trigger="click"
                placement="top"
                @hide="changeCellStatus(scope, index, false)"
              >
                <BaseFormCom
                  :labels="formLabelsArr[scope.$index][index]['formLabels']"
                  :comData="formLabelsArr[scope.$index][index]['comData']"
                  :originFormData="formLabelsArr[scope.$index][index]['data']"
                  :isShowSumbitBtn="false"
                  :isShowLabel="false"
                >
                </BaseFormCom>
                <div style="text-align: right;margin-top: 10px;">
                  <el-button
                    v-if="
                      formLabelsArr[scope.$index][index]['comData']['type'] ===
                        'detail'
                    "
                    type="primary"
                    size="mini"
                    @click="changeCellStatus(scope, index, true)"
                  >
                    修改
                  </el-button>
                  <div v-else>
                    <el-button
                      type="primary"
                      size="mini"
                      @click="
                        saveCellChange(formLabelsArr[scope.$index][index])
                      "
                    >
                      保存
                    </el-button>
                  </div>
                </div>
                <p
                  slot="reference"
                  v-text="getCellWord(scope, item)"
                  class="word"
                ></p>
              </el-popover>

              <BaseFormCom
                v-else
                :labels="formLabelsArr[scope.$index][index]['formLabels']"
                :comData="formLabelsArr[scope.$index][index]['comData']"
                :originFormData="scope.row"
                :isShowSumbitBtn="false"
                :isShowLabel="false"
              >
              </BaseFormCom>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          align="center"
          type="operating"
          v-if="hasOperationBtn"
          label="操作"
          :class-name="hasRowHeaderExpand ? 'hasRowHeaderExpandPaddingTop' : ''"
          :width="operationWidth"
        >
          <template v-slot="scope">
            <slot name="operationBtn" :scope="scope"></slot>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="list-pagination" v-if="showPage">
      <el-pagination
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="sizes"
        :page-size="pageSize"
        layout="total, prev, pager, next, sizes, jumper"
        :total="totalItems"
      >
      </el-pagination>
    </div>

    <el-dialog title="图片" :visible.sync="dialogImgVisible">
      <img :src="dialogImgUrl" style="width: 100%;" />
    </el-dialog>
  </div>
</template>
<script>
/**
 * 说明：
 * 1. 对表格数据进行选择
 *    1）canSelect 设置为 true
 *    2）handleDataSelect 处理选择数据的函数
 */
export default {
  props: {
    itemKey: {
      type: Number,
      required: 0
    },
    tableData: {
      type: Array,
      default: () => [],
      required: true
    }, // table 数据
    tableHeader: {
      type: Array,
      default: () => [],
      required: true
    }, // table 表头
    canSelect: {
      type: Boolean,
      default: false
    }, // 是否有全选
    hasOperationBtn: {
      type: Boolean,
      default: false
    }, // 是否有操作的按钮
    comData: {
      type: Object,
      default: () => {},
      required: true
    }, // 记录父组件相关信息
    getTableData: {
      type: Function,
      default: () => {},
      required: false
    }, // 获取table数据
    handleDataSelect: {
      type: Function,
      default: () => {}
    }, // 选择数据后的处理函数
    totalItems: {
      type: Number,
      default: 0
    }, //数据总量
    tableStyle: {
      type: Object,
      default: () => {}
    }, //table高度
    operationWidth: {
      type: Number,
      default: 150
    }, //操作栏宽度
    showPage: {
      type: Boolean,
      default: true
    }, //是否显示分页
    checkSelectable: {
      type: Function,
      default: () => true
    }, //控制table的行是否可选，默认全部可选
    formSize: {
      type: String,
      default: "mini"
    }, //控制表单元素的size
    tableBorder: {
      type: Boolean,
      default: false
    }, //表格的border属性值
    canChangeCell: {
      type: Boolean,
      default: true
    }, //是否可以通过单元格修改值
    handleSaveCellChange: {
      type: Function,
      default: (data, resolve, reject) => {}
    }, // 保存单元格的修改
    isExpand: {
      type: Boolean,
      default: false
    }, //表格是否有扩展行
    hasRowHeaderExpand: {
      type: Boolean,
      default: false
    }, //表格是否有行的头部的扩展
    hadIndex: {
      type: Boolean,
      default: false
    } //是否有序号
  },
  data: function() {
    return {
      sizes: [10, 20, 30, 40, 50], //页码数规格
      currentPage: 1, //当前页
      pageSize: 10, //页面条数
      tableOnff: true,
      listData: [], //列表数据
      listHeader: [], //列表头
      dialogImgVisible: false, //图片放大弹窗的显现控制
      dialogImgUrl: "", //图片url
      comDataForm: {
        id: "BaseTableCom"
      }, //组件相关数据
      formLabelsArr: [] //存放可操作表单元素的数组
    };
  },
  created: function() {
    if (this.showPage) {
      sessionStorage.setItem(
        `${this.comData.id}Page`,
        JSON.stringify({
          data: {},
          pageNo: 0,
          pageSize: 10
        })
      );
    }
    this.handleCurrentChange(0);
  },
  watch: {
    tableHeader: {
      handler(newVal) {
        this.tableOnff = false;
        this.listHeader = [];
        newVal.forEach(item => {
          this.listHeader.push(item);
        });
        this.$nextTick(() => {
          this.tableOnff = true;
        });
      },
      immediate: true
    },
    tableData: {
      handler(newVal) {
        this.listData = [];
        newVal.forEach(item => {
          this.listData.push(item);
        });
        let data = JSON.parse(sessionStorage.getItem(`${this.comData.id}Page`));
        // TOTEST: 页面数据为空且不是第一页时，返回前一页
        if (this.listData.length === 0 && data && data.pageNo !== 0) {
          this.handleCurrentChange(data.pageNo);
          return;
        }
        if (data) {
          this.pageSize = data.pageSize ? data.pageSize : 10;
          this.currentPage = data.pageNo ? data.pageNo + 1 : 0;
        }
        //处理table中的元素
        if (this.formLabelsArr.length === 0) {
          this.handleFormLabels();
        }
      },
      immediate: true
    }
  },
  computed: {
    rowHeaderStyleLeft() {
      let len = 0;
      if (this.isExpand) len += 48;
      if (this.canSelect) len += 48;
      return `-${len}px`;
    }
  },
  methods: {
    // 处理格仔中的文本
    handleRowText(row,prop){
      if(row[prop]){
        let text = row[prop].split('/exe/')[0];
        return text;
      }
    },
    // 处理格仔中的图标
    handleRowImage(row,prop){
      if(row[prop]){
        let img = row[prop].split('/exe/')[1];
        return img;
      }
    },
    //保存已修改的单元格的数据
    saveCellChange(data) {
      //TOTEST: 保存单元格的修改后，popover是否关闭？是否更新列表
      new Promise((resolve, reject) => {
        this.handleSaveCellChange(data, resolve, reject);
      }).then(res => {
        this.handleCurrentChange(this.currentPage);
      });
    },
    //获取单元格应该显示的文本
    getCellWord(data, columnConfig) {
      if (
        columnConfig.type === "select" ||
        columnConfig.type === "radio" ||
        columnConfig.type === "switch"
      ) {
        return this.$utils.funcData.isNullOrUndefined(
          data.row[columnConfig.prop]
        )
          ? "--"
          : this.$utils.funcData.getLabelFromArr(
              columnConfig.options,
              data.row[columnConfig.prop]
            );
      } else if (columnConfig.type === "timestamp") {
        let date = new Date(data.row[columnConfig.prop]);
        date = `${date.getFullYear()}-${date.getMonth() +
          1}-${date.getDate()} ${String(date.getHours()).padStart(
          2,
          "0"
        )}:${String(date.getMinutes()).padStart(2, "0")}:${String(
          date.getSeconds()
        ).padStart(2, "0")}`;
        return this.$utils.funcData.isNullOrUndefined(
          data.row[columnConfig.prop]
        )
          ? "--"
          : date;
      } else if (columnConfig.type === "image") {
        return this.$utils.funcData.isNullOrUndefined(
          data.row[columnConfig.prop]
        )
          ? "--"
          : data.row[columnConfig.prop]
              .split("/")
              .pop()
              .split(".")[0] + ".exe";
      } else {
        return this.$utils.funcData.isNullOrUndefined(
          data.row[columnConfig.prop]
        )
          ? "--"
          : data.row[columnConfig.prop];
      }
    },
    //修改单元格的状态
    changeCellStatus(data, index, statusValue) {
      if (statusValue) {
        this.formLabelsArr[data.$index][index]["comData"]["type"] = "update";
      } else {
        this.formLabelsArr[data.$index][index]["comData"]["type"] = "detail";
      }
    },
    //处理table中的表单元素
    handleFormLabels() {
      for (let i = 0; i < this.listData.length; ++i) {
        let arr = [];
        this.tableHeader.forEach((itemInTH, indexInTH) => {
          let obj = {
            comData: {
              id: `${this.comDataForm.id}${i}-${indexInTH}`
            },
            formLabels: new Array(JSON.parse(JSON.stringify(itemInTH)))
          };
          let excludeFormItemName = ["image", "word"];
          if (!excludeFormItemName.includes(itemInTH.type)) {
            //通过表单元素的columnOperable属性处理单元格中表单元素的状态
            switch (itemInTH.columnOperable) {
              case "part":
                obj.comData.type = "detail";
                obj.comData.canChange = true;
                obj.data = JSON.parse(JSON.stringify(this.listData[i]));
                break;
              case "all":
                obj.comData.type = "update";
                obj.comData.canChange = false;
                break;
            }
            //处理表单元素的fns属性
            obj.formLabels[0].fns = this.handleFn(
              itemInTH,
              this.listData[i],
              i,
              indexInTH
            );
          }
          arr.push(obj);
        });
        this.formLabelsArr.push(arr);
      }
    },
    //使用弹窗放大略缩图
    zoomImg(url) {
      this.dialogImgUrl = url;
      this.dialogImgVisible = true;
    },
    //处理table中表单元素的监听函数
    handleFn(formItemConfig, data, rowIndex, columnIndex) {
      if (!formItemConfig.fns) return null;
      let fns = {};
      //绑定传入的参数
      Object.keys(formItemConfig.fns).forEach(itemInF => {
        fns[itemInF] = value => {
          formItemConfig.fns[itemInF].call(
            this,
            value,
            {
              rowIndex,
              columnIndex,
              ...formItemConfig
            },
            data
          );
        };
      });
      return fns;
    },
    //选择数据的回调函数
    handleSelect: function(selection) {
      this.handleDataSelect(selection);
    },
    // 根据传入的值调整单元格字体颜色
    handleCellColor: function(data) {
      let specialColumn = ["selection", "operating", "expand", "rowExpand"];
      if (specialColumn.includes(data.column.type)) return;
      let index =
        data.columnIndex - (this.canSelect ? 1 : 0) - (this.isExpand ? 1 : 0);
      if (
        this.$utils.funcData.isEmpty(this.tableHeader[index]) ||
        this.$utils.funcData.isEmpty(this.tableHeader[index]["colorAndVal"])
      )
        return;
      let val = Object.values(data.row)[index];
      let resultColor = this.tableHeader[index]["colorAndVal"][val]
        ? this.tableHeader[index]["colorAndVal"][val]
        : null;
      return resultColor ? `color: ${resultColor}` : "";
    },
    //页码改变执行的函数
    handleSizeChange(pageSize) {
      let pageData = {};
      if (this.showPage) {
        pageData = JSON.parse(sessionStorage.getItem(`${this.comData.id}Page`));
        pageData.pageSize = pageSize;
        sessionStorage.setItem(
          `${this.comData.id}Page`,
          JSON.stringify(pageData)
        );
      }
      this.getTableData(pageData);
    },
    //当前页改变执行的函数
    handleCurrentChange(curPage) {
      let pageData = {};
      if (this.showPage) {
        pageData = JSON.parse(sessionStorage.getItem(`${this.comData.id}Page`));
        if (curPage == 0) {
          pageData.pageNo = curPage;
        } else {
          pageData.pageNo = curPage - 1;
        }
        sessionStorage.setItem(
          `${this.comData.id}Page`,
          JSON.stringify(pageData)
        );
      }
      this.getTableData(pageData);
    },
    //排序处理函数
    //首先会将传入数据转为Number类型，若为NaN，则对比字符串的charCodeAt，若为Number，则用来比较大小
    sortList(item, param1, param2) {
      if (!this.$utils.funcData.isNaN(Number(param1[item.prop]))) {
        return Number(param1[item.prop]) - Number(param2[item.prop]);
      } else {
        let arr1 = String(param1)
          .split("")
          .map(e => {
            return String(e.charCodeAt());
          });
        let arr2 = String(param1)
          .split("")
          .map(e => {
            return String(e.charCodeAt());
          });
        return arr1.join("") > arr2.join("") ? 1 : -1;
      }
    }
  }
};
</script>
<style lang="less" scoped>
#baseTableCom {
  .tableDiv {
    background: #fff;
    padding-top: 20px;
  }
  .pageDiv {
    text-align: center;
    margin-top: 24px;
    margin-bottom: 38px;
  }
  .tableTop {
    overflow: hidden;
  }
  p {
    margin: 0;
    padding: 0;
  }
}
.list-pagination {
  width: 100%;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
}
</style>
<style lang="less">
#baseTableCom {
  .el-icon-success {
    color: @iconSuccessColor;
  }
  .el-icon-warning {
    color: @iconWarningColor;
  }
  .el-table td {
    padding: 0;
  }
  .el-table .cell {
    line-height: 32px;
    button{
      margin-left: 10px!important;
      padding: 5px 8px;
    }
  }
  .word {
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
    word-break: break-all;
    // margin: 0;
  }
  #baseFormCom .baseFormDetail .el-form-item {
    margin-top: 0;
  }
  .el-form-item {
    margin-bottom: 0 !important;
  }
  .rowHeader {
    overflow: hidden;
    position: absolute;
    left: 0;
    top: 0;
    width: 80vw;
  }
  .el-table--striped .el-table__body tr.el-table__row--striped td,
  .el-table--enable-row-transition .el-table__body td {
    background: rgba(0, 0, 0, 0) !important;
  }
  .el-table__row--striped:nth-child(even) {
    background: #fafafa;
  }
  .hasRowHeaderExpandPaddingTop {
    padding-top: 20px !important; //每行扩展头部的高度
  }
}
.el-table th {
  background: #fafafa !important;
  // padding: 0;
}
.el-table thead {
  color: #666666;
}
</style>
