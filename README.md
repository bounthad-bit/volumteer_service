# 基于SpringBoot的志愿者服务信息系统

本科毕业设计项目。做的是一个面向高校学生社团和社区的志愿者服务信息系统，把志愿活动的发布、报名、签到签退、服务时长统计和评价反馈都放到线上，替代原来"微信群发通知 + Excel 接龙报名 + 手工核对时长"的做法。

前后端分离：后端 SpringBoot + Spring Security + JWT + MyBatis-Plus，数据库 MySQL 8；前端 Vue 3 + Vite + Element Plus + Pinia + Axios。

## 写了什么

系统分三种角色，各干各的事：

- **志愿者**：浏览和搜索活动、报名、取消报名、活动当天签到签退、看自己累计的服务时长、给参加过的活动打分评价、维护个人资料
- **活动组织者**：发布活动、编辑或取消活动、审核报名申请、查看自己活动的签到记录和收到的评价
- **系统管理员**：审核组织者发布的活动、管理用户（禁用违规账号）、发布公告、按人汇总服务时长并导出 Excel(CSV)

七个功能模块：用户管理、志愿活动管理、活动报名、签到签退、服务时长统计、评价反馈、公告管理。

几个自己做的时候比较在意的点：

- 报名表和评价表都加了 `(activity_id, user_id)` 唯一索引，防止重复报名、重复评价
- 签到要填报名时登记的手机号做二次确认，防止代签（没做扫码和定位，够用就行）
- 服务时长在签退时自动用签退时间减签到时间算出来写进签到记录，不用手工填
- 活动状态靠定时任务自动推进：招募中 → 进行中 → 已结束
- 权限直接用 `sys_user.role` 一个字段区分，没单独建权限表，减少复杂度

## 数据库

六张表，都在 `sql/volunteer_service.sql` 里（含建表语句和演示数据）：

| 表 | 说明 |
| --- | --- |
| `sys_user` | 用户表，role 字段区分角色（1志愿者 / 2组织者 / 3管理员） |
| `activity` | 活动表，status：0待审核 1招募中 2进行中 3已结束 4已取消 |
| `registration` | 报名表，status：0待审核 1已通过 2已拒绝 3已取消 |
| `check_in_record` | 签到记录，签到/签退时间 + 服务时长（分钟） |
| `evaluation` | 评价表，1~5 分 |
| `notice` | 公告表 |

## 目录结构

```
volunteer-service-system/
├── backend/                     后端 SpringBoot 工程
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/volunteer/
│       │   ├── common/          统一响应、分页、异常处理、角色枚举
│       │   ├── config/          MyBatis-Plus 配置、字段自动填充
│       │   ├── security/        Spring Security + JWT
│       │   └── modules/         auth / user / activity / registration
│       │                        / checkin / evaluation / notice / statistics
│       └── resources/
│           ├── application.yml
│           └── mapper/StatisticsMapper.xml   时长统计 SQL
├── web/                         前端（前台 + 后台两个入口）
│   ├── index.html               前台官网入口
│   ├── admin/                   管理后台入口
│   ├── vite.config.js           前台配置（5173）
│   ├── vite.admin.config.js     管理后台配置（5174）
│   └── src/
│       ├── api/                 按模块封装的接口
│       ├── stores/              登录状态（Pinia）
│       ├── router/              路由 + 角色守卫
│       ├── components/          前台/后台两套布局
│       └── views/               页面
└── sql/volunteer_service.sql    数据库脚本
```

## 怎么跑

### 1. 建库导数据

```bash
mysql -uroot < sql/volunteer_service.sql
```

会重建 `volunteer_service` 库并写入演示数据。

### 2. 起后端

数据库账号默认读环境变量 `DB_USERNAME` / `DB_PASSWORD`，没设就用 `root / 123456`。我这台机器 MySQL root 是空密码，所以启动时显式传空：

```bash
cd backend
DB_USERNAME=root DB_PASSWORD= mvn spring-boot:run
```

也可以直接用 IntelliJ IDEA 打开 `backend` 目录，跑 `VolunteerServiceApplication`。后端跑在 `http://localhost:8080`，接口前缀 `/api/v1`。

### 3. 起前端（两个端口）

前台官网和管理后台是两个独立入口，各占一个端口，所以要开两个终端：

```bash
cd web
npm install

npm run dev          # 前台官网  http://localhost:5173
npm run dev:admin    # 管理后台  http://localhost:5174
```

两边都配了代理，`/api` 自动转发到后端 8080。

生产构建也是两套：`npm run build`（前台 → `web/dist`）、`npm run build:admin`（后台 → `web/dist-admin`）。

## 演示账号

密码都是 `123456`：

| 账号 | 角色 | 从哪登 |
| --- | --- | --- |
| `volunteer` | 志愿者（王芳） | 前台 5173 |
| `organizer` | 组织者（李明） | 前台 5173 |
| `admin` | 系统管理员 | 后台 5174 |

另外还有 `zhangsan`、`lisi` 两个志愿者用来演示多人报名审核，`zhaoliu` 是已禁用账号用来演示禁用状态。

初始数据里有 3 个已结束活动（带时长记录和评价）、2 个招募中的活动、1 个待审核活动。其中「敬老院重阳节慰问」有一条**已签到未签退**的记录，登录 `volunteer` 可以演示签退自动算时长。

## 接口约定

- 统一返回 `{ "code": 200, "msg": "success", "data": ... }`，失败时 code=500、msg 是提示
- 除登录注册和活动/公告的只读接口外，都要带 `Authorization: Bearer <token>`
- 细粒度权限写在 Controller 的 `@PreAuthorize("hasRole('ADMIN')")` 这类注解上
- 密码用 BCrypt 加密存库，查询走 MyBatis-Plus 参数预编译防 SQL 注入

## 关于活动配图

`web/public/images/` 下的 7 张图来自 [Wikimedia Commons](https://commons.wikimedia.org/)，都是公有领域或 CC 授权，用作演示：

| 文件 | 内容 | 授权 |
| --- | --- | --- |
| `activity-library.jpg` | 图书馆书架 | CC BY-SA 4.0 |
| `activity-clinic.jpg` | 测量血压 | CC BY-SA 4.0 |
| `activity-elderly.jpg` | 探访老人 | Public domain |
| `activity-sports.jpg` | 田径场 | CC BY-SA 4.0 |
| `activity-recycle.jpg` | 分类垃圾桶 | CC BY-SA 3.0 |
| `activity-donation.jpg` | 打包捐赠物资 | Public domain |
| `hero-volunteers.jpg` | 志愿者团队（官网头图） | Public domain |

## 说明

按开题报告的要求做的，没引入微服务、消息队列这些对校级/社区级系统来说用不上的东西，重点是把核心业务流程做完整、跑得稳。
