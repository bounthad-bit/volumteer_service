<template>
  <div class="page">
    <div class="stat-row">
      <div class="stat-card">
        <div class="stat-value">{{ stats.users }}</div>
        <div class="stat-label">注册用户总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.volunteers }}</div>
        <div class="stat-label">志愿者人数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value warn">{{ stats.pending }}</div>
        <div class="stat-label">待审核活动</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.activities }}</div>
        <div class="stat-label">活动总数</div>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header><span class="page-title">待审核活动</span></template>
          <el-empty v-if="!pendingList.length" description="暂无待审核活动" />
          <el-table v-else :data="pendingList" border stripe>
            <el-table-column prop="title" label="活动名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="organizerName" label="组织者" width="110" />
            <el-table-column label="活动时间" min-width="170">
              <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="90" align="center">
              <template #default>
                <el-button link type="primary" @click="$router.push('/activities')">去审核</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card shadow="never">
          <template #header><span class="page-title">最新公告</span></template>
          <el-empty v-if="!notices.length" description="暂无公告" />
          <div v-for="n in notices" :key="n.id" class="dash-notice">
            <div class="dash-notice-title">{{ n.title }}</div>
            <div class="dash-notice-time">{{ formatDateTime(n.createTime) }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { pageUsersApi } from "../api/user";
import { pageAdminActivitiesApi } from "../api/activity";
import { pageNoticesApi } from "../api/notice";
import { formatDateTime } from "../utils/format";

const stats = reactive({ users: 0, volunteers: 0, pending: 0, activities: 0 });
const pendingList = ref([]);
const notices = ref([]);

onMounted(async () => {
  const users = await pageUsersApi({ pageNo: 1, pageSize: 1 });
  stats.users = users.data.total;
  const vols = await pageUsersApi({ pageNo: 1, pageSize: 1, role: 1 });
  stats.volunteers = vols.data.total;

  const all = await pageAdminActivitiesApi({ pageNo: 1, pageSize: 100 });
  stats.activities = all.data.total;
  const pending = await pageAdminActivitiesApi({ pageNo: 1, pageSize: 5, status: 0 });
  stats.pending = pending.data.total;
  pendingList.value = pending.data.records;

  const n = await pageNoticesApi({ pageNo: 1, pageSize: 5 });
  notices.value = n.data.records;
});
</script>

<style scoped>
.stat-value.warn {
  color: #e6a23c;
}

.dash-notice {
  padding: 12px 0;
  border-bottom: 1px dashed #e4e7ed;
}

.dash-notice:last-child {
  border-bottom: none;
}

.dash-notice-title {
  font-size: 14px;
  margin-bottom: 5px;
  color: #303133;
}

.dash-notice-time {
  font-size: 12px;
  color: #a8abb2;
}
</style>
