<template>
  <div id="baseFormCom">
    <!-- 显示详情的表单 -->
    <el-form 
      v-if="comData.type === 'detail'"
      class="baseFormDetail"
      :model="formData">
      <el-form-item
        v-for="(item, index) in formLabel" 
        :label="isShowLabel ? item.label : null" 
        :key="item.prop + index" 
        :style="{'width': `${item.widthNum ? item.widthNum * 25 : 4 * 25}%`, 'display': 'inline-block'}"
        :prop="item.prop">

        <!-- 上传图片 -->
        <span v-if="item.type === 'photoUpload'">
          <img 
            v-for="(itemPhoto, index) in formData[item.prop]"
            :key="index"
            :src="itemPhoto.url"
            href="加载失败"
            style="width: 250px; height: 250px;margin-top: 5px"/>
        </span>

        <!-- 富文本编辑器 -->
        <span 
          v-else-if="item.type === 'richTextEditor'" 
          class="richTextEditorDetail" 
          v-html="formData[item.prop]"></span>

        <!-- 有options属性的表单元素 -->
        <span 
          class="word"
          v-else-if="item.type === 'select' || item.type === 'radio' || item.type === 'switch'">
          {{$utils.funcData.isNullOrUndefined(formData[item.prop]) ? '--' : getValueFromOptions(item.options, formData[item.prop])}}
        </span>

        <!-- 其他元素 -->
        <span v-else class="word">
          {{$utils.funcData.isNullOrUndefined(formData[item.prop]) ? '--' : formData[item.prop]}}
        </span>

      </el-form-item>
    </el-form>

    <!-- 可操作值的表单 -->
    <el-form 
      v-else
      class="baseForm"
      ref="baseForm" 
      :model="formData"
      :rules="rules"
      :label-width="isShowLabel ? labelWidth : null">
      <el-form-item 
        v-for="(item, index) in formLabel" 
        :label="isShowLabel ? item.label : null"
        :key="item.prop + index" 
        :prop="getPropsValue(item)">

        <!-- 特殊表单元素 -->
        <template v-if="item.isSpecial" :item="item">
          <slot :name="item.prop" :itemData="item" :formData="formData"></slot>
        </template>

        <!-- 文本 word -->
        <span
          class="word"
          v-else-if="item.type === 'word'">
          {{ formData[item.prop] }}
        </span>

        <!-- 输入框 input -->
        <el-input 
          v-else-if="item.type === 'input'" 
          v-on="item.fns"
          v-model="formData[item.prop]"
          :disabled="item.disabled"
          :size="formSize"
          :clearable="item.clearable ? item.clearable : false"
          :placeholder="item.placeholder ? item.placeholder : '请输入'"></el-input>


        <!-- 输入框带搜索建议 autocomplete -->
        <el-autocomplete
          v-else-if="item.type === 'autocomplete'" 
          :size="formSize"
          v-on="item.fns"
          style="width: 100%"
          v-model="formData[`${item.prop}Label`]"
          :fetch-suggestions="(queryString, cb) => querySearch(queryString, cb, item.options)"
          :placeholder="item.placeholder ? item.placeholder : '请输入'"
          :trigger-on-focus="false"
          @select="(selectItem) => autocompleteSelect(selectItem, item)">
          <template slot-scope="{ item }">
            <div class="name">{{ item.label }}</div>
          </template>
        </el-autocomplete>

        <!-- 多行文本 textarea -->
        <el-input 
          v-else-if="item.type === 'textarea'" 
          v-model="formData[item.prop]"
          type="textarea"
          :size="formSize"
          v-on="item.fns"
          :rows="item.rows ? item.rows : 2"
          :placeholder="item.placeholder ? item.placeholder : '请输入'"></el-input>

        <!-- 密码输入框 password -->
        <el-input 
          v-else-if="item.type === 'password'" 
          v-model="formData[item.prop]"
          style="width: 100%"
          :size="formSize"
          v-on="item.fns"
          type="password"
          :placeholder="item.placeholder ? item.placeholder : '请输入'"></el-input>
        
        <!-- 选择器 select -->
        <el-select 
          clearable
          v-else-if="item.type === 'select'" 
          v-model="formData[item.prop]" 
          style="width: 100%"
          v-on="item.fns"
          :size="formSize"
          :placeholder="item.placeholder ? item.placeholder : '请选择'">
          <el-option 
            v-for="item2 in item.options" 
            :key="item2.value"
            :label="item2.label" 
            :value="item2.value"></el-option>
        </el-select>

        <!-- 开关 switch -->
        <div v-else-if="item.type === 'switch'">
          <el-switch
            active-color="#155ee3"
            inactive-color="#cccccc" 
            v-on="item.fns"
            v-model="formData[item.prop]">
          </el-switch>
          <span v-if="item.showWord">{{getValueFromOptions(item.options, formData[item.prop])}}</span>
        </div>

        <!-- 单选框 radio -->
        <el-radio-group
          v-else-if="item.type === 'radio'" 
          v-model="formData[item.prop]" 
          v-on="item.fns"
          style="width: 100%"
          :size="formSize">
          <el-radio
            v-for="item2 in item.options" 
            :key="item2.value" 
            :label="item2.value">{{item2.label}}</el-radio>
        </el-radio-group>

        <!-- 多选框 checkbox -->
        <el-checkbox-group
          v-else-if="item.type === 'checkbox'" 
          v-model="formData[item.prop]"
          v-on="item.fns"
          style="width: 100%"
          :size="formSize">
          <el-checkbox 
            v-for="itemInO in item.options" 
            :key="itemInO.label" 
            :label="itemInO.value" >
            {{itemInO.label}}
          </el-checkbox>
        </el-checkbox-group>

        <!-- 日期 date -->
        <el-date-picker 
          v-else-if="item.type === 'date'" 
          v-model="formData[item.prop]"
          v-on="item.fns"
          style="width: 100%"
          :size="formSize"
          :format="item.format ? item.format : 'yyyy-MM-dd'"
          :placeholder="item.placeholder ? item.placeholder : '请选择'"
          :value-format="item.format ? item.format : 'yyyy-MM-dd'"></el-date-picker>
          
        <!-- 日期选择 datePicker -->
        <el-date-picker
          v-model="formData[item.prop]"
          v-else-if="item.type === 'datePicker'"
          :type="item.itemType ? item.itemType : 'datetimerange'"
          v-on="item.fns"
          style="width: 100%"
          :size="formSize"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :placeholder="item.placeholder ? item.placeholder : '请选择'"
          :value-format="item.format ? item.format : 'yyyy-MM-dd HH:mm:ss'">
        </el-date-picker>

        <!-- 图片上传 photoUpload -->
        <!-- 上传一张图片，换图直接替换 -->
        <ul
          class="photoUpload1Ul"
          v-else-if="item.type === 'photoUpload' && item.limit === 1">
          <li class="photoUpload1Li">
            <div class="photoUpload1Div1">
              <el-upload
                v-if="fileObj[item.prop].length === 0"
                :action="item.action ? item.action : '#'"
                :show-file-list="false"
                :before-upload="(file) => handlePhotoBeforeUpload(file, item, 1, 'default')"
                :file-list="fileObj[item.prop]">
                <span class="defaultImg">
                  <i class="el-icon-plus defaultImg-icon"></i>
                </span>
              </el-upload>
              <div v-else class="photoUpload1Div2" v-for="(itemPhoto, index) in fileObj[item.prop]" :key="index">
                <img :src="itemPhoto.url" class="" />
                <span class="photoUpload1Actions" v-if="item.operations.length > 0">
                  <el-upload
                    :action="item.action ? item.action : '#'"
                    :show-file-list="false"
                    :before-upload="(file) => handlePhotoBeforeUpload(file, item, 'onphoto', index)"
                    :file-list="fileObj[item.prop]"
                    style="display: inline-block;margin-right: 28px;">
                    <span
                      class="photoUpload1ActionsUpload">
                      <i class="el-icon-upload2"></i>
                    </span>
                  </el-upload>
                  <span
                    v-if="item.operations.includes('delete')"
                    class="photoUpload1Actions-delete"
                    @click="handlePhotoRemove(itemPhoto, item)">
                    <i class="el-icon-delete"></i>
                  </span>
                </span>
              </div>
            </div>
          </li>
        </ul>
        <!-- 上传多张图片 -->
        <el-upload
          class="avatar-uploader"
          :action="item.action ? item.action : '#'"
          multiple
          v-else-if="item.type === 'photoUpload' && item.limit > 1"
          list-type="picture-card"
          :on-exceed="handleTooManyPhoto"
          :before-upload="(file) => handlePhotoBeforeUpload(file, item, 'default')"
          :file-list="fileObj[item.prop]"
          :limit="item.limit ? item.limit : 2">
            <i class="el-icon-plus"></i>
            <div slot="file" slot-scope="{file}">
              <img
                class="el-upload-list__item-thumbnail"
                :src="file.url" alt="">
              <span class="el-upload-list__item-actions" v-if="item.operations.length > 0">
                <span
                  class="el-upload-list__item-preview"
                  v-if="item.operations.includes('zoom')"
                  @click="handlePictureCardPreview(file, item.prop)">
                  <i class="el-icon-zoom-in"></i>
                </span>
                <span
                  v-if="item.limit === 1"
                  class="el-icon-upload"></span>
                <span
                  v-if="item.operations.includes('delete')"
                  class="el-upload-list__item-delete"
                  @click="handlePhotoRemove(file, item.prop)">
                  <i class="el-icon-delete"></i>
                </span>
              </span>
            </div>
        </el-upload>

        <!-- 富文本 richTextEditor -->
        <BaseRichTextEditorCom 
          v-else-if="item.type === 'richTextEditor'"
          :editorKey="`baseFormRC_${comData.id}`"
          :content="formData[item.prop]"
          :ref="`baseFormRC_${comData.id}`"
          style="width: 100%;margin-top: 5px;">
        </BaseRichTextEditorCom>

        <!-- 选择省市区 area -->
        <!-- <areaPicker v-else-if="item.type === 'area'" :limit='item.limit' v-model="areaSelect"></areaPicker> -->
        <el-select 
          v-model="area.province" 
          placeholder="省"
          v-if="item.type === 'area' && showProvince(item)"
          :style="{width: item.width}"
          :size="formSize"
          @change="handleProvinceChange(item)">
          <el-option
            v-for="(pItm, pIdx) in provinceOptions"
            :key="pIdx"
            :label="pItm[item.labelParamName]"
            :value="pItm[item.valueParamName]">
          </el-option>
        </el-select>
        <el-select 
          v-model="area.city" 
          placeholder="市"
          v-if="item.type === 'area' && showCity(item)"
          :style="{width: item.width}"
          :size="formSize"
          @change="handleCityChange(item)">
          <el-option
            v-for="(cItm, cIdx) in cityOptions"
            :key="cIdx"
            :label="cItm[item.labelParamName]"
            :value="cItm[item.valueParamName]">
          </el-option>
        </el-select>
        <el-select 
          v-model="area.area" 
          placeholder="区"
          :style="{width: item.width}"
          :size="formSize"
          v-if="item.type === 'area' && showArea(item)">
          <el-option
            v-for="(aItm, aIdx) in areaOptions"
            :key="aIdx"
            :label="aItm[item.labelParamName]"
            :value="aItm[item.valueParamName]">
          </el-option>
        </el-select>

      </el-form-item>
      <el-form-item v-if="isShowSumbitBtn">
        <el-button type="primary" class="sumbitBtn" @click="handleSubmit">
          <slot name="sumbitBtn">提交</slot>
        </el-button>
      </el-form-item>
    </el-form>
    <el-dialog v-if="needImgDialog" :visible.sync="dialogVisible">
      <img width="100%" :src="dialogImageUrl" alt="">
    </el-dialog>
  </div>
