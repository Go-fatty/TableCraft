@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM TableCraft AWS Deployment Builder (Batch版)
REM フルスタック構成でElastic Beanstalkデプロイ用パッケージを作成

set "OutputDir=.\bin"
set "SkipFrontendBuild="
set "SkipBackendBuild="

REM パラメータ解析
:parse_args
if "%~1"=="" goto start_build
if /i "%~1"=="--skip-frontend" set "SkipFrontendBuild=1"
if /i "%~1"=="--skip-backend" set "SkipBackendBuild=1"
if /i "%~1"=="--output" set "OutputDir=%~2" & shift
shift
goto parse_args

:start_build
echo ==========================================
echo TableCraft AWS Deployment Builder
echo ==========================================

REM 1. フロントエンドビルド
if defined SkipFrontendBuild (
    echo [1/4] フロントエンドビルドをスキップ
) else (
    echo [1/4] フロントエンドビルド中...
    cd frontend
    
    if not exist "node_modules" (
        echo       依存関係をインストール中...
        call npm install
        if errorlevel 1 goto error
    )
    
    echo       Reactアプリケーションをビルド中...
    call npm run build
    if errorlevel 1 goto error
    
    cd ..
    echo       ✓ フロントエンドビルド完了
)

REM 2. 静的ファイルをバックエンドにコピー
echo [2/4] 静的ファイル統合中...

set "staticDir=backend\src\main\resources\static"
if exist "%staticDir%" rmdir /s /q "%staticDir%"
mkdir "%staticDir%" 2>nul

if exist "frontend\dist" (
    xcopy "frontend\dist\*" "%staticDir%\" /e /i /y >nul
    echo       ✓ 静的ファイル統合完了
) else (
    echo       WARNING: frontend\dist が見つかりません。フロントエンドビルドを実行してください。
)

REM 3. バックエンドビルド
if defined SkipBackendBuild (
    echo [3/4] バックエンドビルドをスキップ
) else (
    echo [3/4] バックエンドビルド中...
    cd backend
    
    echo       Spring Bootアプリケーションをビルド中...
    call mvn clean package -DskipTests -Dspring.profiles.active=prod
    if errorlevel 1 goto error
    
    cd ..
    echo       ✓ バックエンドビルド完了
)

REM 4. デプロイパッケージ作成
echo [4/4] デプロイパッケージ作成中...

REM JAR ファイルを検索してコピー
for %%f in (backend\target\tablecraft-backend-*.jar) do (
    set "jarFile=%%f"
    if "!jarFile!"=="backend\target\tablecraft-backend-*-original.jar" set "jarFile="
)

if not defined jarFile (
    echo ERROR: JAR ファイルが見つかりません。バックエンドビルドを確認してください。
    goto error
)

copy "!jarFile!" "%OutputDir%\application.jar" >nul
echo       ✓ JAR ファイルコピー完了: !jarFile!

REM backend/resourcesから必要なファイルをコピー
echo       backend/resourcesから設定ファイルをコピー中...

REM configディレクトリが存在しない場合は作成
if not exist "%OutputDir%\config" mkdir "%OutputDir%\config"

REM backend/src/main/resources/config から本番用設定ファイルをコピー
if exist "backend\src\main\resources\config" (
    xcopy "backend\src\main\resources\config\*" "%OutputDir%\config\" /y >nul
    echo       ✓ 設定ファイルコピー完了
)

REM Procfile作成（Elastic Beanstalk用）
echo web: java -Dserver.port=5000 -Dspring.profiles.active=prod -Dspring.config.additional-location=file:./config/ -jar application.jar > "%OutputDir%\Procfile"

REM デプロイ用ZIPファイル作成
set "zipPath=%OutputDir%-package.zip"
if exist "%zipPath%" del "%zipPath%"

echo       デプロイパッケージを圧縮中...

REM 一時ディレクトリを作成してファイルを集める
set "tempDir=%OutputDir%\temp-zip"
if exist "%tempDir%" rmdir /s /q "%tempDir%"
mkdir "%tempDir%"

copy "%OutputDir%\application.jar" "%tempDir%\" >nul
copy "%OutputDir%\Procfile" "%tempDir%\" >nul
if exist "%OutputDir%\.ebextensions" xcopy "%OutputDir%\.ebextensions" "%tempDir%\.ebextensions\" /e /i /y >nul
if exist "%OutputDir%\config" xcopy "%OutputDir%\config" "%tempDir%\config\" /e /i /y >nul
if exist "%OutputDir%\*.sql" copy "%OutputDir%\*.sql" "%tempDir%\" >nul

REM PowerShellでZIP作成（Windows 10+）
powershell -command "Compress-Archive -Path '%tempDir%\*' -DestinationPath '%zipPath%' -Force" 2>nul
if errorlevel 1 (
    REM PowerShellが使えない場合はZIP作成をスキップ
    echo       WARNING: ZIPファイル作成をスキップしました。手動でファイルを圧縮してください。
) else (
    echo       ✓ ZIPファイル作成完了
)

REM 一時ディレクトリを削除
if exist "%tempDir%" rmdir /s /q "%tempDir%"

echo.
echo ==========================================
echo 🎉 デプロイパッケージ作成完了!
echo ==========================================
echo.
echo 📦 作成されたファイル:
echo   ● %OutputDir%\application.jar
echo   ● %OutputDir%\Procfile
echo   ● %OutputDir%\.ebextensions\
echo   ● %OutputDir%\config\
if exist "%zipPath%" echo   ● %zipPath%
echo.
echo 🚀 次のステップ:
echo   1. RDS MySQL インスタンスを作成
echo   2. mysql-schema.sql を実行してテーブル作成
echo   3. Elastic Beanstalk で %zipPath% をデプロイ
echo.
goto end

:error
echo.
echo ❌ エラーが発生しました。
echo.
exit /b 1

:end
endlocal
exit /b 0