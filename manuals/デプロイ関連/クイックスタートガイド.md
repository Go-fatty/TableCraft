# TableCraft AWS デプロイ - クイックスタートガイド

🚀 **最短30分でAWSクラウドにTableCraftをデプロイ！**

---

## ⏱️ クイック手順（経験者向け）

```powershell
# 1. ビルド実行（5分）
cd C:\work\projects\thinking\TableCraft
.\build-for-aws.ps1

# 2. RDS作成（10分） 
# AWS Console → RDS → Create database → MySQL 8.0 → Free tier

# 3. EB デプロイ（10分）
cd forDeploy
eb init
eb create tablecraft-prod
eb setenv RDS_HOSTNAME=your-rds-endpoint RDS_DB_NAME=tablecraft RDS_USERNAME=admin RDS_PASSWORD=your-password
eb deploy

# 4. スキーマ実行（3分）
mysql -h your-rds-endpoint -u admin -p -D tablecraft < backend/src/main/resources/mysql-schema.sql

# 5. 動作確認（2分）
eb open
```

---

## 📋 詳細手順（初心者向け）

### 🔨 Step 1: ローカル環境でのビルド

```powershell
# プロジェクトディレクトリに移動
cd C:\work\projects\thinking\TableCraft

# デプロイパッケージ自動作成
.\build-for-aws.ps1
```

**成功時の出力**:
```
🎉 デプロイパッケージ作成完了!
📦 作成されたファイル:
  • forDeploy/application.jar
  • forDeploy-package.zip
```

### 🗄️ Step 2: AWS RDS (MySQL) セットアップ

