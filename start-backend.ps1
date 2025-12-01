# TableCraft Backend 起動スクリプト (PowerShell)
Set-Location $PSScriptRoot\backend

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "TableCraft Backend Starting..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

mvn clean compile spring-boot:run
