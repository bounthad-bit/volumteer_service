<template>
  <div class="pr">
    <div class="pr-left">
      <div class="pr-brand" @click="$router.push('/')">
        <div class="pr-mark">志</div>
        <span>志愿同行</span>
      </div>
      <div class="pr-copy">
        <h1>注册成为志愿者<br />开启你的公益履历</h1>
        <p>填写基本信息即可开通账号，浏览并报名志愿活动，服务时长自动累计到个人档案。</p>
      </div>
      <div class="pr-steps">
        <div v-for="(s, i) in steps" :key="s" class="pr-step">
          <span class="pr-step-num">{{ i + 1 }}</span>{{ s }}
        </div>
      </div>
      <div class="pr-deco d1"></div>
      <div class="pr-deco d2"></div>
    </div>

    <div class="pr-right">
      <div class="pr-card">
        <div class="pr-card-head">
          <h2>志愿者注册</h2>
          <p>带 <em>*</em> 为必填项</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="3~20 位，登录时使用" :prefix-icon="User" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="6~32 位" :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="用于活动签到与时长认证" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="注册与活动签到时二次确认使用" :prefix-icon="Iphone" />
          </el-form-item>
          <el-form-item label="学号（选填）" prop="studentNo">
            <el-input v-model="form.studentNo" placeholder="在校学生可填写学号" />
          </el-form-item>
          <el-button type="primary" class="pr-submit" :loading="loading" @click="submit">注 册</el-button>
        </el-form>

        <div class="pr-switch">
          已有账号？<el-link type="primary" @click="$router.push('/login')">返回登录</el-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock, Iphone } from "@element-plus/icons-vue";
import { useAuthStore } from "../stores/auth";

const auth = useAuthStore();
const router = useRouter();
const formRef = ref();
const loading = ref(false);
const form = reactive({ username: "", password: "", realName: "", phone: "", studentNo: "" });
const steps = ["填写基本信息完成注册", "浏览并报名感兴趣的志愿活动", "活动当天签到签退记录时长", "在个人中心查看累计服务时长"];
const rules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 3, max: 20, message: "长度需在 3~20 之间", trigger: "blur" }
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 32, message: "长度需在 6~32 之间", trigger: "blur" }
  ],
  realName: [{ required: true, message: "请输入真实姓名", trigger: "blur" }],
  phone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "手机号格式不正确", trigger: "blur" }
  ]
};

function submit() {
  formRef.value.validate(async (ok) => {
    if (!ok) return;
    loading.value = true;
    try {
      await auth.register({ ...form });
      ElMessage.success("注册成功，已自动登录");
      router.push("/");
    } catch (e) {
      ElMessage.error(e.message || "注册失败");
    } finally {
      loading.value = false;
    }
  });
}
</script>

<style scoped>
.pr {
  min-height: 100vh;
  display: flex;
}

.pr-left {
  position: relative;
  flex: 1;
  background: linear-gradient(140deg, #0b2a6b 0%, #12539e 48%, #0e7490 100%);
  color: #fff;
  padding: 44px 56px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.pr-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 17px;
  font-weight: 700;
  cursor: pointer;
  z-index: 2;
}

.pr-mark {
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

.pr-copy {
  margin-top: auto;
  z-index: 2;
}

.pr-copy h1 {
  font-size: 34px;
  line-height: 1.4;
  margin: 0 0 18px;
}

.pr-copy p {
  font-size: 15px;
  line-height: 1.9;
  color: rgba(255, 255, 255, 0.82);
  max-width: 440px;
  margin: 0;
}

.pr-steps {
  margin: 34px 0 auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
  z-index: 2;
}

.pr-step {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

.pr-step-num {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.pr-deco {
  position: absolute;
  border-radius: 50%;
}

.pr-deco.d1 {
  width: 420px;
  height: 420px;
  right: -160px;
  top: -120px;
  background: radial-gradient(circle, rgba(56, 189, 248, 0.4), transparent 70%);
}

.pr-deco.d2 {
  width: 340px;
  height: 340px;
  left: -120px;
  bottom: -130px;
  background: radial-gradient(circle, rgba(129, 140, 248, 0.4), transparent 70%);
}

.pr-right {
  flex: 1.15;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  background: #f7f9fc;
  overflow-y: auto;
}

.pr-card {
  width: 100%;
  max-width: 440px;
  background: #fff;
  border-radius: 18px;
  padding: 38px 36px;
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.1);
}

.pr-card-head {
  margin-bottom: 18px;
}

.pr-card-head h2 {
  font-size: 23px;
  margin: 0 0 8px;
  color: #0f172a;
}

.pr-card-head p {
  margin: 0;
  font-size: 13px;
  color: #94a3b8;
}

.pr-card-head em {
  color: #ef4444;
  font-style: normal;
}

.pr-submit {
  width: 100%;
  height: 46px;
  font-size: 16px;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #2563eb, #0ea5e9);
  border: none;
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.28);
  margin-top: 6px;
}

.pr-switch {
  margin-top: 18px;
  text-align: center;
  font-size: 13px;
  color: #64748b;
}

@media (max-width: 860px) {
  .pr-left {
    display: none;
  }
}
</style>
