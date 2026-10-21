-- ============================================================
-- 校园资源智能预约调度系统 数据库初始化脚本
-- DBMS: MySQL 8.0+
-- Charset: utf8mb4
-- 说明：基于 sql/init.sql，增补 resource_booking.request_id 幂等字段
-- ============================================================

DROP DATABASE IF EXISTS campus_dispatch;
CREATE DATABASE campus_dispatch DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_dispatch;

-- ============================================================
-- 1. 用户表
-- ============================================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL COMMENT '学号/工号',
    password    VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密密码',
    real_name   VARCHAR(50)  NOT NULL COMMENT '姓名',
    role        TINYINT      NOT NULL DEFAULT 0 COMMENT '0=学生 1=管理员 2=超管',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0=正常 1=封禁',
    ban_until   DATETIME     DEFAULT NULL COMMENT '封禁截止时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 2. 校园资源表
-- ============================================================
DROP TABLE IF EXISTS campus_resource;
CREATE TABLE campus_resource (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL COMMENT '资源名称',
    type        TINYINT      NOT NULL COMMENT '1=教室 2=图书馆座位 3=实验室 4=会议室',
    location    VARCHAR(100) DEFAULT NULL COMMENT '位置',
    capacity    INT          NOT NULL DEFAULT 1 COMMENT '容纳人数',
    open_start  TIME         NOT NULL COMMENT '每日开放开始',
    open_end    TIME         NOT NULL COMMENT '每日开放结束',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0=空闲 1=占用 2=维护中',
    description VARCHAR(255) DEFAULT NULL COMMENT '设备描述',
    deleted     TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_type (type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校园资源表';

-- ============================================================
-- 3. 预约表（核心业务表，含 request_id 幂等字段）
-- ============================================================
DROP TABLE IF EXISTS resource_booking;
CREATE TABLE resource_booking (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    user_id       BIGINT      NOT NULL COMMENT '预约用户',
    resource_id   BIGINT      NOT NULL COMMENT '预约资源',
    request_id    VARCHAR(64) DEFAULT NULL COMMENT '幂等凭证',
    start_time    DATETIME    NOT NULL COMMENT '预约开始',
    end_time      DATETIME    NOT NULL COMMENT '预约结束',
    status        TINYINT     NOT NULL DEFAULT 0 COMMENT '0=待签到 1=使用中 2=已完成 3=已取消 4=违约',
    check_in_time DATETIME    DEFAULT NULL COMMENT '签到时间',
    deleted       TINYINT     NOT NULL DEFAULT 0,
    create_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_request_id (request_id),
    KEY idx_resource_slot (resource_id, start_time, end_time),
    KEY idx_user_time (user_id, create_time),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- ============================================================
-- 4. 违规记录表
-- ============================================================
DROP TABLE IF EXISTS user_violation;
CREATE TABLE user_violation (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL,
    booking_id  BIGINT      NOT NULL,
    type        TINYINT     NOT NULL COMMENT '1=超时未签到 2=早退 3=恶意占座',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='违规记录';

-- ============================================================
-- 5. 消息通知表
-- ============================================================
DROP TABLE IF EXISTS message_notice;
CREATE TABLE message_notice (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    title       VARCHAR(100) NOT NULL,
    content     VARCHAR(500) NOT NULL,
    type        TINYINT      NOT NULL COMMENT '1=预约成功 2=到期提醒 3=违约通知 4=系统公告',
    is_read     TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知';

-- ============================================================
-- 6. 资源分时统计表
-- ============================================================
DROP TABLE IF EXISTS resource_statistics;
CREATE TABLE resource_statistics (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    resource_id      BIGINT         NOT NULL,
    stat_date        DATE           NOT NULL,
    hour             TINYINT        NOT NULL COMMENT '0-23',
    booking_count    INT            NOT NULL DEFAULT 0,
    utilization_rate DECIMAL(5,2)   NOT NULL DEFAULT 0.00 COMMENT '使用率%',
    PRIMARY KEY (id),
    UNIQUE KEY uk_resource_date_hour (resource_id, stat_date, hour)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源分时统计';

-- ============================================================
-- 7. AI 人流预测表
-- ============================================================
DROP TABLE IF EXISTS resource_forecast;
CREATE TABLE resource_forecast (
    id            BIGINT   NOT NULL AUTO_INCREMENT,
    resource_id   BIGINT   NOT NULL,
    forecast_time DATETIME NOT NULL,
    heat_value    INT      NOT NULL COMMENT '预测热度 0-100',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_resource_time (resource_id, forecast_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人流预测结果';

-- ============================================================
-- 8. 系统参数表
-- ============================================================
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    config_key   VARCHAR(50)  NOT NULL,
    config_value VARCHAR(255) NOT NULL,
    remark       VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE KEY uk_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 超管账号：admin / 123456
INSERT INTO sys_user (username, password, real_name, role) VALUES
('admin', '$2a$10$8K2m//QckmcF0o/PtiNE9eQrw3IntOBXkcgB4vSo8uWFhz5.c.IWW', '超级管理员', 2);

-- 示例资源
INSERT INTO campus_resource (name, type, location, capacity, open_start, open_end, description) VALUES
('图书馆3楼A001', 2, '图书馆3楼', 1, '07:00', '22:00', '靠窗座位，有插座'),
('图书馆3楼A002', 2, '图书馆3楼', 1, '07:00', '22:00', '普通座位'),
('教学楼A101', 1, '教学楼A座', 60, '07:00', '22:00', '多媒体教室'),
('教学楼A102', 1, '教学楼A座', 40, '07:00', '22:00', '普通教室'),
('实验楼B201', 3, '实验楼B座', 30, '08:00', '20:00', '计算机实验室，配台式机'),
('行政楼会议室301', 4, '行政楼3楼', 20, '08:30', '18:00', '投影仪 + 视频会议系统');

-- 系统参数
INSERT INTO sys_config (config_key, config_value, remark) VALUES
('booking.max_daily_per_user', '3', '每人每日最大预约次数'),
('booking.max_duration_hours', '4', '单次最长预约小时数'),
('booking.overtime_minutes', '15', '开始后多少分钟未签到判违约'),
('booking.remind_minutes', '30', '预约到期前多少分钟提醒'),
('booking.ban_threshold', '3', '累计违约几次封禁'),
('booking.ban_days', '7', '封禁天数');