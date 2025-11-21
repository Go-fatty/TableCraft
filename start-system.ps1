# TableCraft システム起動スクリプト (PowerShell版)
# UTF-8 with BOM エンコーディング対応

# PowerShell実行ポリシー確認
if ((Get-ExecutionPolicy) -eq "Restricted") {
    Write-Warning "PowerShell実行ポリシーが制限されています。"
    Write-Host "管理者として以下を実行してください: Set-ExecutionPolicy RemoteSigned" -ForegroundColor Yellow
    exit 1
}

# 文字エンコーディング設定
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "TableCraft システム起動スクリプト" -ForegroundColor Cyan  
Write-Host "==========================================" -ForegroundColor Cyan

try {
    Write-Host ""
    Write-Host "[1/4] 設定ファイル確認中..." -ForegroundColor Yellow
    Set-Location "backend"
    
    if (Test-Path "src\main\resources\config\table-config.json") {
        Write-Host "      ✓ 設定ファイル確認完了" -ForegroundColor Green
    } else {
        Write-Warning "      設定ファイルが見つかりません"
    }
    Write-Host "     完了: 設定ファイルをコピーしました" -ForegroundColor Green

    Write-Host ""
    Write-Host "[2/4] Spring Boot ビルド中..." -ForegroundColor Yellow
    & mvn clean package -q
    if ($LASTEXITCODE -ne 0) {
        throw "ビルドに失敗しました"
    }
    Write-Host "     完了: ビルドが完了しました" -ForegroundColor Green

    Write-Host ""
    Write-Host "[3/4] バックエンド起動中..." -ForegroundColor Yellow
    $jarPath = "target\tablecraft-backend-0.0.1-SNAPSHOT.jar"
    Start-Process -WindowStyle Hidden -FilePath "java" -ArgumentList "-jar",$jarPath,"--spring.profiles.active=dev","--server.port=8082"
    Start-Sleep -Seconds 3
    Write-Host "     完了: Spring Boot (ポート8082) を起動しました" -ForegroundColor Green

    Write-Host ""
    Write-Host "[4/4] フロントエンド起動中..." -ForegroundColor Yellow
    Set-Location "..\frontend"
    Start-Process -WindowStyle Normal -FilePath "cmd" -ArgumentList "/c","npm run dev"
    Write-Host "     完了: React (ポート5173) を起動しました" -ForegroundColor Green

    Write-Host ""
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host "システム起動完了！" -ForegroundColor Green
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "アクセス方法:" -ForegroundColor White
    Write-Host "  フロントエンド: " -NoNewline; Write-Host "http://localhost:5173" -ForegroundColor Yellow
    Write-Host "  バックエンドAPI: " -NoNewline; Write-Host "http://localhost:8082" -ForegroundColor Yellow
    Write-Host "  MySQL接続: " -NoNewline; Write-Host "localhost:3306/tablecraft" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "システム停止: " -NoNewline; Write-Host "taskkill /F /IM java.exe" -ForegroundColor Red
    Write-Host "==========================================" -ForegroundColor Cyan

    # 起動確認
    Write-Host ""
    Write-Host "起動確認中..." -ForegroundColor Yellow
    Start-Sleep -Seconds 5
    
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8082/api/sql/tables" -Method POST -ContentType "application/json" -Body "{}" -TimeoutSec 10
        $data = $response.Content | ConvertFrom-Json
        Write-Host "✅ バックエンドAPI正常動作確認: テーブル数 $($data.count)" -ForegroundColor Green
    }
    catch {
        Write-Host "⚠️  バックエンドAPIの起動確認に失敗しました" -ForegroundColor Yellow
        Write-Host "    数秒後に再度確認してください" -ForegroundColor Yellow
    }

} catch {
    Write-Host ""
    Write-Host "❌ エラーが発生しました: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
} finally {
    Set-Location "$PSScriptRoot"
}

Read-Host "Enterキーで終了"