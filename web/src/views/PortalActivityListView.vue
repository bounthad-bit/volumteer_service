<template>
  <div class="pl-page">
    <div class="pl-hero">
      <div class="pl-hero-inner">
        <h1>志愿活动</h1>
        <p>浏览正在招募的志愿活动，登录后即可报名参加</p>
      </div>
    </div>

    <div class="pl-body">
      <div class="filter-bar">
        <el-input v-model="query.keyword" placeholder="按活动名称搜索" clearable style="width: 260px"
                  :prefix-icon="Search" @keyup.enter="reload" />
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 150px" @change="reload">
          <el-option label="招募中" :value="1" />
          <el-option label="进行中" :value="2" />
          <el-option label="已结束" :value="3" />
        </el-select>
        <el-button type="primary" @click="reload">查询</el-button>
        <div class="spacer"></div>
        <span class="filter-hint">共 {{ total }} 个活动</span>
      </div>

      <div v-loading="loading" class="act-grid">
        <div v-for="a in rows" :key="a.id" class="act-card" @click="$router.push(`/activities/detail/${a.id}`)">
          <div class="act-cover">
            <img :src="a.coverUrl || FALLBACK_COVER" :alt="a.title" class="act-cover-img" loading="lazy" />
            <span class="act-cover-tag">{{ a.statusLabel }}</span>
            <span v-if="a.avgScore" class="act-cover-score">{{ a.avgScore.toFixed(1) }} 分</span>
          </div>
          <div class="act-body">
            <div class="act-title">{{ a.title }}</div>
            <div class="act-desc">{{ a.description || "暂无活动说明" }}</div>
            <div class="act-meta">
              <span><el-icon><Location /></el-icon>{{ a.location }}</span>
              <span><el-icon><User /></el-icon>{{ a.approvedCount }} / {{ a.maxNum }}</span>
            </div>
            <div class="act-foot">
              <span class="act-time"><el-icon><Clock /></el-icon>{{ formatDateTime(a.startTime) }}</span>
              <el-tag v-if="a.myRegistrationStatus !== null && a.myRegistrationStatus !== undefined" size="small"
                      :type="registrationStatusType(a.myRegistrationStatus)">
                {{ registrationStatusLabel(a.myRegistrationStatus) }}
              </el-tag>
            </div>
          </div>
        </div>
        <el-empty v-if="!loading && !rows.length" description="没有找到符合条件的活动" style="grid-column: 1 / -1" />
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
import { Search, Location, User, Clock } from "@element-plus/icons-vue";
import { pagePublicActivitiesApi } from "../api/activity";
import { formatDateTime } from "../utils/format";
import { registrationStatusLabel, registrationStatusType } from "../utils/dict";

const FALLBACK_COVER = "/images/hero-volunteers.jpg";
const rows = ref([]);
const total = ref(0);
const loading = ref(false);
const query = reactive({ pageNo: 1, pageSize: 9, keyword: "", status: null });

async function load() {
  loading.value = true;
  try {
    const res = await pagePublicActivitiesApi({
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

function reload() {
  query.pageNo = 1;
  load();
}

function onPage(p) {
  query.pageNo = p;
  load();
}

onMounted(load);
</script>

<style scoped>
.pl-page {
  min-height: 60vh;
}

.pl-hero {
  background: linear-gradient(135deg, #0b2a6b, #0e7490);
  color: #fff;
  padding: 52px 24px 46px;
}

.pl-hero-inner {
  max-width: 1200px;
  margin: 0 auto;
}

.pl-hero h1 {
  margin: 0 0 10px;
  font-size: 30px;
}

.pl-hero p {
  margin: 0;
  color: rgba(255, 255, 255, 0.82);
  font-size: 14px;
}

.pl-body {
  max-width: 1200px;
  margin: 0 auto;
  padding: 26px 24px 60px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  border-radius: 12px;
  padding: 16px 18px;
  margin-bottom: 22px;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.05);
}

.filter-bar .spacer {
  flex: 1;
}

.filter-hint {
  font-size: 13px;
  color: #94a3b8;
}

.act-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 22px;
  min-height: 200px;
}

.act-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s;
  border: 1px solid #eef2f7;
}

.act-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.1);
}

.act-cover {
  height: 150px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #1d4ed8, #0ea5e9 60%, #22d3ee);
}

.act-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.35s;
}

.act-card:hover .act-cover-img {
  transform: scale(1.06);
}

.act-cover::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(2, 8, 23, 0) 45%, rgba(2, 8, 23, 0.55) 100%);
  pointer-events: none;
}

.act-cover-tag,
.act-cover-score {
  position: absolute;
  bottom: 12px;
  background: rgba(255, 255, 255, 0.92);
  color: #1d4ed8;
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 999px;
  font-weight: 600;
}

.act-cover-tag {
  left: 14px;
}

.act-cover-score {
  right: 14px;
  color: #d97706;
}

.act-body {
  padding: 18px 20px 20px;
}

.act-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #0f172a;
}

.act-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.7;
  height: 44px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 14px;
}

.act-meta {
  display: flex;
  gap: 18px;
  font-size: 12px;
  color: #475569;
  margin-bottom: 12px;
}

.act-meta span,
.act-time {
  display: flex;
  align-items: center;
  gap: 5px;
}

.act-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.act-time {
  font-size: 12px;
  color: #94a3b8;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}
</style>
