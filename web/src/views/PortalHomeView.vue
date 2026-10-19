<template>
  <div>
    <section class="hero">
      <div class="hero-bg"></div>
      <div class="hero-inner">
        <div class="hero-left">
          <div class="hero-badge">志愿服务 · 数字化管理</div>
          <h1 class="hero-title">
            让每一次志愿服务<br />都被<span class="hl">认真记录</span>
          </h1>
          <p class="hero-desc">
            面向高校社团与社区的志愿者服务信息系统，把活动发布、报名审核、签到签退、时长统计全部搬到线上，
            志愿者和组织者都能随时掌握自己的服务轨迹。
          </p>
          <div class="hero-actions">
            <el-button v-if="!auth.isLogin" type="primary" size="large" round class="cta" @click="$router.push('/register')">
              成为志愿者
            </el-button>
            <el-button v-else type="primary" size="large" round class="cta" @click="$router.push('/activities')">
              浏览志愿活动
            </el-button>
            <el-button size="large" round class="cta-ghost" @click="$router.push('/activities')">
              查看全部活动
            </el-button>
          </div>

          <div class="hero-stats">
            <div class="hero-stat">
              <div class="hero-stat-num">{{ stats.activities }}</div>
              <div class="hero-stat-label">已发布志愿活动</div>
            </div>
            <div class="hero-stat">
              <div class="hero-stat-num">{{ stats.recruiting }}</div>
              <div class="hero-stat-label">正在招募中</div>
            </div>
            <div class="hero-stat">
              <div class="hero-stat-num">{{ stats.notices }}</div>
              <div class="hero-stat-label">平台公告</div>
            </div>
          </div>
        </div>

        <div class="hero-right">
          <div class="glass-card">
            <div class="glass-title">最新志愿活动</div>
            <div v-if="!latest.length" class="glass-empty">暂无可报名的活动</div>
            <div v-for="a in latest" :key="a.id" class="glass-item" @click="$router.push(`/activities/detail/${a.id}`)">
              <div class="glass-item-main">
                <div class="glass-item-title">{{ a.title }}</div>
                <div class="glass-item-meta">
                  <el-icon><Location /></el-icon>{{ a.location }}
                  <el-tag :type="activityStatusType(a.status)" size="small" effect="dark">{{ a.statusLabel }}</el-tag>
                </div>
              </div>
              <el-icon class="glass-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="section-inner">
        <div class="section-head">
          <div class="section-eyebrow">核心功能</div>
          <h2 class="section-title">一条完整的志愿服务业务线</h2>
          <p class="section-sub">从活动发布到服务时长沉淀，七个模块相互衔接，不需要任何线下补录</p>
        </div>
        <div class="feature-grid">
          <div v-for="f in features" :key="f.title" class="feature-card">
            <div class="feature-icon" :style="{ background: f.bg }">
              <el-icon :size="22"><component :is="f.icon" /></el-icon>
            </div>
            <div class="feature-title">{{ f.title }}</div>
            <div class="feature-desc">{{ f.desc }}</div>
          </div>
        </div>
      </div>
    </section>

    <section class="section alt">
      <div class="section-inner">
        <div class="section-head">
          <div class="section-eyebrow">参与流程</div>
          <h2 class="section-title">四步完成一次志愿服务</h2>
        </div>
        <div class="flow">
          <div v-for="(step, i) in flows" :key="step.title" class="flow-item">
            <div class="flow-num">{{ i + 1 }}</div>
            <div class="flow-title">{{ step.title }}</div>
            <div class="flow-desc">{{ step.desc }}</div>
            <div v-if="i < flows.length - 1" class="flow-line"></div>
          </div>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="section-inner">
        <div class="section-head">
          <div class="section-eyebrow">活动速览</div>
          <h2 class="section-title">近期志愿活动</h2>
          <p class="section-sub">登录后即可报名参加，活动名额与报名进度实时同步</p>
        </div>
        <div class="act-grid">
          <div v-for="a in activities" :key="a.id" class="act-card" @click="$router.push(`/activities/detail/${a.id}`)">
            <div class="act-cover">
              <img :src="a.coverUrl || FALLBACK_COVER" :alt="a.title" class="act-cover-img" loading="lazy" />
              <span class="act-cover-tag">{{ a.statusLabel }}</span>
            </div>
            <div class="act-body">
              <div class="act-title">{{ a.title }}</div>
              <div class="act-desc">{{ a.description || "暂无活动说明" }}</div>
              <div class="act-meta">
                <span><el-icon><Location /></el-icon>{{ a.location }}</span>
                <span><el-icon><User /></el-icon>{{ a.approvedCount }} / {{ a.maxNum }} 人</span>
              </div>
              <div class="act-time">
                <el-icon><Clock /></el-icon>{{ formatDateTime(a.startTime) }}
              </div>
            </div>
          </div>
          <el-empty v-if="!activities.length" description="暂无活动" style="grid-column: 1 / -1" />
        </div>
      </div>
    </section>

    <section class="section alt">
      <div class="section-inner">
        <div class="section-head">
          <div class="section-eyebrow">公告资讯</div>
          <h2 class="section-title">平台公告</h2>
        </div>
        <div class="notice-list">
          <div v-for="n in notices" :key="n.id" class="notice-row">
            <div class="notice-date">{{ formatDateTime(n.createTime).slice(0, 10) }}</div>
            <div class="notice-body">
              <div class="notice-title">{{ n.title }}</div>
              <div class="notice-text">{{ n.content }}</div>
            </div>
          </div>
          <el-empty v-if="!notices.length" description="暂无公告" />
        </div>
      </div>
    </section>

    <section class="join">
      <div class="join-inner">
        <h2 class="join-title">加入我们，让服务时长成为你成长的刻度</h2>
        <p class="join-desc">注册成为志愿者，浏览并报名志愿活动，签到签退后系统自动累计服务时长。</p>
        <el-button v-if="!auth.isLogin" type="primary" size="large" round class="join-btn" @click="$router.push('/register')">
          立即注册
        </el-button>
        <el-button v-else size="large" round class="join-btn ghost" @click="$router.push('/activities')">
          去参加活动
        </el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, markRaw } from "vue";
