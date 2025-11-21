# 🚀 クイックスタートガイド

## 最も簡単な起動方法

### PowerShell (推奨)
```powershell
# 起動
.\start-system.ps1

# 停止  
.\stop-system.ps1
```

### コマンドプロンプト
```cmd
# 起動
start-system.bat
```

### 手動起動
```powershell
# バックエンド
cd backend
mvn clean package -q
Start-Process -WindowStyle Hidden -FilePath "java" -ArgumentList "-jar","target\tablecraft-backend-0.0.1-SNAPSHOT.jar","--spring.profiles.active=dev","--server.port=8082"

# フロントエンド  
cd ..\frontend
npm run dev
```

## アクセス先

| サービス | URL | 説明 |
|----------|-----|------|
| フロントエンド | http://localhost:5173 | メインアプリ |
| バックエンドAPI | http://localhost:8082 | REST API |
| H2コンソール | http://localhost:8082/h2-console | データベース管理 |

## システム停止

```powershell
# 全てのJavaプロセスを停止
taskkill /F /IM java.exe

# または停止スクリプト使用
.\stop-system.ps1
```

## トラブルシューティング

### ポート使用中エラー
```powershell
netstat -ano | findstr :8082
taskkill /F /PID [見つかったPID]
```

### 設定ファイルエラー
```powershell
cd backend
Copy-Item "..\json_create\output\frontend\*" "src\main\resources\" -Force
```

---
詳細は [README.md](README.md) を参照してください。