import api from '@/request/api'
import http from '@/request/http'
export default {
  state: () => ({ 
    softwareDetail: {},
    processList: {}
  }),
  mutations: {
    setSoftwareDetail(state, data) {
      state.softwareDetail[data.id] = data;
    },
    setProcessList(state, data) {
      state.processList[data.id] = data.list;
    }
  },
  actions: {
    getSoftwareDetail({ commit, state }, id) {
      return new Promise((res, rej) => {
        if(state.softwareDetail[id]){
          res(state.softwareDetail[id]);
        }else{
          http({
            url: api.softwareDetailSoftware,
            method: 'POST',
            data: {
              data:{id}
            }
          }).then((r) => {
            console.log(r)
            if(r.code == '0'){
              commit('setSoftwareDetail', r.data);
              res(r.data);
            }
          }).catch(err => {
            rej(err);
          })
        }
      })
    },
    getProcessList({ commit, state }, softwareData) {
      return new Promise((res, rej) => {
        if(state.processList[softwareData.id]){
          res(state.processList[softwareData.id]);
        }else{
          let programName = softwareData.path.split("\\").pop();
          http({
            url: api.apiFileOperationGetProcessList,
            method: 'POST',
            data: {
              data: {
                imageName: programName,
                programName: softwareData.name
              }
            }
          }).then((r) => {
            if(r.code == '0' && r.data.length > 0){
              commit('setProcessList', {
                list: r.data,
                id: softwareData.id
              });
              res(r.data);
            }
          }).catch(err => {
            rej(err);
          })
        }
      })
    },
    replaceState({commit},data){
      commit('setSoftwareDetail',data)
    }
  }
}