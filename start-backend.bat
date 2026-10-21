@echo off
chcp 65001 >nul
cd /d "%~dp0backend"
start "后端-SpringBoot-8080" /min "C:\dev\jdk21\bin\java.exe" -jar "target\campus-dispatch-1.0.0.jar" > nul 2>&1