<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="按志愿者姓名搜索" clearable style="width: 220px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <div class="spacer" />
      <el-button type="success" @click="exportCsv">导出 Excel(CSV)</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe>
      <el-table-column type="index" label="排名" width="70" align="center" />
      <el-table-column prop="realName" label="志愿者" width="130" />
      <el-table-column prop="username" label="账号" width="130" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column label="参加活动数" width="120" align="center" prop="activityCount" />
      <el-table-column label="累计服务时长" min-width="160">
        <template #default="{ row }">{{ formatDuration(row.totalMinutes) }}</template>
      </el-table-column>
      <el-table-column label="最近一次服务" min-width="170">
        <template #default="{ row }">{{ row.lastServiceTime ? formatDateTime(row.lastServiceTime) : "-" }}</template>
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
import { exportHoursApi, pageHoursStatsApi } from "../api/statistics";
import { formatDateTime, formatDuration } from "../utils/format";

const rows = ref([]);
const total = ref(0);
const loading = ref(false);
const query = reactive({ pageNo: 1, pageSize: 10, keyword: "" });

async function load() {
  loading.value = true;
  try {
    const res = await pageHoursStatsApi({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined
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

async function exportCsv() {
  try {
    const res = await exportHoursApi({ keyword: query.keyword || undefined });
    const lines = ["志愿者,账号,手机号,参加活动数,累计服务时长(分钟),累计服务时长,最近一次服务"];
    res.data.forEach((r) => {
      lines.push(
        [r.realName, r.username, r.phone, r.activityCount, r.totalMinutes, formatDuration(r.totalMinutes),
          r.lastServiceTime || ""]
          .map((v) => `"${String(v ?? "").replace(/"/g, '""')}"`)
          .join(",")
      );
    });
    const blob = new Blob(["﻿" + lines.join("\n")], { type: "text/csv;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `志愿者服务时长统计_${new Date().toISOString().slice(0, 10)}.csv`;
    a.click();
    URL.revokeObjectURL(url);
    ElMessage.success("已导出，可直接用 Excel 打开");
  } catch (e) {
    ElMessage.error(e.message);
  }
}

onMounted(load);
</script>
