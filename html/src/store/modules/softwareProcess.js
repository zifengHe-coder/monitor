import api from '@/request/api'
import http from '@/request/http'
export default {
  state: () => ({
    softwareList: {},
    monitoringIds: [], // 监听中软件的id集合
    comData: {
      id: 'softwareList'
    },
    totalPages: 0,
    onff: false
  }),
  mutations: {
    setSoftwareList(state, data) {
      state.totalPages = data.totalPage;
      let list = state.softwareList;
      let totalArr = [];
      let letterArr = [];
      state.softwareList = {};
      for (let i = 0; i < data.data.length; i++) {
        data.data[i].firstLetter = data.data[i].softwareName.slice(0, 1).toUpperCase();
        let letter = data.data[i].softwareName.slice(0, 1);
        totalArr.push(letter)
      }
      totalArr = totalArr.map(item => 
        item.toLocaleUpperCase() 
      )
      totalArr = [...new Set(totalArr)]
      const letterReg = /^[A-Z]$/g;
      letterArr = totalArr.map(item => item.match(letterReg)).filter(item => item != null);
      letterArr.sort((a,b)=> {
        if(a[0] > b[0]){
          return 1
        }else{
          return -1
        }
      });
      list['常用程序'] = [];
      letterArr.forEach(item => {
        list[item] = [];
      })
      list['其他'] = [];
      for(let i=0;i<data.data.length;i++){
        if(/[a-zA-Z]/.test(data.data[i].firstLetter)){
          list[data.data[i].firstLetter].push(data.data[i])
        }else{
          list['其他'].push(data.data[i])
        }
      }
      for(let k in list){
        for(let j=0;j<list[k].length;j++){
          if(list[k][j].favorite === true){
            list['常用程序'].push(list[k][j])
            list[k].splice(j,1);
          }
        }
      }
      state.softwareList = list;
    },
    setOnff(state, value) {
      state.onff = value;
    },
    setMonitoringSoftwareList(state){
      let list = JSON.parse(JSON.stringify(state.softwareList));
      for(let k in list){
        state.softwareList[k] = [];
        for(let i=0; i<list[k].length;i++){
          for(let j=0;j<state.monitoringIds.length;j++){
            if(list[k][i].id === state.monitoringIds[j]){
              state.softwareList[k].push(list[k][i]);
            }
          }
        }
        if(state.softwareList[k].length === 0){
          delete state.softwareList[k]
        }
      }
      console.log(JSON.parse(JSON.stringify(state.softwareList)))
    },
  },
  getters: {
    getSoftwareList(state) {
      return state.softwareList;
    },
    getTotalPages(state) {
      return state.totalPages;
    }
  },
  actions: {
    getMonitoringIds({commit,state}){
      return new Promise(resolve=>{
        http({
          url: api.monitorGetMonitoringIds,
          method: "GET"
        }).then(r => {
          if(r.code === '0'){
            if(r.data.length > 0){
              state.monitoringIds = r.data;
            }else{
              state.monitoringIds = [];
            }
            commit('setMonitoringSoftwareList')
            resolve();
          }
        }).catch(err => {
          console.log(err)
        })
      })
    },
    getSoftwareList({ commit, state }, forceGet = true) {
      return new Promise((res, rej) => {
        if (!state.onff) {
          commit('setOnff', true);
          let params = JSON.parse(sessionStorage.getItem(`${state.comData.id}Page`));
          http({
            url: api.softwareGetSystemSoftware,
            method: 'GET',
          }).then((r) => {
            if (r.code == '0') {
              commit('setSoftwareList', r);
              if (params.pageNo < r.totalPage) {
                ++params.pageNo;
              }
              sessionStorage.setItem(`${state.comData.id}Page`, JSON.stringify(params));
              commit('setOnff', false);
              res(r);
            }
          }).catch(err => {
            commit('setOnff', false);
          })
        }
      })
    },
    resetSoftwareList({ state, dispatch }) {
      state.softwareList = {};
      sessionStorage.setItem(`${state.comData.id}Page`, JSON.stringify({
        data: {},
        pageNo: 0,
        pageSize: 20
      }));
      dispatch('getSoftwareList');
    },
  }
}