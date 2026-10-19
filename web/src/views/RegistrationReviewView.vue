<template>
  <div class="page">
    <div class="toolbar">
      <el-select v-model="query.activityId" placeholder="全部活动" clearable style="width: 240px" @change="reload">
        <el-option v-for="a in activities" :key="a.id" :label="a.title" :value="a.id" />
      </el-select>
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px" @change="reload">
        <el-option v-for="s in REGISTRATION_STATUS" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe>
      <el-table-column prop="activityTitle" label="活动名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="realName" label="志愿者" width="110" />
      <el-table-column prop="username" label="账号" width="120" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="studentNo" label="学号" width="120">
        <template #default="{ row }">{{ emptyText(row.studentNo) }}</template>
      </el-table-column>
      <el-table-column label="报名时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.registerTime) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="registrationStatusType(row.status)" size="small">{{ row.statusLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button link type="success" @click="review(row, true)">通过</el-button>
            <el-button link type="danger" @click="review(row, false)">拒绝</el-button>
          </template>
          <span v-else>-</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination background layout="total, prev, pager, next" :total="total" :page-size="query.pageSize"
                     :current-page="query.pageNo" @current-change="onPage" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { pageMyActivitiesApi } from "../api/activity";
import { pageOrganizerRegistrationsApi, reviewRegistrationApi } from "../api/registration";
import { formatDateTime, emptyText } from "../utils/format";
import { REGISTRATION_STATUS, registrationStatusType } from "../utils/dict";

const rows = ref([]);
const activities = ref([]);
const total = ref(0);
const loading = ref(false);
const query = reactive({ pageNo: 1, pageSize: 10, activityId: null, status: null });

function reload() {
  query.pageNo = 1;
  load();
}

async function load() {
  loading.value = true;
  try {
    const res = await pageOrganizerRegistrationsApi({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      activityId: query.activityId ?? undefined,
      status: query.status ?? undefined
    });
    rows.value = res.data.records;
    total.value = res.data.total;
  } catch (e) {
    ElMessage.error(e.message);
  } finally {
    loading.value = false;
  }
}

function onPage(p) {
  query.pageNo = p;
  load();
}

function review(row, pass) {
  const action = pass ? "通过" : "拒绝";
  ElMessageBox.prompt(`请输入${action}该报名的审核意见（可选）`, `${action}报名`, {
    inputPlaceholder: "审核意见",
    inputValue: pass ? "通过" : "不符合活动要求"
  })
    .then(async ({ value }) => {
      try {
        await reviewRegistrationApi(row.id, pass, value);
        ElMessage.success(`已${action}`);
        load();
      } catch (e) {
        ElMessage.error(e.message);
      }
    })
    .catch(() => {});
}

onMounted(async () => {
  const a = await pageMyActivitiesApi({ pageNo: 1, pageSize: 100 });
  activities.value = a.data.records;
  load();
});
</script>
