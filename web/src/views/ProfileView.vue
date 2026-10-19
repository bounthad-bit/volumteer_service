<template>
  <div class="page">
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="page-title">基本资料</span></template>
          <el-form :model="form" label-width="90px">
            <el-form-item label="用户名">
              <el-input :model-value="auth.username" disabled />
            </el-form-item>
            <el-form-item label="角色">
              <el-tag>{{ auth.roleLabel }}</el-tag>
            </el-form-item>
            <el-form-item label="真实姓名">
              <el-input v-model="form.realName" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" maxlength="11" />
            </el-form-item>
            <el-form-item label="学号">
              <el-input v-model="form.studentNo" />
            </el-form-item>
            <el-button type="primary" :loading="saving" @click="save">保存修改</el-button>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="page-title">修改密码</span></template>
          <el-form ref="pwdRef" :model="pwd" :rules="pwdRules" label-width="90px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwd.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwd.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirm">
              <el-input v-model="pwd.confirm" type="password" show-password />
            </el-form-item>
            <el-button type="primary" :loading="changing" @click="changePwd">修改密码</el-button>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { changePasswordApi, getMyProfileApi, updateMyProfileApi } from "../api/user";
import { useAuthStore } from "../stores/auth";

const auth = useAuthStore();
const saving = ref(false);
const changing = ref(false);
const pwdRef = ref();
const form = reactive({ realName: "", phone: "", studentNo: "" });
const pwd = reactive({ oldPassword: "", newPassword: "", confirm: "" });

const pwdRules = {
  oldPassword: [{ required: true, message: "请输入原密码", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, max: 32, message: "长度需在 6~32 之间", trigger: "blur" }
  ],
  confirm: [
    {
      validator: (rule, value, cb) => (value === pwd.newPassword ? cb() : cb(new Error("两次输入的密码不一致"))),
      trigger: "blur"
    }
  ]
};

async function load() {
  const res = await getMyProfileApi();
  form.realName = res.data.realName || "";
  form.phone = res.data.phone || "";
  form.studentNo = res.data.studentNo || "";
}

async function save() {
  saving.value = true;
  try {
    const res = await updateMyProfileApi({ ...form });
    ElMessage.success("资料已更新");
    auth.refreshLocal(res.data);
  } catch (e) {
    ElMessage.error(e.message);
  } finally {
    saving.value = false;
  }
}

function changePwd() {
  pwdRef.value.validate(async (ok) => {
    if (!ok) return;
    changing.value = true;
    try {
      await changePasswordApi({ oldPassword: pwd.oldPassword, newPassword: pwd.newPassword });
      ElMessage.success("密码修改成功，请重新登录");
      pwd.oldPassword = pwd.newPassword = pwd.confirm = "";
      setTimeout(() => {
        auth.logout();
        window.location.href = "/login";
      }, 800);
    } catch (e) {
      ElMessage.error(e.message);
    } finally {
      changing.value = false;
    }
  });
}

onMounted(load);
</script>
