# TableCraft AWS Deployment Builder
# フルスタック構成でElastic Beanstalkデプロイ用パッケージを作成
# UTF-8 with BOM エンコーディング対応

# 文字エンコーディング設定
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

param(
    [string]$OutputDir = ".\bin",
    [switch]$SkipFrontendBuild = $false,
    [switch]$SkipBackendBuild = $false
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "TableCraft AWS Deployment Builder" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

try {
    # 1. フロントエンドビルド
    if (-not $SkipFrontendBuild) {
        Write-Host "[1/4] フロントエンドビルド中..." -ForegroundColor Yellow
        Set-Location "frontend"
        
        if (-not (Test-Path "node_modules")) {
            Write-Host "     依存関係をインストール中..." -ForegroundColor Gray
            npm install
        }
        
        Write-Host "     Reactアプリケーションをビルド中..." -ForegroundColor Gray
        npm run build
        
        if ($LASTEXITCODE -ne 0) {
            throw "フロントエンドビルドに失敗しました"
        }
        
        Set-Location ".."
        Write-Host "     ✅ フロントエンドビルド完了" -ForegroundColor Green
    } else {
        Write-Host "[1/4] フロントエンドビルドをスキップ" -ForegroundColor Yellow
    }
    
    # 2. 静的ファイルをバックエンドにコピー
    Write-Host "[2/4] 静的ファイル統合中..." -ForegroundColor Yellow
    
    # バックエンドの静的ファイルディレクトリを作成
    $staticDir = "backend/src/main/resources/static"
    if (Test-Path $staticDir) {
        Remove-Item $staticDir -Recurse -Force
    }
    New-Item -ItemType Directory -Path $staticDir -Force | Out-Null
    
    # フロントエンドのビルドファイルをコピー
    if (Test-Path "frontend/dist") {
        Copy-Item "frontend/dist/*" $staticDir -Recurse -Force
        Write-Host "     ✅ 静的ファイル統合完了" -ForegroundColor Green
    } else {
        Write-Warning "frontend/dist が見つかりません。フロントエンドビルドを実行してください。"
    }
    
    # 3. バックエンドビルド
    if (-not $SkipBackendBuild) {
        Write-Host "[3/4] バックエンドビルド中..." -ForegroundColor Yellow
        Set-Location "backend"
        
        Write-Host "     Spring Bootアプリケーションをビルド中..." -ForegroundColor Gray
        & mvn clean package -DskipTests -Dspring.profiles.active=prod
        
        if ($LASTEXITCODE -ne 0) {
            throw "バックエンドビルドに失敗しました"
        }
        
        Set-Location ".."
        Write-Host "     ✅ バックエンドビルド完了" -ForegroundColor Green
    } else {
        Write-Host "[3/4] バックエンドビルドをスキップ" -ForegroundColor Yellow
    }
    
    # 4. デプロイパッケージ作成
    Write-Host "[4/4] デプロイパッケージ作成中..." -ForegroundColor Yellow
    
    # JAR ファイルをコピー
    $jarFile = Get-ChildItem "backend/target" -Name "tablecraft-backend-*.jar" | Where-Object { $_ -notlike "*-original*" } | Select-Object -First 1
    
    if ($jarFile) {
        Copy-Item "backend/target/$jarFile" "$OutputDir/application.jar" -Force
        Write-Host "     ✅ JAR ファイルコピー完了: $jarFile" -ForegroundColor Green
    } else {
        throw "JAR ファイルが見つかりません。バックエンドビルドを確認してください。"
    }
    
    # backend/resourcesから必要なファイルをコピー
    Write-Host "     backend/resourcesから設定ファイルをコピー中..." -ForegroundColor Gray
    
    # configディレクトリが存在しない場合は作成
    if (-not (Test-Path "$OutputDir/config")) {
        New-Item -ItemType Directory -Path "$OutputDir/config" -Force | Out-Null
    }
    
    # backend/src/main/resources/config から本番用設定ファイルをコピー
    if (Test-Path "backend/src/main/resources/config") {
        Copy-Item "backend/src/main/resources/config/*" "$OutputDir/config/" -Force
        Write-Host "     ✅ 設定ファイルコピー完了" -ForegroundColor Green
    }
    
    # Procfile作成（Elastic Beanstalk用）
    $procfileContent = @"
web: java -Dserver.port=5000 -Dspring.profiles.active=prod -Dspring.config.additional-location=file:./config/ -jar application.jar
"@
    $procfileContent | Out-File -FilePath "$OutputDir/Procfile" -Encoding utf8 -Force
    
    # デプロイ用ZIPファイル作成
    $zipPath = "$OutputDir-package.zip"
    if (Test-Path $zipPath) {
        Remove-Item $zipPath -Force
    }
    
    Write-Host "     デプロイパッケージを圧縮中..." -ForegroundColor Gray
    
    # PowerShell 5.0+ の Compress-Archive を使用
    $filesToZip = @(
        "$OutputDir/application.jar",
        "$OutputDir/Procfile",
        "$OutputDir/.ebextensions",
        "$OutputDir/config"
    )
    
    # 一時ディレクトリを作成してファイルを集める
    $tempDir = "$OutputDir/temp-zip"
    if (Test-Path $tempDir) {
        Remove-Item $tempDir -Recurse -Force
    }
    New-Item -ItemType Directory -Path $tempDir -Force | Out-Null
    
    Copy-Item "$OutputDir/application.jar" "$tempDir/" -Force
    Copy-Item "$OutputDir/Procfile" "$tempDir/" -Force
    Copy-Item "$OutputDir/.ebextensions" "$tempDir/" -Recurse -Force
    Copy-Item "$OutputDir/config" "$tempDir/" -Recurse -Force
    
    Compress-Archive -Path "$tempDir/*" -DestinationPath $zipPath -Force
    
    # 一時ディレクトリを削除
    Remove-Item $tempDir -Recurse -Force
    
    Write-Host ""
    Write-Host "==========================================" -ForegroundColor Green
    Write-Host "🎉 デプロイパッケージ作成完了!" -ForegroundColor Green
    Write-Host "==========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "📦 作成されたファイル:" -ForegroundColor White
    Write-Host "  • $OutputDir/application.jar" -ForegroundColor Cyan
    Write-Host "  • $OutputDir/Procfile" -ForegroundColor Cyan
    Write-Host "  • $OutputDir/.ebextensions/" -ForegroundColor Cyan
    Write-Host "  • $OutputDir/config/" -ForegroundColor Cyan
    Write-Host "  • $zipPath" -ForegroundColor Magenta
    Write-Host ""
    Write-Host "🚀 次のステップ:" -ForegroundColor Yellow
    Write-Host "  1. RDS MySQL インスタンスを作成" -ForegroundColor White
    Write-Host "  2. mysql-schema.sql を実行してテーブル作成" -ForegroundColor White
    Write-Host "  3. Elastic Beanstalk で $zipPath をデプロイ" -ForegroundColor White
    Write-Host ""
    
} catch {
    Write-Host ""
    Write-Host "❌ エラーが発生しました: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    exit 1
}