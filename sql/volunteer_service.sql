-- ============================================================
-- 基于 SpringBoot 的志愿者服务信息系统  数据库脚本
-- 对应开题报告第三章「数据库设计」：6 张核心表
--   sys_user / activity / registration / check_in_record / evaluation / notice
-- 角色在 sys_user.role 字段中区分：1 志愿者、2 活动组织者、3 系统管理员
-- 字符集 utf8mb4，引擎 InnoDB
-- ============================================================

DROP DATABASE IF EXISTS `volunteer_service`;
CREATE DATABASE `volunteer_service` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `volunteer_service`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- 1. 用户表 sys_user
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `username`    VARCHAR(20)  NOT NULL                COMMENT '用户名，唯一',
  `password`    VARCHAR(100) NOT NULL                COMMENT '密码，BCrypt 加密后存储',
  `real_name`   VARCHAR(20)  NOT NULL                COMMENT '真实姓名',
  `phone`       VARCHAR(11)  NOT NULL                COMMENT '手机号，唯一，注册和签到时使用',
  `student_no`  VARCHAR(20)  DEFAULT NULL            COMMENT '学号，可为空',
  `role`        TINYINT      NOT NULL DEFAULT 1      COMMENT '角色：1 志愿者，2 组织者，3 管理员',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：0 禁用，1 正常',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_role_status` (`role`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ------------------------------------------------------------
-- 2. 活动表 activity
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `title`        VARCHAR(50)  NOT NULL                COMMENT '活动名称',
  `description`  TEXT                                 COMMENT '活动说明',
  `cover_url`    VARCHAR(200) DEFAULT NULL            COMMENT '活动封面图路径',
  `location`     VARCHAR(100) NOT NULL                COMMENT '活动地点',
  `start_time`   DATETIME     NOT NULL                COMMENT '活动开始时间',
  `end_time`     DATETIME     NOT NULL                COMMENT '活动结束时间',
  `max_num`      INT          NOT NULL DEFAULT 0      COMMENT '招募人数上限',
  `status`       TINYINT      NOT NULL DEFAULT 0      COMMENT '状态：0 待审核，1 招募中，2 进行中，3 已结束，4 已取消',
  `organizer_id` BIGINT       NOT NULL                COMMENT '组织者 ID，关联 sys_user.id',
  `audit_remark` VARCHAR(200) DEFAULT NULL            COMMENT '审核意见',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (`id`),
  KEY `idx_organizer` (`organizer_id`),
  KEY `idx_status_time` (`status`, `start_time`),
  CONSTRAINT `fk_activity_organizer` FOREIGN KEY (`organizer_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

-- ------------------------------------------------------------
-- 3. 报名表 registration
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `registration`;
CREATE TABLE `registration` (
  `id`             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `activity_id`    BIGINT      NOT NULL                COMMENT '活动 ID，关联 activity.id',
  `user_id`        BIGINT      NOT NULL                COMMENT '志愿者 ID，关联 sys_user.id',
  `status`         TINYINT     NOT NULL DEFAULT 0      COMMENT '状态：0 待审核，1 已通过，2 已拒绝，3 已取消',
  `apply_remark`   VARCHAR(200) DEFAULT NULL           COMMENT '报名备注',
  `review_remark`  VARCHAR(200) DEFAULT NULL           COMMENT '审核意见',
  `register_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reg_activity_user` (`activity_id`, `user_id`),
  KEY `idx_reg_user` (`user_id`),
  CONSTRAINT `fk_reg_activity` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`),
  CONSTRAINT `fk_reg_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报名表';

-- ------------------------------------------------------------
-- 4. 签到记录表 check_in_record
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `check_in_record`;
CREATE TABLE `check_in_record` (
  `id`              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `registration_id` BIGINT   NOT NULL                COMMENT '报名记录 ID，关联 registration.id',
  `activity_id`     BIGINT   NOT NULL                COMMENT '活动 ID，冗余便于统计',
  `user_id`         BIGINT   NOT NULL                COMMENT '志愿者 ID，冗余便于统计',
  `check_in_time`   DATETIME NOT NULL                COMMENT '签到时间',
  `check_out_time`  DATETIME DEFAULT NULL            COMMENT '签退时间，未签退时为空',
  `duration`        INT      DEFAULT NULL            COMMENT '服务时长（分钟），签退时计算写入',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_check_registration` (`registration_id`),
  KEY `idx_check_user` (`user_id`),
  KEY `idx_check_activity` (`activity_id`),
  CONSTRAINT `fk_check_registration` FOREIGN KEY (`registration_id`) REFERENCES `registration` (`id`),
  CONSTRAINT `fk_check_activity` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`),
  CONSTRAINT `fk_check_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签到记录表';

