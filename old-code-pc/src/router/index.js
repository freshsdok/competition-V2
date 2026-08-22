import { createRouter, createWebHistory } from "vue-router";
import webGroups from "./webGroups.js";

// 登录路由 - 独立顶级路由
const login = {
  id: "login",
  path: "/login",
  name: "loginView",

  component: () => import("@/layout/baseLayout.vue"),
  children: [
    {
      path: "",
      name: "login",
      component: () => import("@/views/login/login.vue"),
    },
  ],
};

// 注册路由 - 独立顶级路由
const register = {
  id: "register",
  path: "/register",
  name: "registerView",
  component: () => import("@/layout/baseLayout.vue"),
  children: [
    {
      path: "",
      name: "register",
      component: () => import("@/views/login/register.vue"),
    },
  ],
};
// 忘记密码 - 独立顶级路由
const password = {
  id: "password",
  path: "/password",
  name: "forgetpasswordView",
  component: () => import("@/layout/baseLayout.vue"),
  children: [
    {
      path: "",
      name: "password",
      component: () => import("@/views/login/password.vue"),
    },
  ],
};
const workflow = {
  path: "/workflow/process",
  component: () => import("@/layout/mainLayout.vue"),
  hidden: true,
  children: [
    {
      path: "start/:deployId([\\w|\\-]+)",
      component: () => import("@/views/workflow/work/start.vue"),
      name: "WorkStart",
      meta: {
        title: "发起流程",
        activeMenu: "/workflow/process",
        icon: "",
      },
    },
    {
      path: "detail/:procInsId([\\w|\\-]+)",
      component: () => import("@/views/workflow/work/detail.vue"),
      name: "WorkDetail",
      meta: { title: "流程详情", activeMenu: "/work/own", icon: "" },
    },
      {
      path: "category",
      name: "category",
      meta: { showNav: false, label: "反馈" },
      component: () => import("@/views/workflow/model/index.vue"),
    },
  ],
};
// 404页面路由 - 路径为 /404
const notFound = {
  id: "notFound",
  path: "/404",
  name: "notFoundView",
  component: () => import("@/layout/baseLayout.vue"),
  // 子路由 - 使用相对路径
  children: [
    {
      path: "",
      name: "notFound",
      component: () => import("@/views/404/notFoundView.vue"),
    },
  ],
};

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // 认证相关路由（登录、注册）
    login,
    register,
    password,
    // 主应用路由（含默认重定向到home）
    webGroups,
    workflow,
    notFound,
    // 404页面路由
    { path: "/:pathMatch(.*)*", redirect: "/404" },
  ],
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    } else {
      return { top: 0 };
    }
  },
});

// 路由导航守卫
router.beforeEach((to, from, next) => {
  next();
});

// 路由变化后更新元数据
router.afterEach((to) => {
  // updateMetaInfo(to);
});

export default router;
