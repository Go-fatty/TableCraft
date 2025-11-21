@echo off
chcp 65001 >nul
echo ==========================================
echo TableCraft システム起動スクリプト
echo ==========================================

echo.
echo [1/4] 設定ファイル確認中...
cd backend
if exist "src\main\resources\config\table-config.json" (
    echo      ✓ 設定ファイル確認完了
) else (
    echo      WARNING: 設定ファイルが見つかりません
)

echo.
echo      注意: MySQLサーバーが起動していることを確認してください
echo      データベース: tablecraft
echo      初回起動時は以下のコマンドでテーブル作成が必要です:
echo      mysql -u root -p -D tablecraft ^< src\main\resources\mysql-schema.sql

echo.
echo [2/4] Spring Boot ビルド中...
call mvn clean package -q -DskipTests
if errorlevel 1 (
    echo      ❌ ビルドに失敗しました
    pause
    exit /b 1
)
echo      ✓ ビルド完了

echo.
echo [3/4] バックエンド起動中...
start /B java -jar "target\tablecraft-backend-0.0.1-SNAPSHOT.jar" --spring.profiles.active=dev --server.port=8082
echo      ✓ Spring Boot (ポート8082) を起動しました

echo.
echo [4/4] フロントエンド起動中...
cd ..\frontend
start cmd /c "npm run dev"
echo      ✓ React (ポート5173) を起動しました

echo.
echo ==========================================
echo システム起動完了！
echo ==========================================
echo.
echo アクセス方法:
echo   フロントエンド: http://localhost:5173
echo   バックエンドAPI: http://localhost:8082
echo   ヘルスチェック: http://localhost:8082/api/health
echo.
echo 注意: MySQL接続設定を確認してください
echo   設定ファイル: backend\src\main\resources\application-dev.properties
echo.
echo システム停止: taskkill /F /IM java.exe
echo ==========================================

pause