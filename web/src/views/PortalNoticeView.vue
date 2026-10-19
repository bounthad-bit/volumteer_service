<template>
  <div class="pn-page">
    <div class="pn-hero">
      <div class="pn-hero-inner">
        <h1>公告资讯</h1>
        <p>平台最新通知与志愿服务相关说明</p>
      </div>
    </div>

    <div class="pn-body" v-loading="loading">
      <el-empty v-if="!rows.length && !loading" description="暂无公告" />
      <div v-for="n in rows" :key="n.id" class="pn-card">
        <div class="pn-card-head">
          <h2 class="pn-title">{{ n.title }}</h2>
          <span class="pn-meta">{{ n.adminName }} · {{ formatDateTime(n.createTime) }}</span>
        </div>
        <div class="pn-content">{{ n.content }}</div>
      </div>

      <div class="pager">
        <el-pagination background layout="total, prev, pager, next" :total="total" :page-size="query.pageSize"
                       :current-page="query.pageNo" @current-change="onPage" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { pageNoticesApi } from "../api/notice";
import { formatDateTime } from "../utils/format";

const rows = ref([]);
const total = ref(0);
const loading = ref(false);
const query = reactive({ pageNo: 1, pageSize: 5 });

async function load() {
  loading.value = true;
  try {
    const res = await pageNoticesApi({ pageNo: query.pageNo, pageSize: query.pageSize });
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

onMounted(load);
</script>

<style scoped>
.pn-page {
  min-height: 60vh;
}

.pn-hero {
  background: linear-gradient(135deg, #0b2a6b, #0e7490);
  color: #fff;
  padding: 52px 24px 46px;
}

.pn-hero-inner {
  max-width: 960px;
  margin: 0 auto;
}

.pn-hero h1 {
  margin: 0 0 10px;
  font-size: 30px;
}

.pn-hero p {
  margin: 0;
  color: rgba(255, 255, 255, 0.82);
  font-size: 14px;
}

.pn-body {
  max-width: 960px;
  margin: 0 auto;
  padding: 26px 24px 60px;
}

.pn-card {
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  padding: 24px 26px;
  margin-bottom: 16px;
  transition: box-shadow 0.25s;
}

.pn-card:hover {
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.07);
}

.pn-card-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.pn-title {
  font-size: 17px;
  margin: 0;
  color: #0f172a;
}

.pn-meta {
  font-size: 12px;
  color: #a8abb2;
}

.pn-content {
  color: #475569;
  line-height: 1.9;
  white-space: pre-wrap;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}
</style>