#### 2.1 データベース作成
1. [AWS Console](https://console.aws.amazon.com/) にログイン
2. **RDS** → **Create database** をクリック
3. 設定入力:

```yaml
Engine type: MySQL
Engine version: MySQL 8.0.35
Template: Free tier
DB instance identifier: tablecraft-mysql-prod
Master username: admin  
Master password: [強固なパスワード12文字以上]
DB name: tablecraft
```

4. **Create database** をクリック（作成に10-15分）

#### 2.2 セキュリティ設定
1. **EC2** → **Security Groups** → **tablecraft-mysql-prod** を検索
2. **Inbound rules** → **Edit inbound rules**
3. **Add rule**:
   - Type: `MySQL/Aurora`
   - Port: `3306` 
   - Source: `Anywhere IPv4 (0.0.0.0/0)`
4. **Save rules**

#### 2.3 接続エンドポイント確認
1. **RDS** → **Databases** → **tablecraft-mysql-prod**
2. **Connectivity & security** タブ
3. **Endpoint** をコピー（例: `tablecraft-mysql-prod.xxxxxxxxxx.ap-northeast-1.rds.amazonaws.com`）

### 🚀 Step 3: Elastic Beanstalk デプロイ

#### 3.1 EB CLI セットアップ
```powershell
# EB CLI インストール
pip install awsebcli

# 確認
eb --version
```

#### 3.2 AWS認証設定
```powershell
aws configure
# Access Key ID: [IAMユーザーのアクセスキー]
# Secret Access Key: [IAMユーザーのシークレット]
# Default region: ap-northeast-1
# Output format: json
```

#### 3.3 EB初期化・デプロイ
```powershell
# デプロイディレクトリに移動
cd forDeploy

# EB初期化
eb init
# Application Name: tablecraft
# Platform: Java 11 running on 64bit Amazon Linux 2
# CodeCommit: n
# SSH: y

# 環境作成
eb create tablecraft-prod

# データベース接続情報設定
eb setenv RDS_HOSTNAME=tablecraft-mysql-prod.xxxxxxxxxx.ap-northeast-1.rds.amazonaws.com
eb setenv RDS_DB_NAME=tablecraft
eb setenv RDS_USERNAME=admin  
eb setenv RDS_PASSWORD=your-secure-password
eb setenv SPRING_PROFILES_ACTIVE=prod

# アプリケーションデプロイ
eb deploy
```

### 🗄️ Step 4: データベースセットアップ

```powershell
# MySQLクライアントでスキーマ実行
mysql -h tablecraft-mysql-prod.xxxxxxxxxx.ap-northeast-1.rds.amazonaws.com -u admin -p -D tablecraft < backend/src/main/resources/mysql-schema.sql

# パスワード入力: [RDS作成時に設定したパスワード]
```

**実行結果**:
```
Query OK, 0 rows affected
Query OK, 0 rows affected
...
```

### ✅ Step 5: 動作確認

```powershell
# アプリケーション起動
eb open

# ヘルスチェック
$url = "http://tablecraft-prod.ap-northeast-1.elasticbeanstalk.com"
Invoke-RestMethod -Uri "$url/api/health"
```

**期待される応答**:
```json
{
  "status": "UP",
  "database": "Connected", 
  "environment": "prod"
}
```

---

## 🎯 使用開始ガイド

### Webアプリケーションアクセス
**URL**: `http://tablecraft-prod.ap-northeast-1.elasticbeanstalk.com`

### 基本操作確認
1. **テーブル一覧表示** - 7つのテーブルが表示される
2. **データ追加** - ユーザー情報、商品情報等を入力
3. **複合主キーテーブル** - 注文明細、分析データの登録
4. **バリデーション** - 入力チェックが正常動作

### API エンドポイント
```
GET  /api/health          # ヘルスチェック
GET  /api/info            # アプリケーション情報  
POST /api/sql/tables      # テーブル一覧取得
POST /api/sql/findAll     # データ一覧取得
POST /api/sql/create      # データ作成
POST /api/sql/update      # データ更新
POST /api/sql/delete      # データ削除
```

---

## 🛠️ 運用・メンテナンス

### 設定ファイル更新
```powershell
# 設定ファイル編集: forDeploy/config/table-config.json

# 設定のみ更新デプロイ
.\build-for-aws.ps1 -SkipFrontendBuild -SkipBackendBuild
cd forDeploy
eb deploy
```

### アプリケーション更新  
```powershell
# コード修正後のフルデプロイ
.\build-for-aws.ps1
cd forDeploy  
eb deploy
```

### ログ確認
```powershell
eb logs --all
eb ssh  # SSH接続でサーバー直接調査
```

---

## 💰 料金・コスト

### 無料枠（AWS 12ヶ月）
- **Elastic Beanstalk**: 無料
- **EC2 t3.micro**: 750時間/月
- **RDS db.t3.micro**: 750時間/月  
- **データ転送**: 1GB/月
- **月額料金**: $0

### 有料期間（概算）
- **EC2 t3.micro**: $8.76/月
- **RDS db.t3.micro**: $14.45/月
- **データ転送**: $9/100GB
- **合計**: 約$25-40/月

---

## 🆘 トラブルシューティング

### よくある問題

**❌ データベース接続エラー**
```
解決法: 
1. RDSセキュリティグループで3306ポート開放確認
2. 環境変数 RDS_HOSTNAME が正しく設定されているか確認
```

**❌ デプロイ失敗**
```
解決法:
1. eb logs --all でエラー詳細確認
2. JAVAバージョン確認（Java 11必須）
3. 強制再デプロイ: eb deploy --force
```

**❌ 外部設定ファイル読み込み失敗**
```
解決法:
1. /api/info で設定ファイル状態確認
2. .ebextensions/02_external_config.config 確認
```

### 緊急時のロールバック
```powershell
# 前のバージョンに戻す
eb deploy --version [previous-version-label]

# 環境完全再作成
eb terminate tablecraft-prod
eb create tablecraft-prod-v2
```

---

## 📞 サポート

### ドキュメント
- 📖 [完全版デプロイガイド](./DEPLOYMENT_GUIDE.md)
- 📖 [AWS公式ドキュメント](https://docs.aws.amazon.com/elasticbeanstalk/)

### 問い合わせ
- 🔧 技術的な問題: [AWS Developer Forums](https://forums.aws.amazon.com/)
- 💡 機能改善提案: [GitHub Issues](https://github.com/your-repo/issues)

---

**🎉 デプロイ完了後は、本格的なWebアプリケーション開発をお楽しみください！**

*最終更新: 2025年11月21日*