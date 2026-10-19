<template>
  <div class="page">
    <div class="toolbar">
      <el-select v-model="query.activityId" placeholder="请选择活动" style="width: 260px" @change="reload">
        <el-option v-for="a in activities" :key="a.id" :label="a.title" :value="a.id" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <div class="spacer" />
      <el-tag v-if="summaryCount" type="info">共 {{ summaryCount }} 条签到记录，累计 {{ formatDuration(summaryMinutes) }}</el-tag>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe>
      <el-table-column prop="activityTitle" label="活动名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="realName" label="志愿者" width="120" />
      <el-table-column label="签到时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.checkInTime) }}</template>
      </el-table-column>
      <el-table-column label="签退时间" min-width="170">
        <template #default="{ row }">{{ row.checkOutTime ? formatDateTime(row.checkOutTime) : "未签退" }}</template>
      </el-table-column>
      <el-table-column label="服务时长" width="140" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.duration === null" type="warning" size="small">待签退</el-tag>
          <span v-else>{{ row.durationText }}</span>
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
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { pageMyActivitiesApi } from "../api/activity";
import { pageActivityCheckInsApi } from "../api/checkin";
import { formatDateTime, formatDuration } from "../utils/format";

const rows = ref([]);
const activities = ref([]);
const total = ref(0);
const loading = ref(false);
const query = reactive({ pageNo: 1, pageSize: 10, activityId: null });

const summaryCount = computed(() => rows.value.length);
const summaryMinutes = computed(() => rows.value.reduce((s, r) => s + (r.duration || 0), 0));

function reload() {
  query.pageNo = 1;
  load();
}

async function load() {
  if (!query.activityId) {
    rows.value = [];
    total.value = 0;
    return;
  }
  loading.value = true;
  try {
    const res = await pageActivityCheckInsApi(query.activityId, { pageNo: query.pageNo, pageSize: query.pageSize });
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

onMounted(async () => {
  const a = await pageMyActivitiesApi({ pageNo: 1, pageSize: 100 });
  activities.value = a.data.records;
});
</script>
