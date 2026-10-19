<template>
  <div class="page">
    <div class="toolbar">
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px" @change="load">
        <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe>
      <el-table-column prop="activityTitle" label="活动名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="activityLocation" label="地点" min-width="130" show-overflow-tooltip />
      <el-table-column label="活动时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.activityStartTime) }}</template>
      </el-table-column>
      <el-table-column label="报名状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="registrationStatusType(row.status)" size="small">{{ row.statusLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="签到状态" width="130" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.checkInState === null" type="info" size="small">未签到</el-tag>
          <el-tag v-else-if="row.checkInState === 0" type="warning" size="small">已签到未签退</el-tag>
          <el-tag v-else type="success" size="small">已签退</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="服务时长" width="120" align="center">
        <template #default="{ row }">{{ formatDuration(row.duration) }}</template>
      </el-table-column>
      <el-table-column label="审核意见" min-width="130" show-overflow-tooltip>
        <template #default="{ row }">{{ emptyText(row.reviewRemark) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/activities/detail/${row.activityId}`)">详情</el-button>
          <el-button v-if="row.status !== 3 && row.checkInState === null" link type="danger" @click="cancel(row)">
            取消报名
          </el-button>
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
import { cancelMyRegistrationApi, pageMyRegistrationsApi } from "../api/registration";
import { formatDateTime, formatDuration, emptyText } from "../utils/format";
import { registrationStatusType, REGISTRATION_STATUS } from "../utils/dict";

const rows = ref([]);
const total = ref(0);
const loading = ref(false);
const statusOptions = REGISTRATION_STATUS;
const query = reactive({ pageNo: 1, pageSize: 10, status: null });

async function load() {
  loading.value = true;
  try {
    const res = await pageMyRegistrationsApi({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
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

function cancel(row) {
  ElMessageBox.confirm(`确定取消报名「${row.activityTitle}」吗？`, "提示", { type: "warning" })
    .then(async () => {
      try {
        await cancelMyRegistrationApi(row.id);
        ElMessage.success("已取消报名");
        load();
      } catch (e) {
        ElMessage.error(e.message);
      }
    })
    .catch(() => {});
}

onMounted(load);
</script>
