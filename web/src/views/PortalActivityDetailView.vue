<template>
  <div class="pd-page" v-loading="loading">
    <div class="pd-body">
      <el-page-header content="活动详情" @back="$router.push('/activities')" style="margin-bottom: 18px" />

      <el-card v-if="activity" shadow="never" class="pd-card">
        <div class="pd-banner">
          <img :src="activity.coverUrl || FALLBACK_COVER" :alt="activity.title" class="pd-banner-img" />
          <div class="pd-banner-mask"></div>
          <div class="pd-banner-title">{{ activity.title }}</div>
        </div>
        <div class="pd-head">
          <h1 class="pd-title">{{ activity.title }}</h1>
          <div class="pd-tags">
            <el-tag :type="activityStatusType(activity.status)" effect="dark">{{ activity.statusLabel }}</el-tag>
            <el-tag v-if="activity.avgScore" type="warning" effect="plain">
              平均 {{ activity.avgScore.toFixed(1) }} 分
            </el-tag>
          </div>
        </div>

        <div class="pd-info">
          <div class="pd-info-item">
            <el-icon><Location /></el-icon>
            <div><div class="pd-info-label">活动地点</div><div class="pd-info-value">{{ activity.location }}</div></div>
          </div>
          <div class="pd-info-item">
            <el-icon><Clock /></el-icon>
            <div>
              <div class="pd-info-label">活动时间</div>
              <div class="pd-info-value">
                {{ formatDateTime(activity.startTime) }} 至 {{ formatDateTime(activity.endTime) }}
              </div>
            </div>
          </div>
          <div class="pd-info-item">
            <el-icon><User /></el-icon>
            <div>
              <div class="pd-info-label">招募人数</div>
              <div class="pd-info-value">{{ activity.approvedCount }} / {{ activity.maxNum }} 人已通过</div>
            </div>
          </div>
          <div class="pd-info-item">
            <el-icon><Avatar /></el-icon>
            <div><div class="pd-info-label">发布组织者</div><div class="pd-info-value">{{ activity.organizerName }}</div></div>
          </div>
        </div>

        <el-progress :percentage="applyPercent" :stroke-width="10" :show-text="false" style="margin: 6px 0 22px" />

        <div class="pd-section-title">活动说明</div>
        <div class="pd-desc">{{ activity.description || "暂无活动说明" }}</div>

        <div class="pd-actions" v-if="auth.isVolunteer">
          <el-button v-if="canApply" type="primary" size="large" round @click="apply">报名参加</el-button>
          <el-tag v-else-if="activity.myRegistrationStatus === 0" type="warning" size="large">报名待组织者审核</el-tag>
          <el-tag v-else-if="activity.myRegistrationStatus === 1" type="success" size="large">报名已通过</el-tag>
          <el-tag v-else-if="activity.myRegistrationStatus === 2" type="danger" size="large">报名未通过</el-tag>
          <el-button v-if="canCheck" type="success" size="large" round @click="openCheck('in')">活动签到</el-button>
          <el-button v-if="canCheckOut" type="warning" size="large" round @click="openCheck('out')">活动签退</el-button>
          <el-tag v-if="activity.myCheckInState === 1" type="info" size="large">已完成签退，时长已计入档案</el-tag>
        </div>
        <div class="pd-actions" v-else-if="!auth.isLogin && activity.status === 1">
          <el-button type="primary" size="large" round @click="$router.push('/login')">登录后报名</el-button>
        </div>
      </el-card>

      <el-card shadow="never" class="pd-card" style="margin-top: 18px">
        <div class="pd-section-title">活动评价（{{ evaluations.length }} 条）</div>
        <el-form v-if="canEvaluate" :model="evalForm" inline style="margin: 14px 0">
          <el-form-item label="评分"><el-rate v-model="evalForm.score" /></el-form-item>
          <el-form-item label="评价">
            <el-input v-model="evalForm.content" placeholder="说说本次活动的感受（选填）" style="width: 320px" />
          </el-form-item>
          <el-button type="primary" @click="submitEval">提交评价</el-button>
        </el-form>

        <el-empty v-if="!evaluations.length" description="暂无评价" />
        <div v-else class="pd-eval-list">
          <div v-for="ev in evaluations" :key="ev.id" class="pd-eval">
            <div class="pd-eval-head">
              <span class="pd-eval-user">{{ ev.realName }}</span>
              <el-rate :model-value="ev.score" disabled size="small" />
              <span class="pd-eval-time">{{ formatDateTime(ev.createTime) }}</span>
            </div>
            <div class="pd-eval-content">{{ ev.content || "（未填写评价内容）" }}</div>
          </div>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="checkVisible" :title="checkType === 'in' ? '活动签到' : '活动签退'" width="420px">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 12px"
                title="为防止代签，请输入报名时登记的手机号进行二次确认。" />
      <el-input v-model="checkPhone" placeholder="请输入报名手机号" maxlength="11" size="large" />
      <template #footer>
        <el-button @click="checkVisible = false">取消</el-button>
        <el-button type="primary" :loading="checking" @click="doCheck">
          确定{{ checkType === "in" ? "签到" : "签退" }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { Location, Clock, User, Avatar } from "@element-plus/icons-vue";
import { getActivityDetailApi } from "../api/activity";
import { applyActivityApi, pageMyRegistrationsApi } from "../api/registration";
import { checkInApi, checkOutApi } from "../api/checkin";
import { pageActivityEvaluationsApi, submitEvaluationApi } from "../api/evaluation";
import { formatDateTime } from "../utils/format";
import { activityStatusType } from "../utils/dict";
import { useAuthStore } from "../stores/auth";

const FALLBACK_COVER = "/images/hero-volunteers.jpg";
const auth = useAuthStore();
const route = useRoute();
const id = Number(route.params.id);

const activity = ref(null);
const evaluations = ref([]);
const loading = ref(false);
const myRegistrationId = ref(null);
const checkVisible = ref(false);
const checkType = ref("in");
const checkPhone = ref("");
const checking = ref(false);
const evalForm = reactive({ score: 5, content: "" });

const applyPercent = computed(() => {
  if (!activity.value || !activity.value.maxNum) return 0;
  return Math.min(Math.round((activity.value.approvedCount / activity.value.maxNum) * 100), 100);
});
const canApply = computed(() =>
  auth.isVolunteer && activity.value && activity.value.status === 1 && activity.value.myRegistrationStatus === null
);
const canCheck = computed(() =>
  auth.isVolunteer && activity.value && activity.value.myRegistrationStatus === 1 && myRegistrationId.value &&
  activity.value.myCheckInState === null && [1, 2].includes(activity.value.status)
);
const canCheckOut = computed(() =>
  auth.isVolunteer && activity.value && activity.value.myCheckInState === 0
);
const canEvaluate = computed(() =>
  auth.isVolunteer && activity.value && activity.value.status === 3 && activity.value.myCheckInState === 1
);

async function load() {
  loading.value = true;
  try {
    const res = await getActivityDetailApi(id);
    activity.value = res.data;
    const ev = await pageActivityEvaluationsApi(id, { pageNo: 1, pageSize: 50 });
    evaluations.value = ev.data.records;
    if (auth.isVolunteer) {
      const mine = await pageMyRegistrationsApi({ pageNo: 1, pageSize: 100 });
      const found = mine.data.records.find((r) => r.activityId === id && r.status !== 3);
      myRegistrationId.value = found ? found.id : null;
    }
  } catch (e) {
    ElMessage.error(e.message);
  } finally {
    loading.value = false;
  }
}

async function apply() {
  try {
    await applyActivityApi(id);
    ElMessage.success("报名成功，请等待组织者审核");
    await load();
  } catch (e) {
    ElMessage.error(e.message);
  }
}

function openCheck(type) {
  checkType.value = type;
  checkPhone.value = auth.phone || "";
  checkVisible.value = true;
}

async function doCheck() {
  if (!/^1[3-9]\d{9}$/.test(checkPhone.value)) {
    ElMessage.warning("请输入正确的手机号");
    return;
  }
  checking.value = true;
  try {
    const payload = { registrationId: myRegistrationId.value, phone: checkPhone.value };
    if (checkType.value === "in") {
      const res = await checkInApi(payload);
      ElMessage.success(`签到成功，签到时间 ${formatDateTime(res.data.checkInTime)}`);
    } else {
      const res = await checkOutApi(payload);
      ElMessage.success(`签退成功，本次服务时长 ${res.data.durationText}`);
    }
    checkVisible.value = false;
    await load();
  } catch (e) {
    ElMessage.error(e.message);
  } finally {
    checking.value = false;
  }
}

async function submitEval() {
  try {
    await submitEvaluationApi({ activityId: id, score: evalForm.score, content: evalForm.content });
    ElMessage.success("评价提交成功");
    evalForm.content = "";
    await load();
  } catch (e) {
    ElMessage.error(e.message);
  }
}

onMounted(load);
</script>

<style scoped>
.pd-page {
  min-height: 60vh;
  background: #f7f9fc;
}

.pd-body {
  max-width: 960px;
  margin: 0 auto;
  padding: 26px 24px 60px;
}

.pd-card {
  border-radius: 14px;
  border: 1px solid #eef2f7;
}

.pd-banner {
  position: relative;
  height: 240px;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 22px;
  background: linear-gradient(135deg, #1d4ed8, #0ea5e9);
}

.pd-banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.pd-banner-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(2, 8, 23, 0.05) 40%, rgba(2, 8, 23, 0.68) 100%);
}

