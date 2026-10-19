<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="按活动名称搜索" clearable style="width: 220px" @keyup.enter="load" />
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px" @change="load">
        <el-option v-for="s in ACTIVITY_STATUS" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <div class="spacer" />
      <el-button type="primary" @click="openCreate">发布活动</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe>
      <el-table-column label="封面" width="110" align="center">
        <template #default="{ row }">
          <img :src="row.coverUrl || FALLBACK_COVER" class="cover-thumb" :alt="row.title" loading="lazy" />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="活动名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="location" label="地点" min-width="120" show-overflow-tooltip />
      <el-table-column label="活动时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
      </el-table-column>
      <el-table-column label="报名/名额" width="110" align="center">
        <template #default="{ row }">{{ row.approvedCount }} / {{ row.maxNum }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="activityStatusType(row.status)" size="small">{{ row.statusLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核意见" min-width="130" show-overflow-tooltip>
        <template #default="{ row }">{{ emptyText(row.auditRemark) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/activities/detail/${row.id}`)">详情</el-button>
          <el-button v-if="row.status !== 3 && row.status !== 4" link type="primary" @click="openEdit(row)">
            编辑
          </el-button>
          <el-button v-if="row.status !== 3 && row.status !== 4" link type="danger" @click="cancel(row)">
            取消活动
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination background layout="total, prev, pager, next" :total="total" :page-size="query.pageSize"
                     :current-page="query.pageNo" @current-change="onPage" />
    </div>

    <el-dialog v-model="visible" :title="form.id ? '编辑活动' : '发布活动'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="活动名称" prop="title">
          <el-input v-model="form.title" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="form.location" maxlength="100" />
        </el-form-item>
        <el-form-item label="活动时间" prop="range">
          <el-date-picker v-model="form.range" type="datetimerange" range-separator="至"
                          start-placeholder="开始时间" end-placeholder="结束时间"
                          value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="招募人数" prop="maxNum">
          <el-input-number v-model="form.maxNum" :min="1" :max="500" />
        </el-form-item>
        <el-form-item label="活动封面">
          <div style="width: 100%">
            <el-input v-model="form.coverUrl" placeholder="封面图路径，如 /images/activity-library.jpg（选填）" />
            <img v-if="form.coverUrl" :src="form.coverUrl" class="cover-form-preview" alt="封面预览" />
          </div>
        </el-form-item>
        <el-form-item label="活动说明">
          <el-input v-model="form.description" type="textarea" :rows="4" maxlength="2000" show-word-limit />
        </el-form-item>
        <el-alert type="info" :closable="false" show-icon
                  title="提交后活动将进入「待审核」，管理员审核通过后方可对志愿者可见。" />
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  cancelActivityApi,
  createActivityApi,
  pageMyActivitiesApi,
  updateActivityApi
} from "../api/activity";
import { formatDateTime, emptyText } from "../utils/format";
import { ACTIVITY_STATUS, activityStatusType } from "../utils/dict";

const FALLBACK_COVER = "/images/hero-volunteers.jpg";
const rows = ref([]);
const total = ref(0);
const loading = ref(false);
const saving = ref(false);
const visible = ref(false);
const formRef = ref();
const query = reactive({ pageNo: 1, pageSize: 10, keyword: "", status: null });
const form = reactive({ id: null, title: "", location: "", range: [], maxNum: 10, description: "", coverUrl: "" });

const rules = {
  title: [{ required: true, message: "请输入活动名称", trigger: "blur" }],
  location: [{ required: true, message: "请输入活动地点", trigger: "blur" }],
  range: [{ required: true, type: "array", message: "请选择活动时间", trigger: "change" }],
  maxNum: [{ required: true, message: "请填写招募人数", trigger: "blur" }]
};

async function load() {
  loading.value = true;
  try {
    const res = await pageMyActivitiesApi({
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

function openCreate() {
  Object.assign(form, { id: null, title: "", location: "", range: [], maxNum: 10, description: "", coverUrl: "" });
  visible.value = true;
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    title: row.title,
    location: row.location,
    range: [row.startTime, row.endTime],
    maxNum: row.maxNum,
    description: row.description || "",
    coverUrl: row.coverUrl || ""
  });
  visible.value = true;
}

function save() {
  formRef.value.validate(async (ok) => {
    if (!ok) return;
    saving.value = true;
    const payload = {
      title: form.title,
      location: form.location,
      startTime: form.range[0],
      endTime: form.range[1],
      maxNum: form.maxNum,
      description: form.description,
      coverUrl: form.coverUrl || null
    };
    try {
      if (form.id) {
        await updateActivityApi(form.id, payload);
        ElMessage.success("活动已更新");
      } else {
        await createActivityApi(payload);
        ElMessage.success("活动已提交，等待管理员审核");
      }
      visible.value = false;
      load();
    } catch (e) {
      ElMessage.error(e.message);
    } finally {
      saving.value = false;
    }
  });
}

function cancel(row) {
  ElMessageBox.confirm(`确定取消活动「${row.title}」吗？`, "提示", { type: "warning" })
    .then(async () => {
      try {
        await cancelActivityApi(row.id);
        ElMessage.success("活动已取消");
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

.cover-form-preview {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  margin-top: 10px;
  display: block;
  background: #eef2f7;
}
</style>
