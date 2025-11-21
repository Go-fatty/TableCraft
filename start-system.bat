@echo off
echo ==========================================
echo TableCraft システム起動スクリプト
echo ==========================================

echo.
echo [1/4] 設定ファイルコピー中...
cd backend
copy /Y "..\settings_creates\output\frontend\*" "src\main\resources\" > nul
echo     完了: 設定ファイルをコピーしました

echo.
echo [2/4] Spring Boot ビルド中...
call mvn clean package -q
echo     完了: ビルドが完了しました

echo.
echo [3/4] バックエンド起動中...
start /B java -jar "target\tablecraft-backend-0.0.1-SNAPSHOT.jar" --spring.profiles.active=dev --server.port=8082
echo     完了: Spring Boot (ポート8082) を起動しました

echo.
echo [4/4] フロントエンド起動中...
cd ..\frontend
start cmd /c "npm run dev"
echo     完了: React (ポート5173) を起動しました

echo.
echo ==========================================
echo システム起動完了！
echo ==========================================
echo.
echo アクセス方法:
echo   フロントエンド: http://localhost:5173
echo   バックエンドAPI: http://localhost:8082
echo   H2コンソール: http://localhost:8082/h2-console
echo.
echo システム停止: taskkill /F /IM java.exe
echo ==========================================

pause