-- ------------------------------------------------------------
-- 5. 评价表 evaluation
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `evaluation`;
CREATE TABLE `evaluation` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `activity_id` BIGINT       NOT NULL                COMMENT '活动 ID，关联 activity.id',
  `user_id`     BIGINT       NOT NULL                COMMENT '志愿者 ID，关联 sys_user.id',
  `score`       TINYINT      NOT NULL                COMMENT '评分，1～5 分',
  `content`     VARCHAR(200) DEFAULT NULL            COMMENT '评价内容，可为空',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_evaluation_activity_user` (`activity_id`, `user_id`),
  KEY `idx_eval_user` (`user_id`),
  CONSTRAINT `fk_eval_activity` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`),
  CONSTRAINT `fk_eval_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- ------------------------------------------------------------
-- 6. 公告表 notice
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `title`       VARCHAR(50) NOT NULL                COMMENT '公告标题',
  `content`     TEXT                                COMMENT '公告内容',
  `admin_id`    BIGINT      NOT NULL                COMMENT '发布人 ID，关联 sys_user.id',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (`id`),
  KEY `idx_notice_admin` (`admin_id`),
  CONSTRAINT `fk_notice_admin` FOREIGN KEY (`admin_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 初始数据
-- 三个内置账号密码均为 123456，BCrypt 密文如下
-- ============================================================

INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `phone`, `student_no`, `role`, `status`, `create_time`) VALUES
(1, 'admin',     '$2b$10$OMJzQg3altiAQSl9De/gAOibOyEnOQnnBs53SosJt/Rv3hY1uFF6S', '系统管理员', '13800000001', NULL, 3, 1, '2026-09-01 09:00:00'),
(2, 'organizer', '$2b$10$OMJzQg3altiAQSl9De/gAOibOyEnOQnnBs53SosJt/Rv3hY1uFF6S', '李明',       '13800000002', NULL, 2, 1, '2026-09-01 09:05:00'),
(3, 'volunteer', '$2b$10$OMJzQg3altiAQSl9De/gAOibOyEnOQnnBs53SosJt/Rv3hY1uFF6S', '王芳',       '13800000003', '20220101', 1, 1, '2026-09-02 10:00:00'),
(4, 'zhangsan',  '$2b$10$OMJzQg3altiAQSl9De/gAOibOyEnOQnnBs53SosJt/Rv3hY1uFF6S', '张三',       '13800000004', '20220102', 1, 1, '2026-09-02 10:10:00'),
(5, 'lisi',      '$2b$10$OMJzQg3altiAQSl9De/gAOibOyEnOQnnBs53SosJt/Rv3hY1uFF6S', '李四',       '13800000005', '20220103', 1, 1, '2026-09-02 10:20:00'),
(6, 'zhaoliu',   '$2b$10$OMJzQg3altiAQSl9De/gAOibOyEnOQnnBs53SosJt/Rv3hY1uFF6S', '赵六',       '13800000006', '20220104', 1, 0, '2026-09-02 10:30:00');

INSERT INTO `activity` (`id`, `title`, `description`, `cover_url`, `location`, `start_time`, `end_time`, `max_num`, `status`, `organizer_id`, `audit_remark`, `create_time`) VALUES
(1, '图书馆图书整理志愿服务',
   '协助图书馆管理员将归还图书按分类号上架，整理书架并引导读者查找图书。', '/images/activity-library.jpg',
   '校图书馆三楼', '2026-09-20 09:00:00', '2026-09-20 12:00:00', 8, 3, 2, '内容合规，同意发布', '2026-09-10 09:00:00'),
(2, '社区义诊现场协助',
   '协助社区医院开展免费血压、血糖测量，负责登记信息、维持现场秩序。', '/images/activity-clinic.jpg',
   '幸福里社区服务中心', '2026-09-22 08:30:00', '2026-09-22 11:30:00', 6, 3, 2, '同意发布', '2026-09-11 10:00:00'),
(3, '敬老院重阳节慰问',
   '前往敬老院陪伴老人聊天、表演节目，协助工作人员整理房间。', '/images/activity-elderly.jpg',
   '康乐敬老院', '2026-10-08 14:00:00', '2026-10-08 17:00:00', 10, 3, 2, '同意发布', '2026-09-25 09:30:00'),
