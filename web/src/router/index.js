import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const Portal = () => import("@/components/PortalLayout.vue");
const Console = () => import("@/components/AppLayout.vue");

/** 前台官网路由：志愿者、组织者与游客使用 */
const portalRoutes = [
  { path: "/login", component: () => import("@/views/PortalLoginView.vue"), meta: { guest: true } },
  { path: "/register", component: () => import("@/views/PortalRegisterView.vue"), meta: { guest: true } },
  {
    path: "/",
    component: Portal,
    children: [
      { path: "", component: () => import("@/views/PortalHomeView.vue") },
      { path: "activities", component: () => import("@/views/PortalActivityListView.vue") },
      { path: "activities/detail/:id", component: () => import("@/views/PortalActivityDetailView.vue") },
      { path: "notices", component: () => import("@/views/PortalNoticeView.vue") },
      { path: "about", component: () => import("@/views/PortalAboutView.vue") },
      {
        path: "my/registrations",
        component: () => import("@/views/MyRegistrationsView.vue"),
        meta: { title: "我的报名", roles: [1] }
      },
      { path: "my/hours", component: () => import("@/views/MyHoursView.vue"), meta: { title: "我的时长", roles: [1] } },
      {
        path: "my/profile",
        component: () => import("@/views/ProfileView.vue"),
        meta: { title: "个人中心", requiresAuth: true }
      },
      {
        path: "organizer/activities",
        component: () => import("@/views/OrganizerActivitiesView.vue"),
        meta: { title: "活动管理", roles: [2] }
      },
      {
        path: "organizer/review",
        component: () => import("@/views/RegistrationReviewView.vue"),
        meta: { title: "报名审核", roles: [2] }
      },
      {
        path: "organizer/checkins",
        component: () => import("@/views/CheckInRecordsView.vue"),
        meta: { title: "签到记录", roles: [2] }
      },
      {
        path: "organizer/evaluations",
        component: () => import("@/views/EvaluationsView.vue"),
        meta: { title: "活动评价", roles: [2] }
      }
    ]
  },
  { path: "/:pathMatch(.*)*", redirect: "/" }
];

/** 管理后台路由：仅系统管理员使用 */
const adminRoutes = [
  { path: "/login", component: () => import("@/views/AdminLoginView.vue"), meta: { guest: true } },
  {
    path: "/",
    component: Console,
    meta: { roles: [3] },
    children: [
      { path: "", component: () => import("@/views/AdminHomeView.vue"), meta: { title: "管理概览", roles: [3] } },
      {
        path: "activities",
        component: () => import("@/views/AdminActivityAuditView.vue"),
        meta: { title: "活动审核", roles: [3] }
      },
      { path: "users", component: () => import("@/views/UserManageView.vue"), meta: { title: "用户管理", roles: [3] } },
      {
        path: "notices",
        component: () => import("@/views/NoticeManageView.vue"),
        meta: { title: "公告管理", roles: [3] }
      },
      {
        path: "statistics",
        component: () => import("@/views/StatisticsView.vue"),
        meta: { title: "时长统计", roles: [3] }
      },
      { path: "profile", component: () => import("@/views/ProfileView.vue"), meta: { title: "个人中心", roles: [3] } }
    ]
  },
  { path: "/:pathMatch(.*)*", redirect: "/" }
];

/**
 * 按应用类型创建路由实例。
 * @param {"portal"|"admin"} appType 前台官网 / 管理后台
 */
export default function createAppRouter(appType) {
  const isAdmin = appType === "admin";
  const router = createRouter({
    history: createWebHistory("/"),
    routes: isAdmin ? adminRoutes : portalRoutes
  });

  router.beforeEach((to) => {
    const auth = useAuthStore();

    if (to.meta.guest) {
      if (auth.isLogin) {
        if (isAdmin) {
          // 管理后台：管理员已登录直接进后台；非管理员账号丢弃会话，留在登录页
          if (auth.isAdmin) return "/";
          auth.logout();
          return true;
        }
        return "/";
      }
      return true;
    }

    if (to.meta.roles || to.meta.requiresAuth) {
      if (!auth.isLogin) {
        return "/login";
      }
      if (to.meta.roles && !to.meta.roles.includes(auth.role)) {
        return isAdmin ? "/login" : "/";
      }
    }

    return true;
  });

  return router;
}
