# 校园资源智能预约调度系统

基于 Spring Boot + Vue 3 的校园资源（教室、图书馆座位、实验室、会议室）智能预约调度平台，支持学生预约、管理员后台、数据大屏与 AI 热度预测。

## 技术栈

| 端 | 技术 |
|----|------|
| 后端 | Java 21、Spring Boot 3.2.5、MyBatis-Plus、Spring Security + JWT、Swagger(springdoc) |
| 前端 | Vue 3 + TypeScript、Vite 5、Element Plus、Pinia、Vue Router、Axios、ECharts、Sass |
| 中间件 | MySQL 8.0（utf8mb4）、Redis、RabbitMQ |

## 功能概览

- **学生/教师端**：资源浏览（按类型/状态筛选）、资源详情与按时段预约（幂等防重复提交）、我的预约（签到/取消）、消息通知（已读管理）
- **管理后台**：资源管理（CRUD）、预约管理（含 CSV 导出）、违约登记、用户封禁/解封、系统公告、参数配置、AI 热度预测触发
- **数据大屏**：KPI 总览、资源占用率、预约趋势、AI 预测多线图（深色主题）
- **自动化**：超时未签到判违约、到期提醒、违约封禁（定时任务 + RabbitMQ 通知）

## 环境要求

运行前请确保本机已安装并启动：

1. **JDK 21**（后端编译运行）
2. **Maven 3.8+**（后端构建；Windows 建议配置 `JAVA_HOME` 指向 JDK 21）
3. **Node.js 18+ / npm 9+**（前端构建）
4. **MySQL 8.0**（字符集需支持 utf8mb4）
5. **Redis**（登录限流、分布式锁，**必需**——缺失会导致登录报"系统错误"）
6. **RabbitMQ**（违约/提醒消息投递）

## 快速开始

### 1. 初始化数据库

创建数据库并导入脚本（**必须指定 utf8mb4 字符集，否则中文会变成问号**）：

```bash
mysql -uroot -p --default-character-set=utf8mb4 < backend/src/main/resources/db/init.sql
```

脚本会自动创建 `campus_dispatch` 库、表结构和初始数据（超管账号、示例资源、系统参数）。

> Windows PowerShell 用户若遇到重定向报错，可先 `mysql -uroot -p --default-character-set=utf8mb4` 进入客户端后执行 `source backend/src/main/resources/db/init.sql;`

### 2. 修改后端配置

复制 `backend/src/main/resources/application-dev.yml.example` 为 `application-dev.yml`，然后编辑并填入你本机的数据库账号密码：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_dispatch?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: your_username    # 改成你的用户名
    password: your_password    # 改成你的密码
```

Redis / RabbitMQ 若在本机默认端口且未设密码，无需修改。

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端启动在 `http://localhost:8080`，接口文档见 `http://localhost:8080/swagger-ui/index.html`。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动在 `http://localhost:5173`（开发模式下 `/api` 请求由 Vite 代理转发到 8080，无需额外配置）。

### 5. 登录

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | `<初始密码>` | 超级管理员 |

初始密码在 `sql/init.sql` 中定义（BCrypt 加密存储），首次登录后请立即修改。管理员登录后可在"用户端"与"管理后台"之间自由切换（学生端顶部有"切换到管理后台"按钮）。

## 安全提示

- **JWT 密钥**：生产环境务必通过环境变量设置强随机密钥（至少 32 位），切勿使用代码中的默认值：
  ```bash
  # Linux / macOS
  export JWT_SECRET="<随机字符串>"
  # Windows（PowerShell，永久生效）
  setx JWT_SECRET "<随机字符串>"
  ```
- **初始账号**：`sql/init.sql` 中的超管密码仅为演示用途，部署后请立即登录修改
- **本地配置**：`application-dev.yml` 含本机密码等敏感信息，已被 `.gitignore` 忽略，请基于 `.example` 模板创建

## Windows 一键启动

项目根目录提供 `start-all.bat`（**注意：脚本内写死了 JDK/Maven/Redis/Node 的安装路径，请按需修改**）：

- 自动检查并启动 MySQL / Redis / RabbitMQ
- 优先直接运行 `backend/target/campus-dispatch-1.0.0.jar`（需先 `mvn package -DskipTests` 构建），秒级启动；jar 不存在时回退 Maven 编译启动
- 循环检测 8080 端口就绪后才启动前端 Vite

## 生产构建

```bash
# 后端
cd backend && mvn clean package -DskipTests
java -jar target/campus-dispatch-1.0.0.jar

# 前端
cd frontend && npm run build
# 产物在 frontend/dist，可部署到任意静态服务器（需将 /api 反向代理到后端 8080）
```

## 常见问题

| 现象 | 原因与解决 |
|------|-----------|
| 登录提示"服务器内部错误/系统错误" | Redis 未启动。启动 Redis 后重试 |
| 页面中文显示为 `???` | 导入 init.sql 时未用 utf8mb4 字符集连接。删库后用 `--default-character-set=utf8mb4` 重新导入 |
| 资源开放时间显示 `undefined:undefined` | 已在 1.0 修复；若复现请检查后端 `ResourceVO` 的 `@JsonFormat(pattern="HH:mm")` 序列化 |
| 修改资源报"系统异常" | 前端提交的时间须为 `"HH:mm"` 字符串（已修复），不要传 `{hour, minute}` 对象 |
| Vite 页面反复自动刷新/路由丢失 | 不要同时启用 unplugin-auto-import 与 main.ts 中的完整 Element Plus 导入（当前配置已移除前者） |
| Windows 上 esbuild 崩溃 (0xC0000409) | 本机特例可改用 esbuild-wasm；一般情况下重装 `node_modules` 即可 |

## 目录结构

```
├── start-all.bat                  # Windows 一键启动脚本
├── backend/                       # Spring Boot 后端
│   ├── src/main/java/com/campus/dispatch/
│   │   ├── common/                # 结果封装、全局异常、JWT 安全、工具
│   │   ├── config/                # Security / MyBatis-Plus / Swagger / RabbitMQ 配置
│   │   ├── job/                   # 定时任务（违约判定、到期提醒）
│   │   └── modules/               # auth / resource / booking / notice / violation / user / config / dashboard / forecast / export
│   └── src/main/resources/
│       ├── application.yml        # 主配置（JWT 密钥经 JWT_SECRET 环境变量注入）
│       ├── application-dev.yml    # 开发环境（数据源、Redis、RabbitMQ）
│       └── db/init.sql            # 数据库初始化脚本（utf8mb4）
└── frontend/                      # Vue 3 前端
    └── src/
        ├── api/                   # axios 封装 + 10 个 API 模块
        ├── store/                 # Pinia（认证状态）
        ├── router/                # 路由 + 角色权限守卫
        ├── layout/                # 学生端 / 管理端布局
        └── views/                 # login / student(4页) / admin(7页) / dashboard(大屏)
```