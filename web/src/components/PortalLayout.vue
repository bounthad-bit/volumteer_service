<template>
  <div class="portal">
    <header class="nav" :class="{ scrolled: scrolled }">
      <div class="nav-inner">
        <div class="logo" @click="$router.push('/')">
          <div class="logo-mark">志</div>
          <div class="logo-text">
            <span class="logo-name">志愿同行</span>
            <span class="logo-sub">Volunteer Service Platform</span>
          </div>
        </div>

        <nav class="nav-links">
          <a class="nav-link" :class="{ active: isActive('/') }" @click="$router.push('/')">首页</a>
          <a class="nav-link" :class="{ active: isActive('/activities') }" @click="$router.push('/activities')">志愿活动</a>
          <a class="nav-link" :class="{ active: isActive('/notices') }" @click="$router.push('/notices')">公告资讯</a>
          <a class="nav-link" :class="{ active: isActive('/about') }" @click="$router.push('/about')">关于我们</a>
          <template v-if="auth.isVolunteer">
            <a class="nav-link" :class="{ active: isActive('/my/registrations') }" @click="$router.push('/my/registrations')">我的报名</a>
            <a class="nav-link" :class="{ active: isActive('/my/hours') }" @click="$router.push('/my/hours')">我的时长</a>
          </template>
          <template v-if="auth.isOrganizer">
            <a class="nav-link" :class="{ active: isActive('/organizer/activities') }"
               @click="$router.push('/organizer/activities')">活动管理</a>
            <a class="nav-link" :class="{ active: isActive('/organizer/review') }"
               @click="$router.push('/organizer/review')">报名审核</a>
            <a class="nav-link" :class="{ active: isActive('/organizer/checkins') }"
               @click="$router.push('/organizer/checkins')">签到记录</a>
          </template>
        </nav>

        <div class="nav-right">
          <template v-if="auth.isLogin">
            <el-dropdown @command="onCommand">
              <div class="user-chip">
                <div class="user-avatar">{{ (auth.realName || auth.username).slice(0, 1) }}</div>
                <span class="user-name">{{ auth.realName || auth.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item disabled>{{ auth.roleLabel }}</el-dropdown-item>
                  <el-dropdown-item command="profile" divided>个人中心</el-dropdown-item>
                  <el-dropdown-item v-if="auth.isOrganizer" command="organizer">我的活动</el-dropdown-item>
                  <el-dropdown-item v-if="auth.isAdmin" command="admin">进入管理后台</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button text class="nav-btn" @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" round class="register-btn" @click="$router.push('/register')">立即注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <main class="portal-main">
      <router-view />
    </main>

    <footer class="footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <div class="logo-mark small">志</div>
          <div>
            <div class="footer-name">志愿同行 · 志愿者服务信息系统</div>
            <div class="footer-desc">让每一次付出都被记录，让每一份善意都有回响</div>
          </div>
        </div>
        <div class="footer-cols">
          <div class="footer-col">
            <div class="footer-col-title">平台功能</div>
            <a @click="$router.push('/activities')">志愿活动</a>
            <a @click="$router.push('/notices')">公告资讯</a>
            <a @click="$router.push('/about')">关于我们</a>
          </div>
          <div class="footer-col">
            <div class="footer-col-title">志愿服务</div>
            <a @click="$router.push('/activities')">活动报名</a>
            <a @click="$router.push('/register')">志愿者注册</a>
          </div>
          <div class="footer-col">
            <div class="footer-col-title">技术支持</div>
            <span>SpringBoot + Vue3</span>
            <span>MySQL 8 · JWT</span>
            <span>本科毕业设计</span>
          </div>
        </div>
      </div>
      <div class="footer-bottom">© 2026 志愿同行志愿者服务信息系统 · 面向高校社团与社区的志愿服务管理平台</div>
    </footer>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessageBox } from "element-plus";
import { useAuthStore } from "../stores/auth";

const auth = useAuthStore();
const route = useRoute();
const router = useRouter();
const scrolled = ref(false);

const isActive = (path) => (path === "/" ? route.path === "/" : route.path.startsWith(path));

function onScroll() {
  scrolled.value = window.scrollY > 10;
}

function onCommand(cmd) {
  if (cmd === "profile") return router.push("/my/profile");
  if (cmd === "organizer") return router.push("/organizer/activities");
  if (cmd === "admin") return router.push("/admin");
  ElMessageBox.confirm("确定要退出登录吗？", "提示", { type: "warning" })
    .then(() => {
      auth.logout();
      router.push("/");
    })
    .catch(() => {});
}

onMounted(() => window.addEventListener("scroll", onScroll));
onUnmounted(() => window.removeEventListener("scroll", onScroll));
</script>

<style scoped>
.portal {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #eef1f6;
  transition: box-shadow 0.25s;
}

.nav.scrolled {
  box-shadow: 0 4px 20px rgba(15, 23, 42, 0.07);
}

.nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 68px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 28px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  flex-shrink: 0;
}

.logo-mark {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: linear-gradient(135deg, #2563eb, #0ea5e9);
  color: #fff;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.3);
}

.logo-mark.small {
  width: 32px;
  height: 32px;
  font-size: 15px;
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.15;
}

.logo-name {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: #1e293b;
}

.logo-sub {
  font-size: 10px;
  color: #94a3b8;
  letter-spacing: 0.4px;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  overflow: hidden;
}

.nav-link {
  padding: 7px 13px;
  font-size: 14px;
  color: #475569;
  border-radius: 8px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
}

.nav-link:hover {
  color: #2563eb;
  background: #f1f5ff;
}

.nav-link.active {
  color: #2563eb;
  font-weight: 600;
  background: #eaf1ff;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.nav-btn {
  color: #475569;
}

.register-btn {
  background: linear-gradient(135deg, #2563eb, #0ea5e9);
  border: none;
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.28);
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 12px 5px 5px;
  border-radius: 999px;
  background: #f1f5f9;
  cursor: pointer;
  outline: none;
}

.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb, #0ea5e9);
  color: #fff;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-name {
  font-size: 14px;
  color: #334155;
}

.portal-main {
  flex: 1;
  background: #f7f9fc;
}

.footer {
  background: #0f172a;
  color: #94a3b8;
  padding: 40px 24px 0;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  gap: 60px;
  flex-wrap: wrap;
  padding-bottom: 28px;
}

.footer-brand {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.footer-name {
  color: #e2e8f0;
  font-weight: 600;
  margin-bottom: 6px;
}

.footer-desc {
  font-size: 13px;
}

.footer-cols {
  display: flex;
  gap: 64px;
}

.footer-col {
  display: flex;
  flex-direction: column;
  gap: 9px;
  font-size: 13px;
}

.footer-col-title {
  color: #e2e8f0;
  font-weight: 600;
  margin-bottom: 2px;
}

.footer-col a,
.footer-col span {
  color: #94a3b8;
  cursor: pointer;
  transition: color 0.2s;
}

.footer-col a:hover {
  color: #38bdf8;
}

.footer-bottom {
  border-top: 1px solid #1e293b;
  max-width: 1200px;
  margin: 0 auto;
  padding: 18px 0;
  text-align: center;
  font-size: 12px;
  color: #64748b;
}
</style>
