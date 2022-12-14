let apiArr = [
  "software/listFiles", // 获取系统文件目录
  "software/getSystemSoftware", //获取软件列表
  "software/detailSoftware", // 软件详情
  "software/addFavorite", // 添加收藏
  "software/removeFavorite", // 移出收藏
  "software/addSoftware", // 添加软件
  "software/addCmdSoftware", // 通过命令行添加软件
  "software/removeSoftware", // 删除添加的软件

  "monitor/startAndMonitor", // 开启并监听软件
  "monitor/startMonitor", // 监听软件
  "monitor/stopMonitor", // 停止监控
  "monitor/listTask", // 历史监控任务查询
  "monitor/deleteTask", // 删除任务
  "monitor/getMonitoringIds", // 获取当前正再监听的软件id

  "action/listByFileType", // 查询文件读写醒胃
  "action/listByNetworkType", // 查询网络访问行为
  "action/listByProcessType", // 查询进程调用形成
  "action/listByRegistryType", // 查询注册表行为
  "action/listByDeviceType", // 查询设备访问行为
  "action/listBySecurityType", // 查询注册表行为
  "/api/action/downloadNetworkPackage", // 下载网络包
  "/api/action/downloadWriteFilePackage", // 下载对比文件
  '/api/action/downloadDeleteFile', // 删除文件的备份下载
  "/api/action/exportByFileType", // 导出文件读写行为
  "/api/action/exportByProcessType", // 导出进程调用形成
  "/api/action/exportByRegistryType", // 导出注册表行为
  "/api/action/exportByDeviceType", // 导出设备控制行为
  "/api/action/exportByNetworkType", // 导出网络访问行为
  "/api/action/exportBySecurityType", // 导出对象权限行为

  "system/getSystemOs", // 查询当前系统
  "system/openFileFolder", // 打开文件位置
  "/api/system/downloadFile", // 下载文件
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
    if (process.env.NODE_ENV === 'development') {
      exportObj[tplApi.replace(/\/./g, char => char.substring(1).toUpperCase())] = `/api/${tplApi}`
    } else {
      // TOTEST: 地址更改： /ys2/ ===> /lis/
      exportObj[tplApi.replace(/\/./g, char => char.substring(1).toUpperCase())] = `/api/${tplApi}`
    }
  } else {
    exportObj[api.replace(/\/./g, char => char.substring(1).toUpperCase())] = api
  }
})
export default exportObj
