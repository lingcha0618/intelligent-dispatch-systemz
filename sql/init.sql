-- =====================================================================
--  空教室/校园资源预约系统 数据库初始化脚本
--  技术栈：MySQL 8.0
--  说明：本脚本可直接在 MySQL 8.0 中执行 (source init.sql 或粘贴执行)
--  默认库名：campus_booking   字符集：utf8mb4
--  初始账号：
--    超管  admin   / 123456
--    管理员 admin2 / 123456
--    学生  student / 123456
--  (密码均为 BCrypt 加密哈希，对应明文 123456)
-- =====================================================================

DROP DATABASE IF EXISTS campus_booking;
CREATE DATABASE campus_booking
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;
USE campus_booking;

-- ---------------------------------------------------------------------
-- 为避免表间依赖，按「先删从表/后删主表」顺序清理
-- ---------------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS ai_flow_prediction;
DROP TABLE IF EXISTS resource_statistics;
DROP TABLE IF EXISTS message_notice;
DROP TABLE IF EXISTS user_violation;
DROP TABLE IF EXISTS resource_booking;
DROP TABLE IF EXISTS campus_resource;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_dict_item;
DROP TABLE IF EXISTS sys_dict_type;
DROP TABLE IF EXISTS sys_user;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- 1. 用户表（RBAC）
-- =====================================================================
CREATE TABLE sys_user (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  username     VARCHAR(64)   NOT NULL                COMMENT '用户名（登录账号）',
  password     VARCHAR(128)  NOT NULL                COMMENT '密码（BCrypt 加密存储）',
  student_no   VARCHAR(32)   DEFAULT NULL            COMMENT '学号/工号',
  real_name    VARCHAR(64)   DEFAULT NULL            COMMENT '真实姓名',
  phone        VARCHAR(20)   DEFAULT NULL            COMMENT '手机号',
  role_type    TINYINT       NOT NULL DEFAULT 1      COMMENT '角色类型 1学生 2管理员 3超级管理员',
  status       TINYINT       NOT NULL DEFAULT 1      COMMENT '账号状态 0封禁 1正常',
  deleted      TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除 0未删 1已删',
  create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username   (username),
  UNIQUE KEY uk_student_no (student_no),
  KEY idx_role_status (role_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =====================================================================
-- 2. 角色表
-- =====================================================================
CREATE TABLE sys_role (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  role_code   VARCHAR(64)  NOT NULL COMMENT '角色编码，如 ROLE_STUDENT',
  role_name   VARCHAR(64)  NOT NULL COMMENT '角色名称',
  description VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
  status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- =====================================================================
-- 3. 用户-角色关联表
-- =====================================================================
CREATE TABLE sys_user_role (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id     BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  role_id     BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- =====================================================================
-- 4. 权限/接口表（接口权限粒度控制）
-- =====================================================================
CREATE TABLE sys_permission (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  parent_id   BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父权限ID（0为根）',
  perm_name   VARCHAR(64)  NOT NULL COMMENT '权限名称',
  perm_code   VARCHAR(128) NOT NULL COMMENT '权限编码，如 booking:create',
  perm_type   TINYINT      NOT NULL DEFAULT 3 COMMENT '类型 1菜单 2按钮 3接口',
  url         VARCHAR(255) DEFAULT NULL COMMENT '接口路径',
  method      VARCHAR(16)  DEFAULT NULL COMMENT '请求方法 GET/POST/PUT/DELETE',
  sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
  status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_perm_code (perm_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- =====================================================================
-- 5. 角色-权限关联表
-- =====================================================================
CREATE TABLE sys_role_permission (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  role_id       BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  permission_id BIGINT UNSIGNED NOT NULL COMMENT '权限ID',
  create_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_perm (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- =====================================================================
-- 6. 数据字典类型表（状态枚举统一管理）
-- =====================================================================
CREATE TABLE sys_dict_type (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  dict_type   VARCHAR(64)  NOT NULL COMMENT '字典类型编码，如 booking_status',
  dict_name   VARCHAR(64)  NOT NULL COMMENT '字典名称',
  status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  remark      VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典类型表';

-- =====================================================================
-- 7. 数据字典项表
-- =====================================================================
CREATE TABLE sys_dict_item (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  dict_type_id  BIGINT UNSIGNED NOT NULL COMMENT '字典类型ID',
  dict_type     VARCHAR(64)  NOT NULL COMMENT '字典类型编码（冗余，便于查询）',
  label         VARCHAR(64)  NOT NULL COMMENT '字典项标签',
  `value`       VARCHAR(64)  NOT NULL COMMENT '字典项值',
  sort          INT          NOT NULL DEFAULT 0 COMMENT '排序',
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典项表';

-- =====================================================================
-- 8. 校园资源表
-- =====================================================================
CREATE TABLE campus_resource (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  resource_name  VARCHAR(128) NOT NULL COMMENT '资源名称，如 A101教室',
  resource_type  TINYINT      NOT NULL COMMENT '资源类型 1普通教室 2实验室 3会议室 4图书馆座位',
  area           VARCHAR(64)  DEFAULT NULL COMMENT '所属区域/楼栋（用于人流预测聚合）',
  location       VARCHAR(255) DEFAULT NULL COMMENT '详细位置',
  capacity       INT          NOT NULL DEFAULT 0 COMMENT '容纳人数',
  open_time      TIME         NOT NULL DEFAULT '08:00:00' COMMENT '开放开始时间',
  close_time     TIME         NOT NULL DEFAULT '22:00:00' COMMENT '开放结束时间',
  max_duration   INT          NOT NULL DEFAULT 120 COMMENT '单次最长预约时长(分钟)',
  status         TINYINT      NOT NULL DEFAULT 1 COMMENT '资源状态 0维护中 1空闲 2占用',
  device_desc    VARCHAR(500) DEFAULT NULL COMMENT '设备配置描述',
  deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_type_status (resource_type, status),
  KEY idx_area (area)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校园资源表';

-- =====================================================================
-- 9. 预约表（核心业务表）
--   [防超卖三保险]
--   1) uk_resource_time 唯一索引：仅能阻止「完全相同时段」的重复插入，
--      无法阻止部分重叠时段(如 8:00-9:00 与 8:30-9:30)，重叠由应用层兜底：
--   2) 应用层：Redis 分布式锁 + 事务内冲突检测(start_time<new_end AND end_time>new_start
--      AND status IN(待签到,使用中)) + 悲观行锁，保证部分重叠时段互斥；
--   3) version 乐观锁：并发更新时校验版本号，防脏写。
-- =====================================================================
CREATE TABLE resource_booking (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '预约ID',
  booking_no  VARCHAR(64)  NOT NULL COMMENT '预约单号(业务唯一)',
  request_id  VARCHAR(64)  DEFAULT NULL COMMENT '幂等请求凭证(防重复提交)',
  user_id     BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  resource_id BIGINT UNSIGNED NOT NULL COMMENT '资源ID',
  start_time  DATETIME     NOT NULL COMMENT '预约开始时间',
  end_time    DATETIME     NOT NULL COMMENT '预约结束时间',
  status      TINYINT      NOT NULL DEFAULT 0 COMMENT '预约状态 0待签到 1使用中 2已完成 3已取消 4违约',
  sign_time   DATETIME     DEFAULT NULL COMMENT '签到时间',
  cancel_time DATETIME     DEFAULT NULL COMMENT '取消时间',
  version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_resource_time (resource_id, start_time, end_time),
  UNIQUE KEY uk_booking_no (booking_no),
  UNIQUE KEY uk_request_id (request_id),
  KEY idx_user_time (user_id, start_time),
  KEY idx_status_time (status, start_time),
  KEY idx_resource_status (resource_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源预约表(核心)';

-- =====================================================================
-- 10. 违规记录表
-- =====================================================================
CREATE TABLE user_violation (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id         BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  violation_type  TINYINT      NOT NULL COMMENT '违规类型 1超时未签到 2恶意占座',
  violation_count INT          NOT NULL DEFAULT 1 COMMENT '累计违规次数',
  ban_start_time  DATETIME     DEFAULT NULL COMMENT '封禁开始时间',
  ban_end_time    DATETIME     DEFAULT NULL COMMENT '封禁截止时间',
  remark          VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_user_violation (user_id, violation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户违规记录表';

-- =====================================================================
-- 11. 消息通知表
-- =====================================================================
CREATE TABLE message_notice (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id     BIGINT UNSIGNED NOT NULL COMMENT '接收用户ID（0为全员广播）',
  biz_type    VARCHAR(32)  DEFAULT NULL COMMENT '业务类型，如 booking/cancel/expire',
  msg_type    TINYINT      NOT NULL DEFAULT 1 COMMENT '消息类型 1系统 2预约 3提醒',
  channel     TINYINT      NOT NULL DEFAULT 1 COMMENT '通知渠道 1站内信 2短信',
  title       VARCHAR(128) DEFAULT NULL COMMENT '消息标题',
  content     TEXT         NOT NULL COMMENT '消息内容',
  read_status TINYINT      NOT NULL DEFAULT 0 COMMENT '读取状态 0未读 1已读',
  read_time   DATETIME     DEFAULT NULL COMMENT '读取时间',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_user_read (user_id, read_status),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- =====================================================================
-- 12. 资源统计数据表（大屏/统计接口聚合）
-- =====================================================================
CREATE TABLE resource_statistics (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  stat_date        DATE         NOT NULL COMMENT '统计日期',
  resource_id      BIGINT UNSIGNED NOT NULL COMMENT '资源ID',
  area             VARCHAR(64)  DEFAULT NULL COMMENT '区域(冗余)',
  booking_count    INT          NOT NULL DEFAULT 0 COMMENT '当日预约量',
  usage_count      INT          NOT NULL DEFAULT 0 COMMENT '实际使用量(签到)',
  usage_rate       DECIMAL(8,2) DEFAULT 0 COMMENT '使用率(%)',
  idle_duration    INT          NOT NULL DEFAULT 0 COMMENT '闲置时长(分钟)',
  violation_count  INT          NOT NULL DEFAULT 0 COMMENT '违约人数',
  flow_predict     DECIMAL(8,2) DEFAULT 0 COMMENT '人流预测值',
  create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_stat_resource_date (resource_id, stat_date),
  KEY idx_stat_date (stat_date),
  KEY idx_stat_area (area, stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源统计数据表';

-- =====================================================================
-- 13. AI 人流预测表（模块5：数据采集 -> AI 预测 -> 调度优化闭环）
-- =====================================================================
CREATE TABLE ai_flow_prediction (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  predict_date   DATE         NOT NULL COMMENT '预测日期',
  area           VARCHAR(64)  NOT NULL COMMENT '区域',
  time_slot      TINYINT      NOT NULL COMMENT '时段(0-23 小时)',
  predict_value  DECIMAL(8,2) DEFAULT 0 COMMENT '预测人流值',
  congest_level  TINYINT      NOT NULL DEFAULT 1 COMMENT '拥堵等级 1低 2中 3高',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_predict (predict_date, area, time_slot),
  KEY idx_predict_date (predict_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI人流预测表';

-- =====================================================================
-- 14. 系统配置表（预约规则参数，超管在线可调，业务从读配置而非硬编码）
-- =====================================================================
CREATE TABLE sys_config (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  config_key   VARCHAR(64)  NOT NULL COMMENT '参数键',
  config_value VARCHAR(128) NOT NULL COMMENT '参数值',
  description  VARCHAR(255) DEFAULT NULL COMMENT '参数说明',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- =====================================================================
-- 初始数据：数据字典
-- =====================================================================

INSERT INTO sys_dict_type (id, dict_type, dict_name, remark) VALUES
(1, 'role_type',       '角色类型',       '用户角色分层'),
(2, 'resource_type',   '资源类型',       '校园资源分类'),
(3, 'resource_status', '资源状态',       '资源实时状态'),
(4, 'booking_status',  '预约状态',       '预约生命周期状态'),
(5, 'violation_type',  '违规类型',       '用户违规分类'),
(6, 'msg_type',        '消息类型',       '通知消息分类'),
(7, 'notice_channel',  '通知渠道',       '消息下发渠道'),
(8, 'read_status',     '读取状态',       '消息读取标记'),
(9, 'congest_level',   '拥堵等级',       'AI人流拥堵分级');

INSERT INTO sys_dict_item (dict_type_id, dict_type, label, `value`, sort) VALUES
-- 角色类型
(1, 'role_type', '学生',     '1', 1),
(1, 'role_type', '管理员',   '2', 2),
(1, 'role_type', '超级管理员','3', 3),
-- 资源类型
(2, 'resource_type', '普通教室', '1', 1),
(2, 'resource_type', '实验室',   '2', 2),
(2, 'resource_type', '会议室',   '3', 3),
(2, 'resource_type', '图书馆座位','4', 4),
-- 资源状态
(3, 'resource_status', '维护中', '0', 1),
(3, 'resource_status', '空闲',   '1', 2),
(3, 'resource_status', '占用',   '2', 3),
-- 预约状态
(4, 'booking_status', '待签到', '0', 1),
(4, 'booking_status', '使用中', '1', 2),
(4, 'booking_status', '已完成', '2', 3),
(4, 'booking_status', '已取消', '3', 4),
(4, 'booking_status', '违约',   '4', 5),
-- 违规类型
(5, 'violation_type', '超时未签到', '1', 1),
(5, 'violation_type', '恶意占座',   '2', 2),
-- 消息类型
(6, 'msg_type', '系统通知', '1', 1),
(6, 'msg_type', '预约通知', '2', 2),
(6, 'msg_type', '到期提醒', '3', 3),
-- 通知渠道
(7, 'notice_channel', '站内信', '1', 1),
(7, 'notice_channel', '短信',   '2', 2),
-- 读取状态
(8, 'read_status', '未读', '0', 1),
(8, 'read_status', '已读', '1', 2),
-- 拥堵等级
(9, 'congest_level', '低', '1', 1),
(9, 'congest_level', '中', '2', 2),
(9, 'congest_level', '高', '3', 3);

-- =====================================================================
-- 初始数据：角色
-- =====================================================================
INSERT INTO sys_role (id, role_code, role_name, description) VALUES
(1, 'ROLE_STUDENT',     '学生',       '普通学生用户，可预约资源'),
(2, 'ROLE_ADMIN',       '管理员',     '管理人员，负责资源与预约审核'),
(3, 'ROLE_SUPER_ADMIN', '超级管理员', '系统最高权限，可管理账号与角色');

-- =====================================================================
-- 初始数据：权限（接口级）
-- =====================================================================
INSERT INTO sys_permission (id, parent_id, perm_name, perm_code, perm_type, url, method, sort) VALUES
(1,  0, '用户管理', 'user:manage',        3, '/api/user/**',    'GET',    1),
(2,  0, '资源管理', 'resource:manage',    3, '/api/resource/**','POST',  2),
(3,  0, '资源查询', 'resource:query',     3, '/api/resource/**','GET',   3),
(4,  0, '预约申请', 'booking:create',     3, '/api/booking/create', 'POST', 4),
(5,  0, '预约查询', 'booking:query',      3, '/api/booking/**','GET',   5),
(6,  0, '预约取消', 'booking:cancel',     3, '/api/booking/cancel', 'POST', 6),
(7,  0, '签到核销', 'booking:sign',       3, '/api/booking/sign', 'POST', 7),
(8,  0, '预约审核', 'booking:audit',      3, '/api/booking/audit', 'POST', 8),
(9,  0, '统计大屏', 'statistics:view',    3, '/api/statistics/**','GET',  9),
(10, 0, '违规管理', 'violation:manage',   3, '/api/violation/**','POST', 10);

-- =====================================================================
-- 初始数据：角色权限绑定
-- =====================================================================
-- 学生：资源查询 + 预约申请/查询/取消/签到
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(1, 3), (1, 4), (1, 5), (1, 6), (1, 7);

-- 管理员：资源管理/查询 + 预约查询/审核 + 统计 + 违规 + 用户管理
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(2, 1), (2, 2), (2, 3), (2, 5), (2, 8), (2, 9), (2, 10);

-- 超级管理员：全部权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 3, id FROM sys_permission;

-- =====================================================================
-- 初始数据：账号（密码均对应明文 123456，BCrypt 哈希）
-- =====================================================================
INSERT INTO sys_user (id, username, password, student_no, real_name, phone, role_type, status) VALUES
(1, 'admin',   '$2a$10$8K2m//QckmcF0o/PtiNE9eQrw3IntOBXkcgB4vSo8uWFhz5.c.IWW', 'T0001', '超级管理员', '13800000001', 3, 1),
(2, 'admin2',  '$2a$10$8K2m//QckmcF0o/PtiNE9eQrw3IntOBXkcgB4vSo8uWFhz5.c.IWW', 'T0002', '管理员',     '13800000002', 2, 1),
(3, 'student', '$2a$10$8K2m//QckmcF0o/PtiNE9eQrw3IntOBXkcgB4vSo8uWFhz5.c.IWW', '20260001', '张三',     '13800000003', 1, 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 3),
(2, 2),
(3, 1);

-- =====================================================================
-- 初始数据：示例校园资源（以「空教室」为主）
-- =====================================================================
INSERT INTO campus_resource (id, resource_name, resource_type, area, location, capacity, open_time, close_time, max_duration, status, device_desc) VALUES
(1, 'A101 教室', 1, '博学楼',  '博学楼1层', 60, '08:00:00', '22:00:00', 120, 1, '多媒体投影、空调'),
(2, 'A102 教室', 1, '博学楼',  '博学楼1层', 60, '08:00:00', '22:00:00', 120, 1, '多媒体投影、空调'),
(3, 'A201 教室', 1, '博学楼',  '博学楼2层', 80, '08:00:00', '22:00:00', 120, 1, '阶梯教室、智能黑板'),
(4, 'A202 教室', 1, '博学楼',  '博学楼2层', 80, '08:00:00', '22:00:00', 120, 2, '阶梯教室、智能黑板'),
(5, 'B301 实验室', 2, '实验楼', '实验楼3层', 40, '09:00:00', '21:00:00', 180, 1, '计算机机房 60台'),
(6, 'B302 实验室', 2, '实验楼', '实验楼3层', 40, '09:00:00', '21:00:00', 180, 0, '网络实验室(维护中)'),
(7, 'C101 会议室', 3, '行政楼', '行政楼1层', 20, '09:00:00', '18:00:00', 240, 1, '投影、视频会议系统'),
(8, '图书馆自习位 3F', 4, '图书馆', '图书馆3层', 1, '08:00:00', '22:00:00', 240, 1, '独立自习座位');

-- =====================================================================
-- 初始数据：系统配置参数（预约规则，超管可在线调整）
-- =====================================================================
INSERT INTO sys_config (config_key, config_value, description) VALUES
('booking.max_per_day',      '3',   '单人单日最大预约次数'),
('booking.max_duration',     '120', '单次最长预约时长(分钟)'),
('booking.lock_ttl_seconds', '300', 'Redis 短时资源锁定时长(秒)'),
('booking.sign_timeout_min', '15',  '超时未签到判定阈值(分钟)'),
('violation.ban_threshold',  '3',   '累计违约封禁阈值(次)'),
('violation.ban_days',       '7',   '封禁天数');

-- =====================================================================
-- 结束
-- =====================================================================