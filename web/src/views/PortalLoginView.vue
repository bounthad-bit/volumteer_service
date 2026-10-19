<template>
  <div class="pl">
    <div class="pl-left">
      <div class="pl-brand" @click="$router.push('/')">
        <div class="pl-mark">志</div>
        <span>志愿同行</span>
      </div>

      <div class="pl-copy">
        <h1>欢迎回来<br />继续你的志愿服务之旅</h1>
        <p>登录后即可报名志愿活动、查看个人服务时长，组织者还能发布活动并审核报名。</p>
      </div>

      <ul class="pl-points">
        <li v-for="p in points" :key="p">
          <el-icon><CircleCheckFilled /></el-icon>{{ p }}
        </li>
      </ul>

      <div class="pl-deco d1"></div>
      <div class="pl-deco d2"></div>
    </div>

    <div class="pl-right">
      <div class="pl-card">
        <div class="pl-card-head">
          <h2>账号登录</h2>
          <p>志愿者 / 活动组织者登录入口</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-button type="primary" class="pl-submit" :loading="loading" @click="submit">登 录</el-button>
        </el-form>

        <div class="pl-switch">
          还没有账号？<el-link type="primary" @click="$router.push('/register')">立即注册成为志愿者</el-link>
        </div>

        <div class="pl-demo">
          <div class="pl-demo-title">演示账号（密码均为 123456）</div>
          <div class="pl-demo-row">
            <el-tag size="small" @click="quick('volunteer')">志愿者 volunteer</el-tag>
            <el-tag size="small" type="success" @click="quick('organizer')">组织者 organizer</el-tag>
            <el-tag size="small" type="info" @click="$router.push('/login')">管理员走后台</el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock, CircleCheckFilled } from "@element-plus/icons-vue";
import { useAuthStore } from "../stores/auth";

const auth = useAuthStore();
const router = useRouter();
const formRef = ref();
const loading = ref(false);
const form = reactive({ username: "", password: "" });
const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }]
};
const points = [
  "一站式浏览、报名志愿活动",
  "签到签退后服务时长自动累计",
  "组织者可发布活动、审核报名",
  "活动结束后可提交评价反馈"
];

function quick(name) {
  form.username = name;
  form.password = "123456";
}

function submit() {
  formRef.value.validate(async (ok) => {
    if (!ok) return;
    loading.value = true;
    try {
      const payload = await auth.login(form.username.trim(), form.password);
      ElMessage.success("登录成功");
      router.push(payload.role === 3 ? "/admin" : "/");
    } catch (e) {
      ElMessage.error(e.message || "登录失败");
    } finally {
      loading.value = false;
    }
  });
}
</script>

<style scoped>
.pl {
  min-height: 100vh;
  display: flex;
}

.pl-left {
  position: relative;
  flex: 1.1;
  background: linear-gradient(140deg, #0b2a6b 0%, #12539e 48%, #0e7490 100%);
  color: #fff;
  padding: 44px 56px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.pl-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 17px;
  font-weight: 700;
  cursor: pointer;
  z-index: 2;
}

.pl-mark {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.pl-copy {
  margin-top: auto;
  z-index: 2;
}

.pl-copy h1 {
  font-size: 38px;
  line-height: 1.35;
  margin: 0 0 18px;
  font-weight: 700;
}

.pl-copy p {
  font-size: 15px;
  line-height: 1.9;
  color: rgba(255, 255, 255, 0.82);
  max-width: 460px;
  margin: 0;
}

.pl-points {
  list-style: none;
  padding: 0;
  margin: 34px 0 auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
  z-index: 2;
}

.pl-points li {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

.pl-points .el-icon {
  color: #7dd3fc;
}

.pl-deco {
  position: absolute;
  border-radius: 50%;
  filter: blur(2px);
}

.pl-deco.d1 {
  width: 420px;
  height: 420px;
  right: -160px;
  top: -120px;
  background: radial-gradient(circle, rgba(56, 189, 248, 0.4), transparent 70%);
}

.pl-deco.d2 {
  width: 360px;
  height: 360px;
  left: -120px;
  bottom: -140px;
  background: radial-gradient(circle, rgba(129, 140, 248, 0.4), transparent 70%);
}

.pl-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  background: #f7f9fc;
}

.pl-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 18px;
  padding: 40px 38px;
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.1);
}

.pl-card-head {
  margin-bottom: 26px;
}

.pl-card-head h2 {
  font-size: 23px;
  margin: 0 0 8px;
  color: #0f172a;
}

.pl-card-head p {
  margin: 0;
  font-size: 13px;
  color: #94a3b8;
}

.pl-submit {
  width: 100%;
  height: 46px;
  font-size: 16px;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #2563eb, #0ea5e9);
  border: none;
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.28);
}

.pl-switch {
  margin-top: 18px;
  text-align: center;
  font-size: 13px;
  color: #64748b;
}

.pl-demo {
  margin-top: 24px;
  padding: 14px 16px;
  background: #f4f7fc;
  border-radius: 10px;
}

.pl-demo-title {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 10px;
}

.pl-demo-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pl-demo-row .el-tag {
  cursor: pointer;
}

@media (max-width: 860px) {
  .pl-left {
    display: none;
  }
}
</style>
