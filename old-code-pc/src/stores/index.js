// src/stores/counter.js
import { defineStore } from "pinia";
import { getMenuList } from "@/api/index";

export const useCounterStore = defineStore("counter", {
  // state：响应式状态
  state: () => ({
    //总弹窗开关
    loginopen: false,
    //处于那个状态
    title: "账号密码登录",
    //真机校验
    shimingzhuangtai: false,
    menuList: {},

    // 修改手机号邮箱
    changePhoneEmail: false,
    
    // 消息更新触发
    messageUpdate: 0
  }),

  // getters：计算属性（类似 computed）
  getters: {},
  // actions：修改状态的方法（支持同步/异步）
  actions: {
    getApiMenu(params){
      return new Promise((resolve, reject) => { 
        getMenuList(params).then((res) => {
          if (res.code == 200) {
            let data = res?.data || {}
            this.menuList = data || {}
            resolve(data || {})
          }else{
            reject({})
          }
        }).catch(error => {
          reject({})
        })
      });
    },
    // 修改手机号邮箱
    changePhoneEmailincrement() {
      this.changePhoneEmail = true;
    },
    changePhoneEmaildecrement() {
      this.changePhoneEmail = false;
    },
    increment() {
      this.title = "账号密码登录";
      this.loginopen = true;
    },
    phoneincrement() {
      this.title = "手机号注册";
      this.loginopen = true;
    },
    decrement() {
      this.loginopen = false;
    },
    logintitle() {
      this.title = "账号密码登录";
    },
    phonelogintitle() {
      this.shimingzhuangtai = false;
      this.title = "手机号登录";
    },
    phoneregister() {
      this.shimingzhuangtai = false;
      this.title = "手机号注册";
    },
    emailregister() {
      this.shimingzhuangtai = false;
      this.title = "邮箱注册";
    },
    password() {
      this.shimingzhuangtai = false;
      this.title = "重置密码";
    },
    shimingtrue() {
      this.shimingzhuangtai = true;
    },
    // 触发消息更新
    triggerMessageUpdate() {
      this.messageUpdate++;
    }
  },
});
