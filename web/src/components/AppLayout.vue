<template>
  <el-container style="height: 100%">
    <el-aside width="220px" class="aside">
      <div class="brand">
        <div class="brand-mark">志</div>
        <div class="brand-text">
          <div class="brand-name">志愿同行</div>
          <div class="brand-sub">管理后台</div>
        </div>
      </div>
      <el-menu :default-active="activePath" router class="menu" background-color="#1f2d3d" text-color="#c0c4cc"
               active-text-color="#ffffff">
        <el-menu-item index="/admin">
          <el-icon><DataBoard /></el-icon><span>管理概览</span>
        </el-menu-item>
        <el-menu-item index="/admin/activities">
          <el-icon><Stamp /></el-icon><span>活动审核</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon><span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/notices">
          <el-icon><Notification /></el-icon><span>公告管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/statistics">
          <el-icon><DataAnalysis /></el-icon><span>时长统计</span>
        </el-menu-item>
        <el-menu-item index="/admin/profile">
          <el-icon><Setting /></el-icon><span>个人中心</span>
        </el-menu-item>
      </el-menu>

      <div class="aside-foot">
        <el-button text class="back-btn" @click="goPortal">
          <el-icon><Back /></el-icon>返回前台官网
        </el-button>
      </div>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-title">{{ pageTitle }}</div>
        <div class="spacer" />
        <el-tag size="small" type="info" effect="plain">系统管理员</el-tag>
        <el-dropdown @command="onCommand">
          <span class="user">
            {{ auth.realName || auth.username }}<el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="portal">返回前台官网</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessageBox } from "element-plus";
import { useAuthStore } from "../stores/auth";

const auth = useAuthStore();
const route = useRoute();
const router = useRouter();

const activePath = computed(() => route.path);
const pageTitle = computed(() => route.meta.title || "志愿者服务管理后台");

const PORTAL_ORIGIN = `${window.location.protocol}//${window.location.hostname}:5173`;
const goPortal = () => (window.location.href = `${PORTAL_ORIGIN}/`);

function onCommand(cmd) {
  if (cmd === "profile") return router.push("/profile");
  if (cmd === "portal") return goPortal();
  ElMessageBox.confirm("确定要退出登录吗？", "提示", { type: "warning" })
    .then(() => {
      auth.logout();
      router.push("/login");
    })
    .catch(() => {});
}
</script>

<style scoped>
.aside {
  background: #1f2d3d;
  display: flex;
  flex-direction: column;
}

.brand {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 18px;
  border-bottom: 1px solid #2b3a4d;
}

.brand-mark {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  background: linear-gradient(135deg, #2563eb, #0ea5e9);
  color: #fff;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-text {
  line-height: 1.2;
}

.brand-name {
  color: #fff;
  font-weight: 600;
  font-size: 15px;
}

.brand-sub {
  color: #64748b;
  font-size: 11px;
  letter-spacing: 1px;
}

.menu {
  border-right: none;
  flex: 1;
}

.aside-foot {
  padding: 12px 16px 18px;
  border-top: 1px solid #2b3a4d;
}

.back-btn {
  color: #94a3b8;
  font-size: 13px;
}

.back-btn:hover {
  color: #38bdf8;
}

.header {
  background: #fff;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #ebeef5;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
}

.spacer {
  flex: 1;
}

.user {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #303133;
}

.main {
  background: #f5f7fa;
}
</style>
