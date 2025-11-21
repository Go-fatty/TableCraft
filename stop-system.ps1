# APIServerByProperties システム停止スクリプト

Write-Host "==========================================" -ForegroundColor Red
Write-Host "TableCraft システム停止" -ForegroundColor Red
Write-Host "==========================================" -ForegroundColor Red

Write-Host ""
Write-Host "実行中のJavaプロセスを確認中..." -ForegroundColor Yellow

$javaProcesses = Get-Process java -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Write-Host "以下のJavaプロセスが見つかりました:" -ForegroundColor White
    $javaProcesses | Format-Table Id, ProcessName, StartTime -AutoSize
    
    Write-Host ""
    $confirm = Read-Host "これらのJavaプロセスを停止しますか？ (y/n)"
    
    if ($confirm -eq 'y' -or $confirm -eq 'Y') {
        try {
            taskkill /F /IM java.exe
            Write-Host "✅ Javaプロセスを停止しました" -ForegroundColor Green
        }
        catch {
            Write-Host "❌ Javaプロセスの停止に失敗しました" -ForegroundColor Red
        }
    } else {
        Write-Host "停止をキャンセルしました" -ForegroundColor Yellow
    }
} else {
    Write-Host "実行中のJavaプロセスは見つかりませんでした" -ForegroundColor Green
}

Write-Host ""
Write-Host "Node.jsプロセスを確認中..." -ForegroundColor Yellow

$nodeProcesses = Get-Process node -ErrorAction SilentlyContinue
if ($nodeProcesses) {
    Write-Host "以下のNode.jsプロセスが見つかりました:" -ForegroundColor White
    $nodeProcesses | Format-Table Id, ProcessName, StartTime -AutoSize
    
    Write-Host ""
    $confirm = Read-Host "これらのNode.jsプロセス(フロントエンド)を停止しますか？ (y/n)"
    
    if ($confirm -eq 'y' -or $confirm -eq 'Y') {
        try {
            $nodeProcesses | ForEach-Object { Stop-Process -Id $_.Id -Force }
            Write-Host "✅ Node.jsプロセスを停止しました" -ForegroundColor Green
        }
        catch {
            Write-Host "❌ Node.jsプロセスの停止に失敗しました" -ForegroundColor Red
        }
    }
} else {
    Write-Host "実行中のNode.jsプロセスは見つかりませんでした" -ForegroundColor Green
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Red
Write-Host "システム停止完了" -ForegroundColor Red  
Write-Host "==========================================" -ForegroundColor Red

Read-Host "Enterキーで終了"