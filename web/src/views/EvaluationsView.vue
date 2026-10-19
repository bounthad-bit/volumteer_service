<template>
  <div class="page">
    <div class="toolbar">
      <el-select v-model="query.activityId" placeholder="全部活动" clearable style="width: 260px" @change="reload">
        <el-option v-for="a in activities" :key="a.id" :label="a.title" :value="a.id" />
      </el-select>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe>
      <el-table-column prop="activityTitle" label="活动名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="realName" label="评价人" width="120" />
      <el-table-column label="评分" width="180" align="center">
        <template #default="{ row }">
          <el-rate :model-value="row.score" disabled size="small" show-score />
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评价内容" min-width="240" show-overflow-tooltip>
        <template #default="{ row }">{{ emptyText(row.content) }}</template>
      </el-table-column>
      <el-table-column label="评价时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
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
import { ElMessage } from "element-plus";
import { pageMyActivitiesApi } from "../api/activity";
import { pageOrganizerEvaluationsApi } from "../api/evaluation";
import { formatDateTime, emptyText } from "../utils/format";

const rows = ref([]);
const activities = ref([]);
const total = ref(0);
const loading = ref(false);
const query = reactive({ pageNo: 1, pageSize: 10, activityId: null });

function reload() {
  query.pageNo = 1;
  load();
}

async function load() {
  loading.value = true;
  try {
    const res = await pageOrganizerEvaluationsApi({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      activityId: query.activityId ?? undefined
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

onMounted(async () => {
  const a = await pageMyActivitiesApi({ pageNo: 1, pageSize: 100 });
  activities.value = a.data.records;
  load();
});
</script>
