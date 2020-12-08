<template>
  <div id="baseSearchCom">
    <div class="rightDiv headRight">
      <slot name="headRight"></slot>
    </div>
    <div class="form">
      <div
        v-for="(item, index) in formLabel"
        :key="index"
        class="formItem">
        <label
          class="formLabel"
          :style="{width: labelWidth ? `${labelWidth}px` : '', display: 'inline-block'}">{{item.label}}</label>
        <div :style="{display: 'inline-block', 'margin-right': '32px'}">
          <!-- 
            输入框
            {
              type: input,
              prop： 字段名,
              placeholder: 提示文字,//可选
            }
          -->
          <el-input 
            v-if="item.type === 'input'"
            :size="formSize" 
            width="320px"
            v-model="formData[item.prop]"  
            :placeholder="item.placeholder ? item.placeholder : '请输入'">
          </el-input>

          <!-- 
            日期范围选择
            {
              type: datePicker,
              prop： 字段名,
              format: 日期的格式,//可选，默认为yyyy-MM-dd
            }
          -->
          <template v-else-if="item.type === 'datePicker'">
            <el-date-picker
              v-model="formData[`${item.prop}_date`]"
              type="datetimerange"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              size="mini">
            </el-date-picker>
          </template>

          <!-- 
            日期
            {
              type: date,
              prop： 字段名,
              format: 日期的格式,//可选，默认为yyyy-MM-dd HH:mm:ss
            }
          -->
          <el-date-picker
            v-model="formData[item.prop]"
            v-else-if="item.type === 'date'"
            :type="item.itemType ? item.itemType : 'date'"
            :size="formSize"
            list-type='picture-card'
            :value-format="item.format ? item.format : 'yyyy-MM-dd HH:mm:ss'"
            placeholder="选择日期">
          </el-date-picker>
          
          <!-- 
            选择器 
            {
              type: select,
              prop： 字段名,
              placeholder: 提示文字,//可选
              options: 存放选项的数组，//label属性为选项显示的文字，value属性为选项对应的值
            }
          -->
          <el-select 
            v-else-if="item.type === 'select'" 
            v-model="formData[item.prop]"
            :placeholder="item.placeholder ? item.placeholder : '请选择'"
            class="selectItem"
            style="width: 320px"
            :size="formSize">
            <el-option
              v-for="itemInO in item.options"
              :key="itemInO.value"
              :label="itemInO.label"
              :value="itemInO.value">
            </el-option>
          </el-select>

          <!-- 
            选择省市区 
            {
              type: area,
              prop： 字段名,
              level: 值,//适用情况：省市区分开，取值：省->1，市->2，区->3
              limit: 值,//适用情况：省市区拼在一起，取值：省->1，省市->2，省市区->3，连接符由 data 中的 areaDelimiter 决定
              width: 选框宽度,
            }
          -->
          <template v-else-if="item.type === 'area'">
            <el-select 
              v-model="area.province" 
              placeholder="省"
              v-show="item.type === 'area' && showProvince(item)"
              :style="{width: item.width}"
              :size="formSize"
              @change="handleProvinceChange(item)">
              <el-option
                v-for="(pItm, pIdx) in provinceOptions"
                :key="pIdx"
                :label="pItm[areaLabelParams]"
                :value="pItm[areaValueParams]">
              </el-option>
            </el-select>
            <el-select 
              v-model="area.city" placeholder="市"
              v-show="item.type === 'area' && showCity(item)"
              :style="{width: item.width}"
              :size="formSize"
              @change="handleCityChange(item)">
              <el-option
                v-for="(cItm, cIdx) in cityOptions"
                :key="cIdx"
                :label="cItm[areaLabelParams]"
                :value="cItm[areaValueParams]">
              </el-option>
            </el-select>
            <el-select 
              v-model="area.area" placeholder="区"
              :style="{width: item.width}"
              :size="formSize"
              v-show="item.type === 'area' && showArea(item)">
              <el-option
                v-for="(aItm, aIdx) in areaOptions"
                :key="aIdx"
                :label="aItm[areaLabelParams]"
                :value="aItm[areaValueParams]">
              </el-option>
            </el-select>
          </template>
        </div>
      </div>
      <div class="formItem">
        <el-button class="btn" :size="formSize" type="primary" @click="searchData">查询</el-button>
        <el-button class="btn" :size="formSize" type="primary" @click="clearFormData(true)" v-if="showResetBtn" plain>重置</el-button>
      </div>
    </div>

    <div class="rightDiv">
      <slot name="footRight"></slot>
    </div>
    
  </div>
