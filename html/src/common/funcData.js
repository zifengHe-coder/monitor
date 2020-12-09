const baseData = {
  /**
     * 从对象中获取属性值，若获取失败，返回 noResult 的值
     * @param {Array} arr 存放属性名的数组
     * @param {Object} obj 获取的对象
     * @param {String} descript 没有获取成功时，发出 warn 的描述信息，可选
     * @param {*} noResult 没有获取成功时，赋给变量的值，默认为 null
     * @param {Boolean} showWarn 是否打印警告信息，默认为 true
     */
  getValueFromObj: function (arr, obj, descript, noResult = null, showWarn = false) {
    let parentLevel = obj;
    for (let i = 0; i < arr.length; i++) {
      if (!parentLevel[arr[i]]) {
        if (showWarn)
          console.warn(`getValueFromObj返回信息：object.${arr.slice(0, i + 1).join('.')}的值获取失败，变量被赋值为${noResult}${descript ? '，' + descript : ''}`)
        return noResult;
      } else {
        parentLevel = parentLevel[arr[i]];
      }
    }
    return parentLevel;
  },
  /**
   * 从对象数组中获取对应的 label
   * @param {Array} arr 
   * @param {*} value 获取时，对比的值
   * @param {String} otherName 获取时，对比的属性名，默认为 value
   */
  getLabelFromArr: function (arr, value, otherName = 'value') {
    for (let i = 0; i < arr.length; i++) {
      if (arr[i][otherName] === value)
        return arr[i].label;
    }
  },
  /**
   * 获取变量类型
   * @param {*} param 变量
   * 可能返回的值有 number/string/boolean/null/undefined/function/array/object/symbol
   */
  getType: function (param) {
    let typeStr = Object.prototype.toString.call(param);
    return typeStr.slice(8, typeStr.length - 1).toLocaleLowerCase();
  },
  /**
   * 检测变量是否为 null 或 undefined
   * @param {*} param 
   */
  isNullOrUndefined: function (param) {
    let type = baseData.getType(param);
    if (type === 'null' || type === 'undefined')
      return true;
    return false;
  },
  /**
   * 判断参数是否为空
   * @param {*} param 
   * 当param为 ""/{}/[]/null/undefined 时，返回true，否则返回 false
   */
  isEmpty(param) {
    let type = baseData.getType(param);
    let result = false;
    switch (type) {
      case 'string':
        if (param === '')
          result = true;
        break;
      case 'null':
      case 'undefined':
        result = true;
        break;
      case "array":
        if (param.length === 0)
          result = true;
        break;
      case "object":
        if (Object.keys(param).length === 0)
          result = true;
        break;
    }
    return result;
  },
  /**
   * 处理object，删除值为 null/''/[]/{} 的属性
   * @param {*} obj 
   */
  handleObjParams(obj) {
    let resultObj = JSON.parse(JSON.stringify(obj));
    if (baseData.isEmpty(obj))
      return obj;
    for (let key in resultObj) {
      if (baseData.isEmpty(resultObj[key]))
        delete resultObj[key];
    }
    return resultObj;
  },
  /**
   * 是否为NaN
   * @param {*} param 
   */
  isNaN(param) {
    return String(param) === 'NaN';
  },
  /**
   * 从结构相似的json中寻找某个属性的值，返回一个数组，包含找到的值及其所有父级的值
   * 即在 options 中递归寻找属性 prop 的值等于 value 的元素，返回一个包含找到的值及其所有父级的值的数组
   * @param {*} prop 寻找时根据的属性名
   * @param {*} value 寻找时根据的属性的值
   * @param {*} result 存放结果的数组
   * @param {*} options 数据源
   */
  getDataFromList(prop, value, result, options) {
    const arrLength = result.length;
    if (!Array.isArray(options)) return;
    for (let i = 0; i < options.length; i++) {
      if (value === options[i][prop]) {
        result.unshift(options[i]);
        return result;
      } else {
        let arr = baseData.getDataFromList(prop, value, result, options[i].children);
        if (Array.isArray(arr) && arr.length > arrLength) {
          result = arr;
          let data = JSON.parse(JSON.stringify(options[i]));
          delete data.children;
          result.unshift(data);
          return result;
        }
      }
    }
  },
  /**
   * 根据省/市/区id获取地区数据
   * @param {number} value 城市id
   */
  getAreaById(value) {
    return baseData.getDataFromList('id', value, [], area.area);
  },
  // 转换时间格式
  formatTime(str, fmt) {
    fmt = fmt || 'yyyy-MM-dd'
    const date = typeof str === 'string'
      ? new Date(str.replace(/-/g, '/'))
      : new Date(str)
    const o = {
      'M+': date.getMonth() + 1,
      'd+': date.getDate(),
      'h+': date.getHours(),
      'm+': date.getMinutes(),
      's+': date.getSeconds(),
      'q+': Math.floor((date.getMonth() + 3) / 3),
      S: date.getMilliseconds()
    }
    if (/(y+)/.test(fmt)) {
      fmt = fmt.replace(RegExp.$1,
        date.getFullYear().toString().substr(4 - RegExp.$1.length))
    }
    Object.keys(o).forEach((k) => {
      if (new RegExp(`(${k})`).test(fmt)) {
        fmt = fmt.replace(
          RegExp.$1, (RegExp.$1.length === 1)
          ? (o[k])
          : ((`00${o[k]}`).substr((`${o[k]}`).length)))
      }
    })
    return fmt
  },
  // 格林尼治时间转北京时间 yyyy-MM-ddThh:mm:ss.000Z  ==>>  yyyy-MM-dd hh:mm:ss
  formDateGMT(dateForm) {
    if (baseData.isEmpty(dateForm)) {
      return "";
    } else {
      let dateTemp = new Date(dateForm).toJSON();
      return new Date(+new Date(dateTemp) + 8 * 3600 * 1000).toISOString().replace(/T/g, ' ').replace(/\.[\d]{3}Z/, '');
    }
  },
}
export default baseData;