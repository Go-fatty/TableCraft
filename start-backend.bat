@echo off
cd /d "%~dp0backend"
echo ========================================
echo TableCraft Backend Starting...
echo ========================================
mvn clean compile spring-boot:run
pause
