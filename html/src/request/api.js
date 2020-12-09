let apiArr = [
  "software/getSystemSoftware", //获取软件列表
  "software/detailSoftware", // 软件详情

  "monitor/startAndMonitor", // 开启并监听软件
  "monitor/startMonitor", // 监听软件
  "monitor/stopMonitor", // 停止监控            
  "monitor/listTask", // 历史监控任务查询
];

const exportObj = {};

// apiArr.forEach(api => {
//   if (api.indexOf("/api/") !== -1) {
//     const tplApi = api.split("/api/")[1];
//     if (process.env.NODE_ENV === "development") {
//       exportObj[
//         tplApi.replace(/\/./g, char => char.substring(1).toUpperCase())
//       ] = `/api/${tplApi}`;
//     } else {
//       exportObj[
//         tplApi.replace(/\/./g, char => char.substring(1).toUpperCase())
//       ] = `/${tplApi}`;
//     }
//   } else {
//     exportObj[
//       api.replace(/\/./g, char => char.substring(1).toUpperCase())
//     ] = api;
//   }
// });
apiArr.forEach((api) => {
  if (api.indexOf('/api/') !== -1) {
    const tplApi = api.split('/api/')[1]
    console.log(tplApi)
    if (process.env.NODE_ENV === 'development') {
      exportObj[tplApi.replace(/\/./g, char => char.substring(1).toUpperCase())] = `/api/${tplApi}`
    } else {
      // TOTEST: 地址更改： /ys2/ ===> /lis/
      exportObj[tplApi.replace(/\/./g, char => char.substring(1).toUpperCase())] = `/lis/api/${tplApi}`
    }
  } else {
    exportObj[api.replace(/\/./g, char => char.substring(1).toUpperCase())] = api
  }
})
export default exportObj
