@echo off
chcp 65001 >nul
echo ==========================================
echo TableCraft システム起動スクリプト
echo ==========================================

echo.
echo [1/6] 設定ファイル確認中...
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
echo [2/6] Spring Boot ビルド中...
call mvn clean package -q -DskipTests
if errorlevel 1 (
    echo      ❌ ビルドに失敗しました
    pause
    exit /b 1
)
echo      ✓ ビルド完了

echo.
echo [3/6] bin フォルダ準備中...
cd ..
if exist "bin" (
    echo      既存のbinフォルダをクリーンアップ...
    rmdir /S /Q bin
)
mkdir bin
mkdir bin\config
mkdir bin\sql
echo      ✓ bin フォルダ作成完了

echo.
echo [4/6] 実行ファイルと設定ファイルをコピー中...
copy /Y backend\target\tablecraft-backend-0.0.1-SNAPSHOT.jar bin\ >nul
if exist "backend\config\table-config.json" (
    copy /Y backend\config\table-config.json bin\config\ >nul
    echo      ✓ 外部設定ファイルをコピー
) else (
    copy /Y backend\src\main\resources\config\table-config.json bin\config\ >nul
    echo      ✓ デフォルト設定ファイルをコピー
)
copy /Y backend\src\main\resources\application*.properties bin\config\ >nul 2>&1

REM SQLファイルをコピー
if exist "backend\src\main\resources\table-definitions.sql" (
    copy /Y backend\src\main\resources\table-definitions.sql bin\sql\ >nul
    echo      ✓ SQLファイル (table-definitions.sql) をコピー
)
if exist "settings_creates\output\sql\table_definitions.sql" (
    copy /Y settings_creates\output\sql\table_definitions.sql bin\sql\ >nul
    echo      ✓ SQLファイル (table_definitions.sql) をコピー
)
if exist "settings_creates\output\sql\create_tables.sql" (
    copy /Y settings_creates\output\sql\create_tables.sql bin\sql\ >nul
    echo      ✓ SQLファイル (create_tables.sql) をコピー
)
if exist "settings_creates\output\sql\create_indexes.sql" (
    copy /Y settings_creates\output\sql\create_indexes.sql bin\sql\ >nul
    echo      ✓ SQLファイル (create_indexes.sql) をコピー
)
if exist "settings_creates\output\sql\create_foreign_keys.sql" (
    copy /Y settings_creates\output\sql\create_foreign_keys.sql bin\sql\ >nul
    echo      ✓ SQLファイル (create_foreign_keys.sql) をコピー
)
echo      ✓ JAR と設定ファイルのコピー完了

echo.
echo [5/6] バックエンド起動中 (bin フォルダから)...
cd bin
start /B java -jar "tablecraft-backend-0.0.1-SNAPSHOT.jar" --spring.profiles.active=dev --server.port=8082 --spring.config.additional-location=file:./config/
echo      ✓ Spring Boot (ポート8082) を起動しました
echo      実行ディレクトリ: %cd%

echo.
echo [6/6] フロントエンド起動中...
cd ..\frontend
start cmd /c "npm run dev"
echo      ✓ React (ポート5173) を起動しました

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
echo   フロントエンド: http://localhost:5173
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
echo   2. このスクリプトを再実行（自動的にbinへコピー）
echo.
echo システム停止: taskkill /F /IM java.exe
echo ==========================================

pause