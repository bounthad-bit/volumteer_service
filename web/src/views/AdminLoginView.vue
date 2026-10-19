<template>
  <div class="al">
    <div class="al-bg"></div>
    <div class="al-card">
      <div class="al-head">
        <div class="al-mark"><el-icon :size="24"><Setting /></el-icon></div>
        <h1>志愿者服务管理后台</h1>
        <p>管理员专属入口</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="管理员账号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" class="al-submit" :loading="loading" @click="submit">登录后台</el-button>
      </el-form>

      <div class="al-hint">
        演示管理员账号：<b>admin</b> / <b>123456</b>
      </div>
      <div class="al-links">
        <el-link type="primary" @click="goPortal">前往前台官网</el-link>
        <span class="al-sep">·</span>
        <el-link @click="goPortalLogin">志愿者 / 组织者登录</el-link>
      </div>
    </div>
    <div class="al-foot">本科毕业设计 · 基于 SpringBoot 的志愿者服务信息系统</div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock, Setting } from "@element-plus/icons-vue";
import { useAuthStore } from "../stores/auth";

const auth = useAuthStore();
const router = useRouter();
const formRef = ref();
const loading = ref(false);
const form = reactive({ username: "", password: "" });
const rules = {
  username: [{ required: true, message: "请输入管理员账号", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }]
};

const PORTAL_ORIGIN = `${window.location.protocol}//${window.location.hostname}:5173`;
const goPortal = () => (window.location.href = `${PORTAL_ORIGIN}/`);
const goPortalLogin = () => (window.location.href = `${PORTAL_ORIGIN}/login`);

function submit() {
  formRef.value.validate(async (ok) => {
    if (!ok) return;
    loading.value = true;
    try {
      const payload = await auth.login(form.username.trim(), form.password);
      if (payload.role !== 3) {
        ElMessage.warning("该账号不是管理员，请使用前台官网登录入口");
        auth.logout();
        return;
      }
      ElMessage.success("登录成功");
      router.push("/");
    } catch (e) {
      ElMessage.error(e.message || "登录失败");
    } finally {
      loading.value = false;
    }
  });
}
</script>

<style scoped>
.al {
  position: relative;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #0b1220;
  overflow: hidden;
}

.al-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(700px 340px at 20% 18%, rgba(37, 99, 235, 0.32), transparent 70%),
    radial-gradient(600px 320px at 82% 80%, rgba(14, 116, 144, 0.32), transparent 70%);
}

.al-card {
  position: relative;
  width: 400px;
  padding: 38px 36px 30px;
  background: rgba(17, 25, 45, 0.86);
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 16px;
  backdrop-filter: blur(14px);
  box-shadow: 0 26px 70px rgba(2, 8, 23, 0.6);
}

.al-head {
  text-align: center;
  margin-bottom: 26px;
}

.al-mark {
  width: 54px;
  height: 54px;
  margin: 0 auto 16px;
  border-radius: 14px;
  background: linear-gradient(135deg, #2563eb, #0ea5e9);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12px 28px rgba(37, 99, 235, 0.4);
}

.al-head h1 {
  font-size: 19px;
  color: #e2e8f0;
  margin: 0 0 6px;
}

.al-head p {
  margin: 0;
  font-size: 12px;
  color: #64748b;
  letter-spacing: 2px;
}

.al-submit {
  width: 100%;
  height: 44px;
  letter-spacing: 3px;
  background: linear-gradient(135deg, #2563eb, #0ea5e9);
  border: none;
}

.al-hint {
  margin-top: 18px;
  padding: 10px 12px;
  background: rgba(148, 163, 184, 0.1);
  border-radius: 8px;
  font-size: 12px;
  color: #94a3b8;
  text-align: center;
}

.al-hint b {
  color: #7dd3fc;
}

.al-links {
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.al-links :deep(.el-link) {
  color: #64b5f6;
}

.al-links :deep(.el-link:last-child) {
  color: #64748b;
}

.al-foot {
  position: relative;
  margin-top: 26px;
  font-size: 12px;
  color: #475569;
}
</style>