.pd-banner-title {
  position: absolute;
  left: 26px;
  right: 26px;
  bottom: 18px;
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  text-shadow: 0 2px 12px rgba(2, 8, 23, 0.5);
}

.pd-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 22px;
}

.pd-title {
  font-size: 22px;
  margin: 0;
  color: #0f172a;
}

.pd-tags {
  display: flex;
  gap: 8px;
}

.pd-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 18px;
  margin-bottom: 22px;
}

.pd-info-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.pd-info-item .el-icon {
  margin-top: 2px;
  color: #2563eb;
  font-size: 18px;
}

.pd-info-label {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 4px;
}

.pd-info-value {
  font-size: 14px;
  color: #334155;
}

.pd-section-title {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 12px;
}

.pd-desc {
  color: #475569;
  line-height: 1.9;
  white-space: pre-wrap;
}

.pd-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 26px;
  padding-top: 22px;
  border-top: 1px dashed #e2e8f0;
}

.pd-eval-list {
  display: flex;
  flex-direction: column;
}

.pd-eval {
  padding: 16px 0;
  border-bottom: 1px dashed #e9edf3;
}

.pd-eval:last-child {
  border-bottom: none;
}

.pd-eval-head {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 8px;
}

.pd-eval-user {
  font-weight: 600;
  color: #0f172a;
}

.pd-eval-time {
  font-size: 12px;
  color: #a8abb2;
}

.pd-eval-content {
  color: #475569;
  line-height: 1.8;
}
</style>
