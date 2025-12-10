@echo off
chcp 65001 >nul
echo ==========================================
echo TableCraft システム起動スクリプト
echo ==========================================

echo.
echo [1/8] 設定ファイル確認中...
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
echo [2/8] ビルド済みJARファイル確認中...
cd ..
if not exist "bin\application.jar" (
    echo      ❌ bin\application.jar が見つかりません
    echo      build-for-aws.bat を実行してビルドしてください
    pause
    exit /b 1
)
echo      ✓ JARファイル確認完了

echo.
echo [3/8] bin フォルダ準備中...
if not exist "bin" mkdir bin
if not exist "bin\config" mkdir bin\config
echo      ✓ bin フォルダ確認完了

echo.
echo [4/8] 設定ファイルをコピー中...
if exist "backend\config\table-config.json" (
    copy /Y backend\config\table-config.json bin\config\ >nul
    echo      ✓ 外部設定ファイルをコピー
) else (
    copy /Y backend\src\main\resources\config\table-config.json bin\config\ >nul
    echo      ✓ デフォルト設定ファイルをコピー
)
copy /Y backend\src\main\resources\application*.properties bin\config\ >nul 2>&1
echo      ✓ JAR と設定ファイルのコピー完了

echo.
echo [5/8] 既存プロセス確認中...
tasklist /FI "IMAGENAME eq java.exe" 2>nul | find /I /N "java.exe">nul
if "%ERRORLEVEL%"=="0" (
    echo      既存のJavaプロセスを停止します...
    taskkill /F /IM java.exe >nul 2>&1
    timeout /t 2 /nobreak >nul
    echo      ✓ プロセスクリーンアップ完了
) else (
    echo      ✓ プロセス確認完了 - 競合なし
)

echo.
echo [6/8] バックエンド起動中 - bin フォルダから...
cd bin
start "TableCraft Backend" java -jar "application.jar" --spring.profiles.active=dev --server.port=8082 --spring.config.additional-location=file:./config/
echo      ✓ Spring Boot ポート8082 を起動中...
echo      実行ディレクトリ: %cd%
echo      起動完了を待機中 - 最大30秒...

REM 起動確認ループ - 最大30秒待機
set /a RETRY=0
:CHECK_BACKEND
set /a RETRY+=1
if %RETRY% GTR 30 (
    echo      ⚠ タイムアウト - バックエンドの起動確認ができませんでした
    goto FRONTEND_START
)
timeout /t 1 /nobreak >nul
powershell -Command "try { Invoke-WebRequest -Uri 'http://localhost:8082/api/health' -TimeoutSec 1 -UseBasicParsing | Out-Null; exit 0 } catch { exit 1 }" >nul 2>&1
if errorlevel 1 (
    echo      待機中... %RETRY%/30秒
    goto CHECK_BACKEND
)
echo      ✓ バックエンド起動完了！

:FRONTEND_START
echo.
echo [7/8] フロントエンド起動中...
cd ..\frontend
start "TableCraft Frontend" cmd /c "npm run dev"
echo      ✓ React ポート5173 を起動しました

echo.
echo [8/8] 管理画面起動中...
cd ..\frontend-admin
timeout /t 2 /nobreak >nul
start "TableCraft Admin" cmd /c "npm run dev"
echo      ✓ Admin ポート5174 を起動しました

echo.
echo ==========================================
echo システム起動完了！
echo ==========================================
echo.
echo 実行環境:
echo   実行ディレクトリ: bin\
echo   JARファイル: bin\tablecraft-backend-0.0.1-SNAPSHOT.jar
echo   設定ファイル: bin\config\table-config.json
echo   SQLファイル: bin\sql\
echo.
echo アクセス方法:
echo   業務画面: http://localhost:5173
echo   管理画面: http://localhost:5174
echo   バックエンドAPI: http://localhost:8082
echo   ヘルスチェック: http://localhost:8082/api/health
echo.
echo 注意: MySQL接続設定を確認してください
echo   設定ファイル: bin\config\application-dev.properties
echo.
echo データベース初期化:
echo   mysql -u root -p -D tablecraft ^< bin\sql\table_definitions.sql
echo   または
echo   mysql -u root -p -D tablecraft ^< bin\sql\create_tables.sql
echo.
echo 設定変更方法:
echo   1. backend\config\table-config.json を編集
echo   2. このスクリプトを再実行 - 自動的にbinへコピー
echo.
echo システム停止: taskkill /F /IM java.exe
echo ==========================================

pause