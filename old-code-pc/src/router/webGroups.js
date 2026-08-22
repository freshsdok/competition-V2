// 主应用路由 - 根路径作为父路由
const webGroups = {
  id: "main",
  path: "/",
  name: "main",
  component: () => import("@/layout/mainLayout.vue"),
  redirect: "/home",
  meta: { showNav: false, label: "首页" },
  // 子路由 - 使用相对路径，home作为默认路由
  children: [
    {
      path: "home",
      name: "home",
      meta: { showNav: true, label: "首页" },
      component: () => import("@/views/home/homeView.vue"),
    },
    {
      path: "event",
      name: "event",
      meta: { label: "赛事中心", showNav: true },
      redirect: "/event/browse",
      children: [
        {
          path: "browse",
          name: "browse",
          meta: { showNav: false, label: "浏览赛事" },
          component: () => import("@/views/event_center/event_browse.vue"),
        },
        {
          path: "detail",
          name: "detailView",
          meta: { label: "赛事详情" },
          children: [
            {
              path: "",
              name: "eventCenterDetail",
              component: () => import("@/views/event_center/detail.vue"),
            },
            {
              path: "team",
              name: "team",
              meta: { label: "我的团队" },
              component: () => import("@/views/event_center/team.vue"),
            },
            {
              path: "apply",
              name: "apply",
              meta: { label: "赛事报名" },
              component: () => import("@/views/event_center/apply.vue"),
            },
            {
              path: "teacherApply",
              name: "teacherApplyView",
              meta: { label: "赛事报名" },
              children: [
                {
                  path: "",
                  name: "teacherApply",
                  meta: { hiddenTriangle: "hidden" },
                  component: () =>
                    import("@/views/event_center/teacherApply/index.vue"),
                },
                {
                  path: "shopping",
                  name: "shopping",
                  meta: { label: "我的赛事" },
                  component: () => import("@/views/shopping/shopping.vue"),
                },
                {
                  path: "order",
                  name: "order",
                  meta: { label: "结算页面" },
                  component: () => import("@/views/shopping/order.vue"),
                },
              ],
            },
          ],
        },
      ],
    },
    {
      path: "learn",
      name: "learn",
      meta: { showNav: true, label: "学习中心" },
      component: () => import("@/views/learn/index.vue"),
    },
    {
      path: "information",
      name: "informationView",
      meta: { showNav: true, label: "公告通知" },
      children: [
        {
          path: "",
          name: "information",
          meta: { showNav: false, label: "" },
          component: () => import("@/views/information/index.vue"),
        },
        {
          path: "detail",
          name: "informationDetail",
          meta: { showNav: false, label: "" },
          component: () => import("@/views/information/detail.vue"),
        },
      ],
    },
    {
      path: "personal",
      name: "personal",
      meta: { showNav: false, label: "个人中心" },
      redirect: "/personal/list",
      children: [
        {
          path: "list",
          name: "list",
          meta: { showNav: false, label: "个人中心" },
          component: () => import("@/views/personal/index.vue"),
        },
        {
          path: "TeamDetails",
          name: "TeamDetails",
          meta: { showNav: false, label: "团队变更详情" },
          component: () =>
            import("@/views/personal/components/TeamDetails.vue"),
        },
        {
          path: "accountmanagement",
          name: "accountmanagement",
          meta: { showNav: false, label: "账号设置" },
          component: () => import("@/views/personal/accountmanagement.vue"),
        },
        {
          path: "paymentrecords",
          name: "paymentrecordsview",
          meta: { label: "我的订单" },
          children: [
            {
              path: "",
              name: "paymentrecords",
              component: () =>
                import("@/views/personal/personaltabs/paymentrecords.vue"),
            },
            {
              path: "payment",
              name: "payment",
              meta: { showNav: false, label: "支付详情" },
              component: () =>
                import("@/views/personal/personaltabs/payment.vue"),
            },
            {
              path: "OrderDetails",
              name: "OrderDetails",
              meta: { showNav: false, label: "订单详情" },
              component: () =>
                import("@/views/personal/personaltabs/OrderDetails.vue"),
            },
            {
              path: "invoiceIssuance",
              name: "invoiceIssuance",
              meta: { showNav: false, label: "申请开票" },
              component: () =>
                import("@/views/personal/personaltabs/invoiceIssuance.vue"),
            },
            {
              path: "invoice-preparation",
              name: "invoice-preparation",
              meta: { showNav: false, label: "选择开票" },
              component: () =>
                import("@/views/personal/personaltabs/invoice-preparation.vue"),
            },
          ],
        },
      ],
    },
    {
      path: "feedback",
      name: "feedback",
      meta: { showNav: false, label: "反馈" },
      component: () => import("@/views/feedback/index.vue"),
    },
    {
      path: "qa",
      name: "qa",
      meta: { showNav: false, label: "帮助中心" },
      component: () => import("@/views/qa/index.vue"),
    },
    {
      path: "customize",
      name: "customize",
      meta: { showNav: false },
      component: () => import("@/views/customize/index.vue"),
    },
    {
      path: "site",
      name: "siteView",
      redirect: "/site/list",
      children: [
        {
          path: "list",
          name: "siteMessageList",
          component: () => import("@/views/site_message/list.vue"),
        },
        {
          path: "detail",
          name: "siteMessageDetail",
          component: () => import("@/views/site_message/detail.vue"),
        },
      ],
    },
    {
      path: "certInterconnect",
      name: "certInterconnect",
      meta: { showNav: true, label: "赛证互通" },
      children: [
        {
          path: "",
          name: "certInterconnectList",
          meta: { hiddenTriangle: "hidden" },
          component: () => import("@/views/certInterconnect/index.vue"),
          meta: { label: "赛证互通" },
        },
        {
          path: "description",
          name: "description",
          component: () => import("@/views/certInterconnect/description.vue"),
          meta: { label: "申请说明" },
        },
        {
          path: "details/:ruleId(\\d+)",
          name: "certInterconnectDetails",
          component: () => import("@/views/certInterconnect/details.vue"),
          meta: { label: "赛证兑换" },
        },
        {
          path: "myCert",
          name: "myCert",
          component: () => import("@/views/certInterconnect/myCert.vue"),
          meta: { label: "我的证书" },
        },
        {
          path: "inquiry",
          name: "inquiry",
          component: () => import("@/views/certInterconnect/inquiry.vue"),
          meta: { label: "查询" },
        },
      ],
    },
    {
      path: "exam",
      name: "exam",
      component: () => import("@/views/exam/index.vue"),
      meta: { label: "我的赛场" },
    },
    {
      path: "awardPublicity",
      name: "awardPublicity",
      component: () => import("@/views/awardPublicity/index.vue"),
      meta: { label: "获奖公示" },
    },
    {
      path: "promotion",
      name: "promotion",
      component: () => import("@/views/promotion/index.vue"),
      meta: { label: "我的晋级队伍" },
    }
  ],
};

export default webGroups;