import {
  Location, User, Clock, ArrowRight, Calendar, Finished, AlarmClock,
  DataAnalysis, ChatDotRound, Bell, Stamp
} from "@element-plus/icons-vue";
import { pagePublicActivitiesApi } from "../api/activity";
import { pageNoticesApi } from "../api/notice";
import { formatDateTime } from "../utils/format";
import { activityStatusType } from "../utils/dict";
import { useAuthStore } from "../stores/auth";

const FALLBACK_COVER = "/images/hero-volunteers.jpg";
const auth = useAuthStore();
const latest = ref([]);
const activities = ref([]);
const notices = ref([]);
const stats = reactive({ activities: 0, recruiting: 0, notices: 0 });

const features = [
  { title: "活动发布与审核", desc: "组织者发布活动，管理员审核后上线，从源头保障活动质量。", icon: markRaw(Calendar), bg: "linear-gradient(135deg,#2563eb,#60a5fa)" },
  { title: "在线报名与审核", desc: "志愿者在线报名，组织者逐条审核，名额上限自动控制。", icon: markRaw(Finished), bg: "linear-gradient(135deg,#0891b2,#22d3ee)" },
  { title: "签到签退防代签", desc: "活动当天签到签退，手机号二次确认，避免代签情况。", icon: markRaw(AlarmClock), bg: "linear-gradient(135deg,#7c3aed,#a78bfa)" },
  { title: "服务时长自动统计", desc: "签退即自动计算本次时长并累计，个人档案随时可查。", icon: markRaw(DataAnalysis), bg: "linear-gradient(135deg,#059669,#34d399)" },
  { title: "活动评价反馈", desc: "活动结束后志愿者打分评价，帮助组织者持续改进。", icon: markRaw(ChatDotRound), bg: "linear-gradient(135deg,#ea580c,#fb923c)" },
  { title: "公告与通知", desc: "管理员统一发布公告，用户登录后第一时间获取安排。", icon: markRaw(Bell), bg: "linear-gradient(135deg,#db2777,#f472b6)" },
  { title: "用户与权限管理", desc: "志愿者、组织者、管理员三种角色各司其职，权限清晰。", icon: markRaw(Stamp), bg: "linear-gradient(135deg,#334155,#64748b)" }
];

