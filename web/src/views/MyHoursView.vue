<template>
  <div class="page">
    <div class="stat-row">
      <div class="stat-card">
        <div class="stat-value">{{ formatDuration(profile.totalDuration) }}</div>
        <div class="stat-label">累计服务时长</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ profile.activityCount || 0 }}</div>
        <div class="stat-label">累计参加活动（含未签退）</div>
      </div>
    </div>

    <el-card shadow="never">
      <template #header><span class="page-title">我的签到与服务时长明细</span></template>
      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column prop="activityTitle" label="活动名称" min-width="200" show-overflow-tooltip />
        <el-table-column label="签到时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.checkInTime) }}</template>
        </el-table-column>
        <el-table-column label="签退时间" min-width="170">
          <template #default="{ row }">{{ row.checkOutTime ? formatDateTime(row.checkOutTime) : "未签退" }}</template>
        </el-table-column>
        <el-table-column label="本次时长" width="140" align="center">
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
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { pageMyCheckInsApi } from "../api/checkin";
import { getMyProfileApi } from "../api/user";
import { formatDateTime, formatDuration } from "../utils/format";

const rows = ref([]);
const total = ref(0);
const loading = ref(false);
const profile = ref({});
const query = reactive({ pageNo: 1, pageSize: 10 });

async function load() {
  loading.value = true;
  try {
    const res = await pageMyCheckInsApi({ pageNo: query.pageNo, pageSize: query.pageSize });
    rows.value = res.data.records;
    total.value = res.data.total;
    const p = await getMyProfileApi();
    profile.value = p.data;
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

onMounted(load);
</script>