</template>
<script>
/**
 * 说明：
 *  1. props 中的 comData：
 *      值： {
 *              id: 值, //将被用作存储组件有关数据的key，及输出错误时可协作寻找父组件的值，尽量唯一
 *              type: 值, //用来区分表单类型的值，取值：create/update/detail
 *           }
 *  2. 添加新的表单元素时，新的表单元素需要放在“选择省市区”之前，使用 v-else-if
 */

export default {
  name: 'BaseFormCom',
  props: {
    labels:{
      type: Array,
      default: () => [],
      required: true
    },//决定表单元素的数组
    formRule: {
      type: Object,
      default: () => {}
    },//表单校验规则
    handleCompleteData: {
      type: Function,
      default: (formData, comDataType, comDataId) => {},
    },//数据校验通过后的处理函数
    handleIncompleteData: {
      type: Function,
      default: (formData, comData) => {},
    },//数据不校验通过后的处理函数
    labelWidth: {
      type: String,
      default: '120px',
    },//表单label的宽度
    originFormData: {
      type: Object,
      default: () => {}
    },//表单数据源
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
    isShowSumbitBtn: {
      type: Boolean,
      default: true,
    },//是否显示提交按钮
    formSize: {
      type: String,
      default: 'mini',
    },//表单元素size
    isShowLabel: {
      type: Boolean,
      default: true,
    },//是否显示label
  },
  data(){
    return {
      formLabel: [],//决定当前表单元素的arr，根据labels处理而得
      formData: {},//表单数据源
      rules: {},//表单的校验规则
      provinceOptions: [
        ...this.$utils.dataArea
      ],//省的选项
      cityOptions: [],//市的选项
      areaOptions: [],//区的选项
      area: {
        province: '',
        city: '',
        area: ''
      },//选择省市区的值
      areaFieldName: [],//存放选择省市区对应的表单元素配置中prop的值的数组
      areaDelimiter: '/',//省市区为同一字段时的分隔符，如“广东省/广州市”，分隔符为“/”
      areaLevelNum: 0,//区域校验需要判断多少，1-省，2-省市，3-省市区
      dialogVisible: false,//放大图片对话框显示控制
      dialogImageUrl: '',//放大图片对话框的图片url
      fileObj: {},//有关文件上传的对象
      fileFormItemName: ['photoUpload'],//文件上传的表单元素
      width: '',//网页可见区域宽
      setArrFormItem: ['checkbox'], //需要初始化为数组的表单元素
      needImgDialog: false, //是否需要显示图片的对话框
    }
  },
  async created(){
    this.width = document.body.offsetWidth;
    this.handleLabels(this.labels);
    await this.resetFormData();
    if(this.comData.type !== 'create' && !this.$utils.funcData.isEmpty(this.originFormData)){
      this.setFormData(this.originFormData);
    }
    this.rules = this.formRule ? this.formRule : this.getDefaultFormRules();
  },
  updated(){
    if(this.comData.type !== 'detail')
      this.$refs['baseForm'].clearValidate();
  },
  watch: {
    labels: async function(newVal){
      this.handleLabels(newVal);
      await this.resetFormData();
      this.rules = this.formRule ? this.formRule : this.getDefaultFormRules();
    },
    originFormData: {
      async handler(newVal){
        this.rules = {};
        if(this.comData.type === 'create'){
          this.resetFormData();
          this.area.province = '';
          this.area.city = '';
          this.area.area = '';
        }else{
          await this.resetFormData();
          this.setFormData(newVal);
          if(this.comData.type === 'detail')
            this.handleImg();
        }
        this.rules = this.formRule ? this.formRule : this.getDefaultFormRules();
      },
      deep: true,
    },
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
            return e !== "" && e !== null;
          })
          this.formData[this.areaFieldName[0]] = arr.join(this.areaDelimiter);
        }
      },
      deep: true,
      immediate: true
    },
    comData: {
      async handler(newVal){
        this.formLabel = this.labels;
        await this.resetFormData();
        if(newVal.type === 'update'){
          this.setFormData(this.originFormData);
        }
        this.rules = this.formRule ? this.formRule : this.getDefaultFormRules();
      },
      deep: true
    }
  },
  methods: {
    //获取表单元素的prop变量名字
    getPropsValue(item){
      if(item.type === 'autocomplete'){
        return `${item.prop}Label`;
      }else{
        return item.prop;
      }
    },
    //当富文本的内容直接显示时，对width过大的图片进行缩放
    handleImg(){
      this.$nextTick(() => {
        if(document.querySelector('.richTextEditorDetail')){
          let imgs = Array.from(document.querySelector('.richTextEditorDetail').getElementsByTagName('img'));
          imgs.forEach(itemInImg => {
            if(itemInImg.width > this.width - 390){
              itemInImg.style.zoom = (this.width - 390) / itemInImg.width;
            }
          })
        }
      })
    },
    //输入框带搜索建议的表单元素，选择选项后处理的函数
    autocompleteSelect(item, formItem){
      this.$set(this.formData, formItem.prop, item.value);
      this.$set(this.formData, `${formItem.prop}Label`, item.label);
      this.$set(this.formData, `${formItem.prop}Origin`, JSON.parse(JSON.stringify(item)));
      this.$set(this.formData, `${formItem.prop}_${formItem.uniqueField}`, item['origin'][formItem.uniqueField]);
      this.$set(this.formData, `${formItem.prop}_randomNum`, Math.floor(Math.random() * 100000));
    },
    //输入框带搜索建议的表单元素，
    querySearch(queryString, cb, options) {
      let results = [];
      options.forEach(itemInOptions => {
        if(itemInOptions.label.indexOf(queryString) > -1)
          results.push(itemInOptions)
      })
      cb(results);
    },
    //处理 labels 的函数
    //若表单元素有设置showType，则该元素只在指定的情况下出现
    handleLabels(labels){
      this.formLabel = [];
      labels.forEach(itemInL => {
        //处理表单元素的fns属性
        if(!this.$utils.funcData.isEmpty(itemInL.fns)){
          if(this.$utils.funcData.getType(itemInL.fns) !== 'object'){
            console.error(`表单元素用于绑定事件的fns属性的类型应为对象，父组件传入comDataId为${this.comData.id}`)
            return;
          } 
          //给表单元素的监听函数绑定this
          let fns = {};
          Object.keys(itemInL.fns).forEach(itemInLFA => {
            fns[itemInLFA] = itemInL.fns[itemInLFA].bind(this);
          })
          itemInL.fns = fns;
        }
        //判断表单元素是否需要显示
        if((itemInL.showType && itemInL.showType === this.comData.type) || (!itemInL.showType)){
          // TOTEST: 有图片上传时才显示对话框
          if(itemInL.type === 'photoUpload')
            this.needImgDialog = true;
          this.formLabel.push(itemInL);
        }
      })
    },
    //从选项数组中获取传入的value对应的label
    //用于在显示类型为detail的表单时，显示选择的值
    getValueFromOptions(options, value){
      if(Array.isArray(value)){
        let labels = [];
        for(let i=0; i<options.length; i++){
          if(value.indexOf(options[i].value) > -1){
            labels.push(options[i].label);
          }
        }
        return labels.join('、');
      }else{
        for(let i=0; i<options.length; i++){
          if(options[i].value === value){
            return options[i].label;
          }
        }
      }
    },
    //是否显示省的选择框
    showProvince(item){
      return  item.level === 1 || item.limit >= 1;
    },
    //是否显示市的选择框
    showCity(item){
      return  item.level === 2 || item.limit > 1;
    },
    //是否显示区的选择框
    showArea(item){
      return  item.level === 3 || item.limit === 3;
    },
    //选择市的处理函数
    handleCityChange(item, onlyHandleOptions=false){
      this.areaOptions = [];
      this.cityOptions.forEach((cItm) => {
        if(cItm[item.valueParamName] === this.area.city){
          this.areaOptions = cItm.children || [];
        }
      })
      if(!onlyHandleOptions){
        this.formLabel.forEach(e => {
          if(e.type === 'area' && (e.level === 3 || e.limit > 2)){
            let labelVal = this.areaOptions.length === 0 ? null : this.areaOptions[0][[item.valueParamName]];
            this.$set(this.formData, e.prop, labelVal);
            this.area.area = labelVal;
          }
        });
      }
    },
    //选择省的处理函数
    handleProvinceChange(item, onlyHandleOptions=false){
      this.cityOptions = [];
      this.provinceOptions.forEach((pItm) => {
        if(pItm[item.valueParamName] === this.area.province) {
          this.cityOptions = pItm.children || [];
        }
      })
      if(!onlyHandleOptions){
        this.areaOptions = [];
        this.formLabel.forEach(e => {
          if(e.type === 'area' && (e.level === 2 || e.limit > 1)){
            this.$set(this.formData, e.prop, this.cityOptions[0][item.valueParamName]);
            this.area.city = this.cityOptions[0][item.valueParamName];
          }
        });
      }
      this.handleCityChange(item);
    },
    //设置formData的值
    setFormData(newVal){
      this.labels.forEach(e => {
        //处理省市区
        if(e.type === 'area'){
          if(e.level){
            if(typeof e.level !== 'number')
              console.error(`组件 BaseFormCom 有关省市区的配置出错？父组件传入 comDataId 为${this.comData.id}，level 类型应为 number`)
            switch(e.level){
              case 1: 
                this.area.province = newVal[e.prop];
                this.areaLevelNum = 1;
                this.handleProvinceChange(e, true);
                break;
              case 2: 
                this.area.city = newVal[e.prop];
                this.areaLevelNum = 2;
                this.handleCityChange(e, true);
                break;
              case 3: 
                this.area.area = newVal[e.prop];
                this.areaLevelNum = 3;
                break;
            }
          }else if(e.limit){
            this.areaLevelNum = e.limit;
            let arr = newVal[e.prop] ? newVal[e.prop].split(this.areaDelimiter) : [];
            this.area.province = arr[0] ? +arr[0] : null;
            this.area.city = arr[1] ? +arr[1] : null;
            this.area.area = arr[2] ? +arr[2] : null;
            this.handleProvinceChange(e, true);
            this.handleCityChange(e, true);
          }else{
            console.error(`组件BaseFormCom有关省市区的配置出错？父组件传入comDataId为${this.comData.id}，既没有配置level，也没有配置limit`)
          }
        }else if(this.fileFormItemName.includes(e.type)){
          //处理文件
          this.fileObj = {};
          this.$set(this.fileObj, e.prop, []);
          this.$set(this.formData, e.prop, []);
          if(Array.isArray(newVal[e.prop])){
            newVal[e.prop].forEach((itemInNP, index) => { 
              let obj = {};
              obj.uid = index;
              obj.hadUpload = true;
              obj.url = itemInNP;
              this.fileObj[e.prop].push(obj);
              this.formData[e.prop].push(obj);
            })
          }else{
            let obj = {};
            obj.url = newVal[e.prop];
            obj.uid = Math.floor(Math.random() * 100000);
            obj.hadUpload = true;
            this.fileObj[e.prop].push(obj);
            this.formData[e.prop].push(obj);
          }
        }else if(e.type === 'autocomplete'){
          //TOTEST: 处理 autocomplete
          if(!this.formData[`${e.prop}_randomNum`])
            this.$set(this.formData, `${e.prop}_randomNum`, Math.floor(Math.random() * 100000));
          if(!this.$utils.funcData.isEmpty(newVal[`${e.prop}_${e.uniqueField}`])){
            e.options.forEach(item => {
              if(item[e.uniqueField] === newVal[`${e.prop}_${e.uniqueField}`]){
                this.$set(this.formData, `${e.prop}Label`, item.label);
                this.$set(this.formData, `${e.prop}_${e.uniqueField}`, item[e.uniqueField]);
                this.$set(this.formData, `${e.prop}Origin`, item);
                this.$set(this.formData, e.prop, newVal[e.prop]);
              }
            })
          }else if(!this.$utils.funcData.isEmpty(newVal[e.prop])){
            let itemOption = null;
            e.options.forEach(item => {
              if(item.value === newVal[e.prop]){
                this.$set(this.formData, `${e.prop}Label`, item.label);
                this.$set(this.formData, `${e.prop}_${e.uniqueField}`, item['origin'][e.uniqueField]);
                this.$set(this.formData, `${e.prop}Origin`, item);
                this.$set(this.formData, e.prop, newVal[e.prop]);
              }
            })
          }else if(!this.$utils.funcData.isEmpty(newVal[`${e.prop}Label`])){
            let itemOption = null;
            e.options.forEach(item => {
              if(item.label === newVal[`${e.prop}Label`]){
                this.$set(this.formData, `${e.prop}_${e.uniqueField}`, item['origin'][e.uniqueField]);
                this.$set(this.formData, `${e.prop}Label`, item.label);
                this.$set(this.formData, `${e.prop}Origin`, item);
                this.$set(this.formData, e.prop, item.value);
              }
            })
          }
        }else if(this.setArrFormItem.includes(e.type)){
          //处理需要值为数组的属性
          let propValue = Array.isArray(newVal[e.prop]) ? newVal[e.prop] : [];
          this.$set(this.formData, e.prop, propValue);
        }else{
          //处理其他类型的表单元素
          let propValue = this.$utils.funcData.isEmpty(newVal[e.prop]) ? null : newVal[e.prop];
          this.$set(this.formData, e.prop, propValue);
        }
      });
      this.formData.id = newVal.id;
    },
    //重置 formData
    resetFormData(){
      this.formData = {};
      this.areaFieldName = [];
      this.formLabel.forEach(e => {
        if(e.type === 'area'){
          //处理省市区的值
          if(this.areaFieldName.length > 3){
            console.error(`组件BaseFormCom有关省市区的配置出错？父组件传入comDataId为${this.comData.id}`)
          }
          if(e.level){
            if(typeof e.level !== 'number')
              console.error(`组件BaseFormCom有关省市区的配置出错？父组件传入comDataId为${this.comData.id}，level类型应为number`)
            switch(e.level){
              case 1: 
                this.areaLevelNum = 1;
                this.areaFieldName[0] = e.prop;
                break;
              case 2: 
                this.areaLevelNum = 2;
                this.areaFieldName[1] = e.prop;
                break;
              case 3: 
                this.areaLevelNum = 3;
                this.areaFieldName[2] = e.prop;
                break;
            }
          }else if(e.limit){
            this.areaLevelNum = e.limit;
            this.areaFieldName.push(e.prop)
          }else{
            console.error(`组件BaseFormCom有关省市区的配置出错？父组件传入comDataId为${this.comData.id}，既没有配置level，也没有配置limit`)
          }
        }else if(this.fileFormItemName.includes(e.type)){
          //处理文件
          this.fileObj = {};
          this.$set(this.fileObj, e.prop, []);
          this.$set(this.formData, e.prop, []);
        }else if(this.setArrFormItem.includes(e.type)){
          //处理需要初始化为数组的属性
          this.$set(this.formData, e.prop, []);
        }else{
          //其他类型的处理，默认初始化为null
          this.$set(this.formData, e.prop, null);
        }
      })
      this.area.province = '';
      this.area.city = '';
      this.area.area = '';
      if(this.comData.type === 'create')
        this.$nextTick(() => {
          this.$refs['baseForm'].resetFields();
        })
    },
    //获取当前formData的值
    getFormData(needUploadFile=false){
      //查看是否有富文本编辑器，若将获取到的值设置到formData对象
      if(this.$refs[`baseFormRC_${this.comData.id}`]){
        this.formLabel.forEach(itemInFL => {
          if(itemInFL.type === 'richTextEditor'){
            this.formData[itemInFL.prop] = this.$refs[`baseFormRC_${this.comData.id}`][0].getValue().getValue;
          }
        })
      }
      let result = this.formData;
      if(this.comData.type !== 'detail'){
        //当不是显示详情时，会校验表单，通过校验后再将formData传给从父组件传入的handleCompleteData函数
        this.$refs['baseForm'].validate((valid) => {
          if (valid) {
            let needWait = false,
              fileHandleArr = [];
            //上传图片
            this.formLabel.forEach(itemInFL => {
              if(itemInFL.type === 'photoUpload'){
                if(needUploadFile){
                  needWait = true;
                  fileHandleArr.push(this.fileUpload(itemInFL.prop, 'image', true));
                }else{
                  this.formData[itemInFL.prop] = this.fileObj[itemInFL.prop];
                }
              }
            })
            if(needWait){
              Promise.all(fileHandleArr).then(values => {
                return this.formData;
              })
            }
          } else {
            this.handleIncompleteData(this.formData, this.comData);
            result = null;
          }
        });
      }
      return result;
    },
    //提交表单
    handleSubmit(){
      //查看是否有富文本编辑器，若将获取到的值设置到formData对象
      if(this.$refs[`baseFormRC_${this.comData.id}`]){
        this.formLabel.forEach(itemInFL => {
          if(itemInFL.type === 'richTextEditor'){
            this.formData[itemInFL.prop] = this.$refs[`baseFormRC_${this.comData.id}`][0].getValue().getValue;
          }
        })
      }
      if(this.comData.type !== 'detail'){
        //当不是显示详情时，会校验表单，通过校验后再将formData传给从父组件传入的handleCompleteData函数
        this.$refs['baseForm'].validate((valid) => {
          if (valid) {
            let needWait = false,
              fileHandleArr = [];
            //上传图片
            this.formLabel.forEach(itemInFL => {
              if(itemInFL.type === 'photoUpload'){
                needWait = true;
                fileHandleArr.push(this.fileUpload(itemInFL.prop, 'image', true));
              }
            })
            if(needWait){
              Promise.all(fileHandleArr).then(values => {
                this.handleCompleteData(this.formData, this.comData.type, this.comData.id);
              })
            }else{
              this.handleCompleteData(this.formData, this.comData.type, this.comData.id);
            }
          } else {
            this.handleIncompleteData(this.formData, this.comData);
          }
        });
      }else{
        //当是显示详情时，将formData直接传给从父组件传入的handleCompleteData函数
        this.handleCompleteData(this.formData, this.comData.type, this.comData.id);
      }
    },
    //获取默认表单的校验规则
    getDefaultFormRules(){
      let rules = {};
      let areaRule = (rule, value, callback) => {
        if(this.areaLevelNum > 1 && this.areaFieldName.length === 1){ //area由limit决定时
          // let inputLength = this.formData[this.areaFieldName[0]].split(this.areaDelimiter).length;
          if(!this.area.province
            || (!this.area.city && this.areaLevelNum > 1)
            || (!this.area.area && this.areaLevelNum > 2)){
            callback(new Error('请选择'))
          }else{
            callback()
          }
        }else{//area由level决定时
          if(!this.formData[rule.field]){
            callback(new Error('请选择'))
          }else{
            callback()
          }
        }
      }
      this.formLabel.forEach(e => {
        let rulesArr = [];
        switch(e.type){
          case 'area':
            if(e.required === 1)
              rulesArr.push({
                message: `请选择`,
                trigger: 'change',
                required: true,
                validator: areaRule
              }) 
            break;
          case 'date': 
          case 'select': 
          case 'radio': 
          case 'checkbox':
            if(e.required === 1)
              rulesArr.push({
                message: `请选择${e.label}`,
                trigger: 'change',
                required: true
              })
            break;
          default:
            if(e.required === 1)
              rulesArr.push({
                message: `请选择${e.label}`,
                trigger: 'blur',
                required: true
              })
            if(e.checkRegArr && e.checkRegArr.length > 0){
              e.checkRegArr.forEach(itemInCRA => {
                if(!this.$utils.funcData.isEmpty(itemInCRA.pattern))
                  rulesArr.push({
                    message: itemInCRA.message || '格式不正确',
                    trigger: 'blur',
                    pattern: itemInCRA.pattern,
                  }) 
              })
            }
        }
        if(e.type === 'autocomplete'){
          rules[`${e.prop}Label`] = rulesArr;
        }else{
          rules[e.prop] = rulesArr;
        }
      });
      return rules;
    },
    //图片删除处理函数
    handlePhotoRemove(file, item) {
      for(let i=0; i<this.fileObj[item.prop].length; i++){
        if(this.fileObj[item.prop][i].url === file.url){
          this.formData[item.prop].splice(i, 1);
          this.fileObj[item.prop].splice(i, 1);
          window.URL.revokeObjectURL(file.url);
          return;
        }
      }
    },
    //点击放大图片按钮的处理函数
    handlePictureCardPreview(file, prop) {
      this.dialogImageUrl = file.url;
      this.dialogVisible = true;
    },
    //图片过多的处理函数
    handleTooManyPhoto(files, fileList){
      this.$message.error(`仅支持上传${fileList.length}张图片`)
    },
    //图片上传之前的处理函数
    handlePhotoBeforeUpload(file, item, type, index){
      file.url = window.URL.createObjectURL(file);
      file.hadUpload = false;
      if(type === 'onphoto'){
        window.URL.revokeObjectURL(this.fileObj[item.prop][index]['url']);
        this.fileObj[item.prop].splice(index, 1, file);
        this.formData[item.prop].splice(index, 1, file);
      }else{
        this.fileObj[item.prop].push(file);
        this.formData[item.prop].push(file);
      }
      return false;
    },
    //上传文件处理函数
    fileUpload(prop, type, nameNotValid){
      return new Promise((resolve, reject) => {
        let promiseArr = [];
        this.fileObj[prop].forEach(itemInFD => {
          if(!itemInFD.hadUpload){
            let data = new FormData;
            window.URL.revokeObjectURL(itemInFD.url);
            delete itemInFD.url;
            data.append('file', itemInFD);
            promiseArr.push(
              new Promise((res, rej) => {
                this.http({
                  url: this.$api.adminFileUpload,
                  method: 'POST',
                  params: {
                    fileType: type, 
                    nameNotValid
                  },
                  data
                }).then((r) => {
                  if (r.code === '0') {
                    res(r.data.url)
                  }
                })
              })
            )
          }
        })
        if(promiseArr.length > 0){
          Promise.all(promiseArr).then((values) => {
            this.formData[prop] = values;
            resolve();
          })
        }else{
          this.formData[prop] = [];
          this.fileObj[prop].forEach(itemInFileObj => {
            this.formData[prop].push(itemInFileObj.url);
          })
          resolve();
        }
      })
    },
  }
}
</script>
<style lang="less" scoped>
#baseFormCom{
  .el-form-item__label{
    font-size: 12px;
  }
  .word{
    display: inline-block;
    width: 100%;
    word-wrap: break-word;
    word-break: normal;
  }
  .photoUpload1Ul{    
    margin: 0;
    display: inline;
    vertical-align: top;
    padding: 0;
    list-style: none;
    .defaultImg{
      display: inline-block;
      background-color: #fbfdff;
      border: 1px dashed #c0ccda;
      border-radius: 6px;
      box-sizing: border-box;
      width: 148px;
      height: 148px;
      cursor: pointer;
      line-height: 146px;
      vertical-align: top;
      text-align: center;
      .defaultImg-icon{
        font-size: 28px;
        margin: 0 auto;
      }
    }
    .photoUpload1Li{
      overflow: hidden;
      background-color: #fff;
      border-radius: 6px;
      box-sizing: border-box;
      width: 148px;
      height: 148px;
      margin: 0 8px 8px 0;
      display: inline-block;
      transition: all .5s cubic-bezier(.55,0,.1,1);
      font-size: 14px;
      color: #606266;
      line-height: 1.8;
    }
    img{
      width: 100%;
      height: 100%;
    }
    .photoUpload1Div2{
      font-size: 14px;
      color: #606266;
      line-height: 1.8;
    }
    .photoUpload1Div2:hover{
      .photoUpload1Actions{
        opacity: 1 !important;
        .photoUpload1Actions-delete{
          display: inline-block;
        }
      }
    }
    .photoUpload1Actions{
      position: absolute;    
      width: 148px;
      height: 148px;
      left: 0;
      top: 0;
      cursor: default;
      text-align: center;
      color: #fff;
      opacity: 0;
      font-size: 20px;
      background-color: rgba(0,0,0,.5);
      -webkit-transition: opacity .3s;
      transition: opacity .3s;
      line-height: 1.8;
      border-radius: 6px;
      .photoUpload1Actions-delete{
        position: static;
        font-size: inherit;
        color: inherit;
        display: none;
        cursor: pointer;
        right: 10px;
        top: 0;
      }
    }
    .photoUpload1Actions::after {
      display: inline-block;
      content: "";
      height: 100%;
      vertical-align: middle;
    }
  }
}

</style>
<style lang="less">
#baseFormCom{
  .avatar-uploader .el-upload {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
  }
  .avatar-uploader .el-upload:hover {
    border-color: #409EFF;
  }
  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    line-height: 178px;
    text-align: center;
  }
  .avatar {
    width: 178px;
    height: 178px;
    display: block;
  }
  .baseForm{
    .el-form-item {
      margin-bottom: 20px;
    }
    .el-form-item__label{
      font-size: 12px;
      line-height: 30px !important;
    }
    .el-form-item__content{
      line-height: 30px !important;
    }
  }
  .baseFormDetail{
    .el-form-item{
      margin-bottom: 0;
      margin-top: 16px;
      vertical-align: top;
    }
    .el-form-item__label{
      font-size: 14px;
      color: rgba(0, 0, 0, 0.85);
      text-align: left;
      line-height: 25px !important;
    }
    .el-form-item__content{
      margin-left: 0 !important;
      line-height: 25px !important;
      color: rgba(0, 0, 0, 0.65);
    }
    .richTextEditorDetail{
      word-wrap: break-word;
      word-break: break-all;
      overflow: hidden;
      display: inline-block;
      width: calc(~"100% - 180px");
    }
  }
}
</style>