const flows = [
  { title: "浏览活动", desc: "按名称搜索、按状态筛选，查看活动详情与剩余名额" },
  { title: "在线报名", desc: "一键报名后由组织者审核，通过即成为正式志愿者" },
  { title: "签到签退", desc: "活动当天在详情页签到、结束时签退，手机号二次确认" },
  { title: "累计时长", desc: "系统自动计算服务时长并计入个人档案，可随时查看" }
];

onMounted(async () => {
  const act = await pagePublicActivitiesApi({ pageNo: 1, pageSize: 100 });
  activities.value = act.data.records.slice(0, 6);
  stats.activities = act.data.total;
  stats.recruiting = act.data.records.filter((a) => a.status === 1).length;
  latest.value = act.data.records.filter((a) => a.status === 1).slice(0, 4);
  const n = await pageNoticesApi({ pageNo: 1, pageSize: 4 });
  notices.value = n.data.records;
  stats.notices = n.data.total;
});
</script>

<style scoped>
.hero {
  position: relative;
  overflow: hidden;
  background:
    linear-gradient(135deg, rgba(11, 42, 107, 0.94) 0%, rgba(18, 83, 158, 0.88) 45%, rgba(14, 116, 144, 0.86) 100%),
    url("/images/hero-volunteers.jpg") center 35% / cover no-repeat;
  padding: 72px 24px 84px;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(600px 300px at 85% 20%, rgba(56, 189, 248, 0.35), transparent 70%),
    radial-gradient(500px 260px at 12% 88%, rgba(99, 102, 241, 0.35), transparent 70%);
}

.hero-inner {
  position: relative;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  gap: 56px;
  align-items: center;
}

.hero-left {
  flex: 1.15;
  color: #fff;
}

.hero-badge {
  display: inline-block;
  padding: 6px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.22);
  font-size: 13px;
  margin-bottom: 20px;
}

.hero-title {
  font-size: 42px;
  line-height: 1.28;
  margin: 0 0 20px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.hero-title .hl {
  color: #7dd3fc;
}

.hero-desc {
  font-size: 15px;
  line-height: 1.9;
  color: rgba(255, 255, 255, 0.82);
  max-width: 560px;
  margin: 0 0 30px;
}

.hero-actions {
  display: flex;
  gap: 14px;
  margin-bottom: 44px;
  flex-wrap: wrap;
}

.cta {
  background: #fff;
  color: #12539e;
  border: none;
  font-weight: 600;
  padding: 0 34px;
}

.cta-ghost {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.35);
}

.hero-stats {
  display: flex;
  gap: 48px;
}

.hero-stat-num {
  font-size: 30px;
  font-weight: 700;
  color: #7dd3fc;
}

.hero-stat-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.75);
  margin-top: 4px;
}

.hero-right {
  flex: 0.85;
}

.glass-card {
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 16px;
  padding: 22px;
  backdrop-filter: blur(14px);
  box-shadow: 0 24px 60px rgba(3, 15, 40, 0.35);
}

.glass-title {
  color: #fff;
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 14px;
}

.glass-empty {
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
  padding: 30px 0;
  text-align: center;
}

.glass-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 13px 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
}

