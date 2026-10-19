<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="按活动名称搜索" clearable style="width: 220px" @keyup.enter="load" />
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px" @change="load">
        <el-option v-for="s in ACTIVITY_STATUS" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe>
      <el-table-column label="封面" width="110" align="center">
        <template #default="{ row }">
          <img :src="row.coverUrl || FALLBACK_COVER" class="cover-thumb" :alt="row.title" loading="lazy" />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="活动名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="organizerName" label="组织者" width="110" />
      <el-table-column prop="location" label="地点" min-width="120" show-overflow-tooltip />
      <el-table-column label="活动时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
      </el-table-column>
      <el-table-column label="名额" width="80" align="center" prop="maxNum" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="activityStatusType(row.status)" size="small">{{ row.statusLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="detail(row)">详情</el-button>
          <template v-if="row.status === 0">
            <el-button link type="success" @click="audit(row, true)">通过</el-button>
            <el-button link type="danger" @click="audit(row, false)">驳回</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination background layout="total, prev, pager, next" :total="total" :page-size="query.pageSize"
                     :current-page="query.pageNo" @current-change="onPage" />
    </div>

    <el-dialog v-model="visible" title="活动详情" width="560px">
      <img v-if="current" :src="current.coverUrl || FALLBACK_COVER" class="cover-preview" :alt="current.title" />
      <el-descriptions v-if="current" :column="1" border>
        <el-descriptions-item label="活动名称">{{ current.title }}</el-descriptions-item>
        <el-descriptions-item label="组织者">{{ current.organizerName }}</el-descriptions-item>
        <el-descriptions-item label="地点">{{ current.location }}</el-descriptions-item>
        <el-descriptions-item label="时间">
          {{ formatDateTime(current.startTime) }} ~ {{ formatDateTime(current.endTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="招募人数">{{ current.maxNum }}</el-descriptions-item>
        <el-descriptions-item label="活动说明">{{ current.description || "无" }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { auditActivityApi, getActivityDetailApi, pageAdminActivitiesApi } from "../api/activity";
import { formatDateTime } from "../utils/format";
import { ACTIVITY_STATUS, activityStatusType } from "../utils/dict";

const FALLBACK_COVER = "/images/hero-volunteers.jpg";
const rows = ref([]);
const total = ref(0);
const loading = ref(false);
const visible = ref(false);
const current = ref(null);
const query = reactive({ pageNo: 1, pageSize: 10, keyword: "", status: null });

async function load() {
  loading.value = true;
  try {
    const res = await pageAdminActivitiesApi({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
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

async function detail(row) {
  const res = await getActivityDetailApi(row.id);
  current.value = res.data;
  visible.value = true;
}

function audit(row, pass) {
  const action = pass ? "通过" : "驳回";
  ElMessageBox.prompt(`请输入${action}意见`, `${action}活动「${row.title}」`, {
    inputValue: pass ? "内容合规，同意发布" : "活动信息不完整，请补充后重新提交"
  })
    .then(async ({ value }) => {
      try {
        await auditActivityApi(row.id, pass, value);
        ElMessage.success(`已${action}`);
        load();
      } catch (e) {
        ElMessage.error(e.message);
      }
    })
    .catch(() => {});
}

onMounted(load);
</script>

<style scoped>
.cover-thumb {
  width: 88px;
  height: 54px;
  object-fit: cover;
  border-radius: 6px;
  display: block;
  margin: 0 auto;
  background: #eef2f7;
}

.cover-preview {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 14px;
  display: block;
  background: #eef2f7;
}
</style>
