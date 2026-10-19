<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="按标题搜索" clearable style="width: 220px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <div class="spacer" />
      <el-button type="primary" @click="openCreate">发布公告</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe>
      <el-table-column prop="title" label="公告标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="content" label="内容摘要" min-width="280" show-overflow-tooltip />
      <el-table-column prop="adminName" label="发布人" width="120" />
      <el-table-column label="发布时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination background layout="total, prev, pager, next" :total="total" :page-size="query.pageSize"
                     :current-page="query.pageNo" @current-change="onPage" />
    </div>

    <el-dialog v-model="visible" :title="form.id ? '编辑公告' : '发布公告'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" />
        </el-form-item>
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
import { createNoticeApi, deleteNoticeApi, pageNoticesApi, updateNoticeApi } from "../api/notice";
import { formatDateTime } from "../utils/format";

const rows = ref([]);
const total = ref(0);
const loading = ref(false);
const visible = ref(false);
const saving = ref(false);
const formRef = ref();
const query = reactive({ pageNo: 1, pageSize: 10, keyword: "" });
const form = reactive({ id: null, title: "", content: "" });

const rules = {
  title: [{ required: true, message: "请输入公告标题", trigger: "blur" }],
  content: [{ required: true, message: "请输入公告内容", trigger: "blur" }]
};

async function load() {
  loading.value = true;
  try {
    const res = await pageNoticesApi({ pageNo: query.pageNo, pageSize: query.pageSize, keyword: query.keyword || undefined });
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
  Object.assign(form, { id: null, title: "", content: "" });
  visible.value = true;
}

function openEdit(row) {
  Object.assign(form, { id: row.id, title: row.title, content: row.content });
  visible.value = true;
}

function save() {
  formRef.value.validate(async (ok) => {
    if (!ok) return;
    saving.value = true;
    try {
      if (form.id) {
        await updateNoticeApi(form.id, { title: form.title, content: form.content });
        ElMessage.success("公告已更新");
      } else {
        await createNoticeApi({ title: form.title, content: form.content });
        ElMessage.success("公告已发布");
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

function remove(row) {
  ElMessageBox.confirm(`确定删除公告「${row.title}」吗？`, "提示", { type: "warning" })
    .then(async () => {
      try {
        await deleteNoticeApi(row.id);
        ElMessage.success("已删除");
        load();
      } catch (e) {
        ElMessage.error(e.message);
      }
    })
    .catch(() => {});
}

onMounted(load);
</script>