.glass-item:hover {
  background: rgba(255, 255, 255, 0.12);
}

.glass-item-main {
  flex: 1;
  min-width: 0;
}

.glass-item-title {
  color: #fff;
  font-size: 14px;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.glass-item-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.72);
}

.glass-arrow {
  color: rgba(255, 255, 255, 0.6);
}

.section {
  padding: 68px 24px;
}

.section.alt {
  background: #fff;
}

.section-inner {
  max-width: 1200px;
  margin: 0 auto;
}

.section-head {
  text-align: center;
  margin-bottom: 40px;
}

.section-eyebrow {
  display: inline-block;
  font-size: 13px;
  color: #2563eb;
  background: #eaf1ff;
  padding: 5px 14px;
  border-radius: 999px;
  margin-bottom: 14px;
}

.section-title {
  font-size: 28px;
  margin: 0 0 10px;
  color: #0f172a;
}

.section-sub {
  color: #64748b;
  font-size: 14px;
  margin: 0;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.feature-card {
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  padding: 26px 24px;
  transition: all 0.25s;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.09);
  border-color: #dbeafe;
}

.feature-icon {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.feature-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #0f172a;
}

.feature-desc {
  font-size: 13px;
  line-height: 1.8;
  color: #64748b;
}

.flow {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  justify-content: center;
}

.flow-item {
  position: relative;
  flex: 1;
  min-width: 200px;
  text-align: center;
  padding: 0 12px;
}

.flow-num {
  width: 46px;
  height: 46px;
  margin: 0 auto 14px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb, #0ea5e9);
  color: #fff;
  font-weight: 700;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.28);
}

.flow-title {
  font-weight: 600;
  margin-bottom: 8px;
  color: #0f172a;
}

.flow-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.8;
}

.flow-line {
  position: absolute;
  top: 23px;
  right: -12px;
  width: 24px;
  height: 2px;
  background: #dbeafe;
}

.act-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 22px;
}

.act-card {
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s;
}

.act-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.1);
}

.act-cover {
  height: 160px;
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

.act-cover-tag {
  position: absolute;
  left: 14px;
  bottom: 12px;
  background: rgba(255, 255, 255, 0.92);
  color: #1d4ed8;
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 999px;
  font-weight: 600;
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
  margin-bottom: 10px;
}

.act-meta span,
.act-time {
  display: flex;
  align-items: center;
  gap: 5px;
}

.act-time {
  font-size: 12px;
  color: #94a3b8;
}

.notice-list {
  max-width: 900px;
  margin: 0 auto;
}

.notice-row {
  display: flex;
  gap: 22px;
  padding: 20px 0;
  border-bottom: 1px dashed #e2e8f0;
}

.notice-date {
  width: 104px;
  flex-shrink: 0;
  color: #94a3b8;
  font-size: 13px;
  padding-top: 2px;
}

.notice-title {
  font-weight: 600;
  margin-bottom: 6px;
  color: #0f172a;
}

.notice-text {
  font-size: 13px;
  color: #64748b;
  line-height: 1.8;
}

.join {
  background: linear-gradient(135deg, #0b2a6b, #0e7490);
  padding: 64px 24px;
  text-align: center;
  color: #fff;
}

.join-title {
  font-size: 26px;
  margin: 0 0 14px;
}

.join-desc {
  color: rgba(255, 255, 255, 0.8);
  margin: 0 0 28px;
  font-size: 14px;
}

.join-btn {
  background: #fff;
  color: #12539e;
  border: none;
  font-weight: 600;
  padding: 0 38px;
}

.join-btn.ghost {
  background: rgba(255, 255, 255, 0.14);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.4);
}

@media (max-width: 900px) {
  .hero-inner {
    flex-direction: column;
  }
  .hero-title {
    font-size: 32px;
  }
  .hero-stats {
    gap: 28px;
  }
  .flow-line {
    display: none;
  }
}
</style>