(4, '校园运动会后勤保障',
   '为校运动会提供饮水补给、检录引导、场地秩序维护等后勤服务。', '/images/activity-sports.jpg',
   '学校田径场', '2026-10-15 08:00:00', '2026-10-15 17:00:00', 20, 1, 2, '同意发布', '2026-10-01 09:00:00'),
(5, '垃圾分类宣传进社区',
   '向社区居民发放宣传手册，讲解垃圾分类知识，协助开展互动小游戏。', '/images/activity-recycle.jpg',
   '阳光家园小区广场', '2026-10-20 09:00:00', '2026-10-20 12:00:00', 12, 1, 2, '同意发布', '2026-10-03 10:00:00'),
(6, '山区小学支教物资整理',
   '整理并打包捐赠的图书、文具和衣物，登记物资清单准备发往山区小学。', '/images/activity-donation.jpg',
   '校志愿服务中心仓库', '2026-10-25 13:30:00', '2026-10-25 17:30:00', 15, 0, 2, NULL, '2026-10-07 11:00:00');

INSERT INTO `registration` (`id`, `activity_id`, `user_id`, `status`, `review_remark`, `register_time`) VALUES
(1, 1, 3, 1, '通过', '2026-09-12 10:00:00'),
(2, 1, 4, 1, '通过', '2026-09-12 10:05:00'),
(3, 2, 3, 1, '通过', '2026-09-13 09:00:00'),
(4, 2, 5, 1, '通过', '2026-09-13 09:10:00'),
(5, 3, 3, 1, '通过', '2026-09-26 09:00:00'),
(6, 3, 4, 1, '通过', '2026-09-26 09:05:00'),
(7, 3, 5, 1, '通过', '2026-09-26 09:10:00'),
(8, 4, 3, 0, NULL, '2026-10-08 10:00:00'),
(9, 4, 4, 0, NULL, '2026-10-08 10:05:00'),
(10, 5, 5, 0, NULL, '2026-10-08 11:00:00');

-- 已签退的记录带服务时长；活动 3 有一条仅签到未签退的记录，便于演示签退流程
INSERT INTO `check_in_record` (`id`, `registration_id`, `activity_id`, `user_id`, `check_in_time`, `check_out_time`, `duration`) VALUES
(1, 1, 1, 3, '2026-09-20 08:55:00', '2026-09-20 12:05:00', 190),
(2, 2, 1, 4, '2026-09-20 08:58:00', '2026-09-20 12:02:00', 184),
(3, 3, 2, 3, '2026-09-22 08:25:00', '2026-09-22 11:35:00', 190),
(4, 4, 2, 5, '2026-09-22 08:30:00', '2026-09-22 11:30:00', 180),
(5, 5, 3, 3, '2026-10-08 13:55:00', NULL, NULL),
(6, 6, 3, 4, '2026-10-08 13:58:00', '2026-10-08 17:02:00', 184),
(7, 7, 3, 5, '2026-10-08 14:00:00', '2026-10-08 17:00:00', 180);

INSERT INTO `evaluation` (`id`, `activity_id`, `user_id`, `score`, `content`, `create_time`) VALUES
(1, 1, 3, 5, '活动组织得很好，分工明确，收获很大。', '2026-09-20 15:00:00'),
(2, 1, 4, 4, '图书馆老师很耐心，就是书有点多，希望下次人手再充足些。', '2026-09-20 15:10:00'),
(3, 2, 3, 5, '很有意义的一次志愿服务，社区老人特别热情。', '2026-09-22 14:00:00'),
(4, 3, 4, 4, '陪老人聊天很暖心，建议以后可以多准备一些文艺节目。', '2026-10-08 19:00:00');

INSERT INTO `notice` (`id`, `title`, `content`, `admin_id`, `create_time`) VALUES
(1, '关于规范志愿服务活动报名的通知',
   '为保证志愿服务活动有序开展，请各位志愿者在报名后按时参加活动，如因故无法参加，请在活动开始前 24 小时取消报名。活动当天请在活动详情页完成签到与签退，服务时长以签到记录为准。',
   1, '2026-09-15 09:00:00'),
(2, '志愿服务时长统计说明',
   '系统会在志愿者签退时自动计算本次服务时长并累计到个人档案。若发现时长记录有误，请联系活动组织者核实后由管理员统一更正。',
   1, '2026-09-28 10:00:00'),
(3, '十月志愿服务活动安排',
   '十月将开展校园运动会后勤保障、垃圾分类宣传进社区、山区小学支教物资整理等多项活动，欢迎大家关注活动列表并踊跃报名。',
   1, '2026-10-02 09:30:00');
