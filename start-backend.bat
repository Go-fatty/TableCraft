@echo off
cd /d "%~dp0backend"
echo ========================================
echo TableCraft Backend Starting...
echo ========================================
mvn spring-boot:run
pause
