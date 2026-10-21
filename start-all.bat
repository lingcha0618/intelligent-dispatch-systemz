@echo off
chcp 65001 >nul
title 校园资源智能预约调度系统 - 一键启动

echo ============================================
echo   校园资源智能预约调度系统 - 一键启动
echo ============================================
echo.

REM 检查 MySQL
echo [1/4] 检查 MySQL...
sc query MySQL >nul 2>&1
if %errorlevel% equ 0 (
    sc query MySQL | find "RUNNING" >nul
    if %errorlevel% equ 0 (
        echo       MySQL 已运行 ✓
    ) else (
        echo       正在启动 MySQL...
        net start MySQL >nul 2>&1
        if %errorlevel% equ 0 (
            echo       MySQL 启动成功 ✓
        ) else (
            echo       MySQL 启动失败，请手动启动 ✗
        )
    )
) else (
    echo       MySQL 服务未找到，请确认已安装 ✗
)

REM 检查 Redis
echo [2/4] 检查 Redis...
sc query Redis >nul 2>&1
if %errorlevel% equ 0 (
    sc query Redis | find "RUNNING" >nul
    if %errorlevel% equ 0 (
        echo       Redis 已运行 ✓
    ) else (
        echo       正在启动 Redis 服务...
        net start Redis >nul 2>&1
        if %errorlevel% equ 0 (
            echo       Redis 启动成功 ✓
        ) else (
            echo       Redis 服务启动失败，请手动启动 ✗
        )
    )
) else (
    REM Redis 未注册为 Windows 服务，改为直接运行 redis-server.exe
    netstat -ano | findstr ":6379" | findstr "LISTENING" >nul 2>&1
    if %errorlevel% equ 0 (
        echo       Redis 已运行 ✓
    ) else if exist "C:\Program Files\Redis\redis-server.exe" (
        echo       正在启动 Redis...
        start "Redis-6379" "C:\Program Files\Redis\redis-server.exe" "C:\Program Files\Redis\redis.windows.conf"
        timeout /t 2 /nobreak >nul
        netstat -ano | findstr ":6379" | findstr "LISTENING" >nul 2>&1
        if %errorlevel% equ 0 (
            echo       Redis 启动成功 ✓
        ) else (
            echo       Redis 启动失败，请手动启动 ✗
        )
    ) else (
        echo       Redis 未安装，请确认已安装 ✗
    )
)

REM 检查 RabbitMQ
echo [3/4] 检查 RabbitMQ...
sc query RabbitMQ >nul 2>&1
if %errorlevel% equ 0 (
    sc query RabbitMQ | find "RUNNING" >nul
    if %errorlevel% equ 0 (
        echo       RabbitMQ 已运行 ✓
    ) else (
        echo       正在启动 RabbitMQ...
        net start RabbitMQ >nul 2>&1
        if %errorlevel% equ 0 (
            echo       RabbitMQ 启动成功 ✓
        ) else (
            echo       RabbitMQ 启动失败，请手动启动 ✗
        )
    )
) else (
    echo       RabbitMQ 服务未找到，请确认已安装 ✗
)

echo.
echo [4/4] 启动后端和前端...
echo.

REM 启动后端：优先直接运行已构建的 jar（秒级启动），jar 不存在才用 Maven 编译运行
echo       启动后端 (Spring Boot, 端口 8080)...
cd /d "%~dp0backend"
if exist "target\campus-dispatch-1.0.0.jar" (
    start "后端-SpringBoot-8080" cmd /k "C:\dev\jdk21\bin\java.exe -jar target\campus-dispatch-1.0.0.jar"
) else (
    echo       未找到已构建的 jar，使用 Maven 编译启动（需要几分钟）...
    start "后端-SpringBoot-8080" cmd /k "set JAVA_HOME=C:\dev\jdk21 && C:\dev\maven\bin\mvn.cmd spring-boot:run"
)

REM 等待后端真正监听 8080 端口（最多等 60 秒）
echo       等待后端就绪（最多60秒）...
set /a WAIT_COUNT=0
:WAIT_BACKEND
timeout /t 3 /nobreak >nul
netstat -ano | findstr ":8080" | findstr "LISTENING" >nul 2>&1
if %errorlevel% equ 0 goto BACKEND_READY
set /a WAIT_COUNT+=3
if %WAIT_COUNT% LSS 60 goto WAIT_BACKEND
echo       警告：后端 60 秒内未监听 8080，请查看"后端-SpringBoot-8080"窗口中的报错 ✗
goto START_FRONTEND

:BACKEND_READY
echo       后端已就绪，监听 8080 端口 ✓

:START_FRONTEND
REM 启动前端
echo       启动前端 (Vite, 端口 5173)...
cd /d "%~dp0frontend"
start "前端-Vite-5173" cmd /k "set PATH=C:\Program Files\nodejs;%PATH% && npx.cmd vite --host"

echo.
echo ============================================
echo   启动完成！
echo.
echo   后端地址: http://localhost:8080
echo   前端地址: http://localhost:5173
echo   登录账号: admin / 123456
echo.
echo   关闭服务: 直接关闭对应的命令行窗口
echo ============================================
echo.
pause