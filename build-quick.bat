@echo off
chcp 65001 >nul
REM TableCraft AWS デプロイパッケージ作成 - 簡単版

echo ==========================================
echo TableCraft AWS デプロイパッケージ作成
echo ==========================================

REM フロントエンドビルド
echo [1/3] フロントエンドビルド...
cd frontend
call npm run build
if errorlevel 1 goto error
cd ..

REM バックエンドビルド
echo [2/3] バックエンドビルド...
cd backend
call mvn clean package -DskipTests
if errorlevel 1 goto error
cd ..

REM デプロイパッケージ作成
echo [3/3] デプロイパッケージ作成...
call build-for-aws.bat --skip-frontend --skip-backend

echo.
echo ✅ 完了! forDeploy-package.zip が作成されました。
echo.
pause
goto end

:error
echo.
echo ❌ ビルドエラーが発生しました。
echo.
pause
exit /b 1

:end