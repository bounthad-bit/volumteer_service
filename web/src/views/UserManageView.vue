<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索用户名 / 姓名 / 手机号" clearable style="width: 260px"
                @keyup.enter="load" />
      <el-select v-model="query.role" placeholder="全部角色" clearable style="width: 140px" @change="load">
        <el-option v-for="r in ROLE_OPTIONS" :key="r.value" :label="r.label" :value="r.value" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe>
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="realName" label="真实姓名" width="120" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="studentNo" label="学号" width="120">
        <template #default="{ row }">{{ emptyText(row.studentNo) }}</template>
      </el-table-column>
      <el-table-column label="角色" width="120" align="center">
        <template #default="{ row }">
          <el-tag size="small">{{ row.roleLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? "正常" : "已禁用" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center">
        <template #default="{ row }">
          <el-button v-if="row.role !== 3" link :type="row.status === 1 ? 'danger' : 'success'" @click="toggle(row)">
            {{ row.status === 1 ? "禁用账号" : "启用账号" }}
          </el-button>
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
import { changeUserStatusApi, pageUsersApi } from "../api/user";
import { formatDateTime, emptyText } from "../utils/format";
import { ROLE_OPTIONS } from "../utils/dict";

const rows = ref([]);
const total = ref(0);
const loading = ref(false);
const query = reactive({ pageNo: 1, pageSize: 10, keyword: "", role: null });

async function load() {
  loading.value = true;
  try {
    const res = await pageUsersApi({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      role: query.role ?? undefined
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

function toggle(row) {
  const next = row.status === 1 ? 0 : 1;
  const action = next === 0 ? "禁用" : "启用";
  ElMessageBox.confirm(`确定${action}用户「${row.realName || row.username}」吗？`, "提示", { type: "warning" })
    .then(async () => {
      try {
        await changeUserStatusApi(row.id, next);
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
