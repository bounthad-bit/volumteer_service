<template>
  <div class="pa-page">
    <div class="pa-hero">
      <div class="pa-hero-inner">
        <h1>关于我们</h1>
        <p>面向高校社团与社区的志愿者服务信息系统</p>
      </div>
    </div>

    <div class="pa-body">
      <el-card shadow="never" class="pa-card">
        <div class="pa-title">系统简介</div>
        <p class="pa-text">
          本系统是一款面向高校学生社团与社区的志愿者服务信息系统，用于把志愿活动的发布、报名审核、签到签退、
          服务时长统计与评价反馈等环节统一搬到线上，解决传统做法中「通知靠群发、报名靠接龙、时长靠人工核对」
          带来的漏报、名额失控、统计出错等问题。
        </p>
        <p class="pa-text">
          系统设置<b>志愿者</b>、<b>活动组织者</b>、<b>系统管理员</b>三种角色：志愿者浏览并报名活动、签到签退后服务时长自动累计；
          组织者发布活动、审核报名、查看本活动的签到记录与评价；管理员审核活动、管理用户、发布公告并按人/按活动统计服务时长。
        </p>
      </el-card>

      <el-card shadow="never" class="pa-card">
        <div class="pa-title">技术架构</div>
        <div class="pa-tech">
          <div v-for="t in techs" :key="t.name" class="pa-tech-item">
            <div class="pa-tech-name">{{ t.name }}</div>
            <div class="pa-tech-desc">{{ t.desc }}</div>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="pa-card">
        <div class="pa-title">功能模块</div>
        <div class="pa-modules">
          <div v-for="m in modules" :key="m" class="pa-module">
            <el-icon><Select /></el-icon>{{ m }}
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { Select } from "@element-plus/icons-vue";

const techs = [
  { name: "SpringBoot 3", desc: "后端主体框架，内嵌 Tomcat，约定优于配置" },
  { name: "Spring Security + JWT", desc: "基于角色的访问控制与无状态登录认证" },
  { name: "MyBatis-Plus", desc: "持久层 ORM，参数预编译防 SQL 注入" },
  { name: "MySQL 8", desc: "关系型数据库，六张核心表支撑完整业务线" },
  { name: "Vue 3 + Vite", desc: "前端框架与极速构建工具" },
  { name: "Element Plus", desc: "前端 UI 组件库，统一交互与视觉规范" }
];

const modules = [
  "用户管理", "志愿活动管理", "活动报名", "签到签退", "服务时长统计", "评价反馈", "公告管理"
];
</script>

<style scoped>
.pa-page {
  min-height: 60vh;
}

.pa-hero {
  background: linear-gradient(135deg, #0b2a6b, #0e7490);
  color: #fff;
  padding: 52px 24px 46px;
}

.pa-hero-inner {
  max-width: 960px;
  margin: 0 auto;
}

.pa-hero h1 {
  margin: 0 0 10px;
  font-size: 30px;
}

.pa-hero p {
  margin: 0;
  color: rgba(255, 255, 255, 0.82);
  font-size: 14px;
}

.pa-body {
  max-width: 960px;
  margin: 0 auto;
  padding: 26px 24px 60px;
}

.pa-card {
  border-radius: 14px;
  border: 1px solid #eef2f7;
  margin-bottom: 18px;
}

.pa-title {
  font-size: 17px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 14px;
}

.pa-text {
  color: #475569;
  line-height: 2;
  margin: 0 0 12px;
  font-size: 14px;
}

.pa-text b {
  color: #2563eb;
}

.pa-tech {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.pa-tech-item {
  background: #f7f9fc;
  border-radius: 10px;
  padding: 16px 18px;
}

.pa-tech-name {
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 6px;
}

.pa-tech-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.7;
}

.pa-modules {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.pa-module {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  background: #eaf1ff;
  color: #1d4ed8;
  border-radius: 999px;
  font-size: 14px;
}
</style>