</template>
<script>
/**
 * 组件说明:
 *    参数： 
 *        1. 必传参数
 *            formLabel   Array       创建表单的数据
 *            getData     Function    获取数据的函数
 *            comData     Object      组件相关信息
 *        2. 可选参数
 *            labelWidth     String    表单label宽度       123px
 *            showResetBtn   Boolean   是否显示重置按钮     true
 *    其他：
 *      
 */
export default {
  data: function(){
    return {
      formData: {},
      provinceOptions: [
        ...this.$utils.dataArea
      ],
      cityOptions: [],
      areaOptions: [],
      area: {
        province: '',
        city: '',
        area: ''
      },
      areaFieldName: [],
    }
  },
  props: {
    formLabel:  {
      type: Array,
      default: function () {
        return []
      },
      required: true
    },// 创建表单的数据
    labelWidth: {
      type: Number,
      default: 60
    },//表单label宽度
    getData:  {
      type: Function,
      default: (data) => {},//data 存储请求需要传递的参数，包括了页码和条数
      required: true
    },// 获取数据
    comData: {
      type: Object,
      default: () => {
        return {
          id: "",//组件key
          type: "",//表单类型，create/update/detail
        }
      },
      required: true
    },//组件相关信息
    showResetBtn: {
      type: Boolean,
      default: true,
    },//是否显示重置按钮
    formSize: {
      type: String,
      default: 'mini',
    },//表单元素size
    areaLabelParams: {
      type: String,
      default: 'name',
    },//省市区选择器显示选项的属性名
    areaValueParams: {
      type: String,
      default: 'id',
    },//省市区选择器取值的属性名
  },
  created: function(){
    this.clearFormData();
  },
  watch:{
    area: {
      handler(newVal){
        if(this.areaFieldName.length === 0)
          return;
        if(this.areaFieldName.length > 1){
          this.areaFieldName.forEach((e, index) => {
            if(index === 0){
              this.formData[e] = newVal.province;
            }else if(index === 1){
              this.formData[e] = newVal.city;
            }else{
              this.formData[e] = newVal.area;
            }
          })
        }else{
          //设置省市区拼接方式
          let arr = Object.values(newVal).filter(e => {
            return e !== "";
          })
          this.formData[this.areaFieldName] = arr.join('/');
        }
      },
      deep: true,
    },
    $route(newVal, oldVal){
      if(newVal.path != oldVal.path)
        this.clearFormData();
    }
  },
  methods: {
    //根据表单元素的类型控制宽度
    // formItemWidth(item){
    //   if(item.type === 'datePicker' || (item.type === 'area' && item.limit > 1)){
    //     return '50%';
    //   }else{
    //     return '25%';
    //   }
    // },
    //是否显示省
    showProvince(item){
      return  item.level === 1 || item.limit;
    },
    //是否显示市
    showCity(item){
      return  item.level === 2 || item.limit > 1;
    },
    //是否显示区
    showArea(item){
      return  item.level === 3 || item.limit === 3;
    },
    //选择市的处理函数
    handleCityChange(item){
      this.areaOptions = [];
      this.cityOptions.forEach((cItm) => {
        if(cItm[this.areaValueParams] === this.area.city){
          this.areaOptions = cItm.children || [];
        }
      })
      this.formLabel.forEach(e => {
        if((e.type === 'area' && e.level === 3) || (e.type === 'area' && e.limit > 2)){
          this.formData[e.prop] = this.cityOptions[0][this.areaValueParams];;
          this.area.area = this.areaOptions[0][this.areaValueParams];
        }
      });
    },
    //选择省的处理函数
    handleProvinceChange(item){
      this.cityOptions = [];
      this.areaOptions = [];
      this.provinceOptions.forEach((pItm) => {
        if(pItm[this.areaValueParams] === this.area.province) {
          this.cityOptions = pItm.children || [];
        }
      })
      this.formLabel.forEach(e => {
        if((e.type === 'area' && e.level === 2) || (e.type === 'area' && e.limit > 1)){
          this.formData[e.prop] = this.cityOptions[0][this.areaValueParams];
          this.area.city = this.cityOptions[0][this.areaValueParams];
          this.handleCityChange()
        }
      });
    },
    //重置表单
    clearFormData(flag){
      this.formData = {};
      this.areaFieldName = [];
      this.formLabel.forEach(e => {
        if(e.type === 'area'){
          //处理省市区
          if(this.areaFieldName.length > 3){
            console.error(`组件baseSearchCom有关省市区的配置出错？父组件传入comDataId为${this.comData.id}`)
          }
          if(e.level){
            if(typeof e.level !== 'number')
              console.error(`组件baseSearchCom有关省市区的配置出错？父组件传入comDataId为${this.comData.id}，level类型应为number`)
            switch(e.level){
              case 1: 
                this.areaFieldName[0] = e.prop;
                break;
              case 2: 
                this.areaFieldName[1] = e.prop;
                break;
              case 3: 
                this.areaFieldName[2] = e.prop;
                break;
            }
          }else if(e.limit){
            this.areaFieldName.push(e.prop)
          }else{
            console.error(`组件baseSearchCom有关省市区的配置出错？父组件传入comDataId为${this.comData.id}，既没有配置level，也没有配置limit`)
          }
        }else if(e.type === 'datePicker'){
          //处理日期
          this.$set(this.formData, `${e.prop}_date`, []);
          // this.$set(this.formData, this.dateParamName(e.prop, 'from'), null);
          // this.$set(this.formData, this.dateParamName(e.prop, 'to'), null);
        }
        this.$set(this.formData, e.prop, null);
      })
      this.area = {
        province: '',
        city: '',
        area: ''
      }
      let data = {
        data: {},
        pageNo: 0,
        pageSize: 10,
      };
      sessionStorage.setItem(`${this.comData.id}Page`, JSON.stringify(data));
      if(flag){
        this.searchData()
      }
    },
    searchData(){
      //校验日期的选择
      for(let i=0; i < this.formLabel.length; i++){
        if(this.formLabel[i].type === 'datePicker'&&this.formData[`${this.formLabel[i].prop}_date`]){
          if(!this.formData[`${this.formLabel[i].prop}_date`][0]
          && this.formData[`${this.formLabel[i].prop}_date`][1]){
            this.$message.error(`请选择${this.formLabel[i].label}起始日期`);
            return;
          }else if(!this.formData[`${this.formLabel[i].prop}_date`][1]
          && this.formData[`${this.formLabel[i].prop}_date`][0]){
            this.$message.error(`请选择${this.formLabel[i].label}结束日期`);
            return;
          }else if(this.formData[`${this.formLabel[i].prop}_date`][0]
          && this.formData[`${this.formLabel[i].prop}_date`][0] > this.formData[`${this.formLabel[i].prop}_date`][1]){
            this.$message.error(`${this.formLabel[i].label}所选的起始日期应小于或等于结束日期`);
            return;
          }else{
            this.formData[`${this.formLabel[i].prop}Start`] = this.formData[`${this.formLabel[i].prop}_date`][0];
            this.formData[`${this.formLabel[i].prop}End`] = this.formData[`${this.formLabel[i].prop}_date`][1];
            delete this.formData[`${this.formLabel[i].prop}End`];
          }
        }
      }
      //设置查询条件，页码和条数
      let data = {
        data: this.$utils.funcData.handleObjParams(this.formData),
        pageNo: 0,
        pageSize: 10,
      }
      sessionStorage.setItem(`${this.comData.id}Page`, JSON.stringify(data));
      this.getData(data);
    },
    // 日期范围拼接规则
    // dateParamName(prop, type){
    //   if(type === 'from'){//开始时间
    //     return `${prop}Start`;
    //   }else{//结束时间
    //     return `${prop}End`;
    //   }
    // }
  }
}
</script>
<style lang="less" scoped>
#baseSearchCom{
  background: #fff;
  overflow: hidden;
  margin-bottom: 10px;
  padding-bottom: 10px;
  .form{
    margin-top: 10px;
  }
  .el-form-item{
    margin-bottom: 2px !important;
    margin-right: 0 !important;
  }
  .btn{
    margin-right: 8px;
  }
  .el-button+.el-button{
    margin-left: 0;
  }
  .rightDiv{
    float: right;
  }
  .headRight{
    float: right;
  }
}
#baseSearchCom{
  .form{
    .formLabel{
      line-height: 35px;
      // text-align: right;
      vertical-align: middle;
      font-size: 14px;
      color: #666;
      margin-right: 5px;
      // padding-left: 32px;
    }
    .formItem{
      display: inline-block;
    }
  }
  .el-date-editor--datetimerange.el-input, .el-date-editor--datetimerange.el-input__inner, .el-input{
    width: 320px;
  }
}
</style>
<style lang="less">
#baseSearchCom{
  .el-input--suffix .el-input__inner{
    padding-right: 0;
  }
  .el-form-item__label, .el-form-item__content{
    line-height: 35px;
    width: 100%;
  }
}
</style>