# TableCraft AWS Elastic Beanstalk デプロイ手順書

## 概要
TableCraftをAWS Elastic Beanstalkにデプロイする手順です。  
フルスタック構成（Spring Boot + React）で、外部設定ファイルを参照する仕組みを維持します。

## 📋 前提条件

### AWS リソース
- AWS アカウント
- IAM ロール（Elastic Beanstalk、RDS アクセス権限）
- VPC、セキュリティグループの設定

### ローカル環境
- Java 11+
- Maven 3.6+
- Node.js 16+
- PowerShell 5.0+ または AWS CLI

## 🚀 デプロイ手順

### Phase 1: ローカルビルド

1. **リポジトリの準備**
   ```powershell
   git checkout branch/forAWS/feature
   cd TableCraft
   ```

2. **デプロイパッケージ作成**
   ```powershell
   .\build-for-aws.ps1
   ```
   
   作成される内容：
   - `forDeploy/application.jar` - 実行可能JAR
   - `forDeploy/Procfile` - Elastic Beanstalk起動設定
   - `forDeploy/.ebextensions/` - EB設定ファイル
   - `forDeploy/config/` - 外部設定ファイル
   - `forDeploy-package.zip` - デプロイ用ZIP

### Phase 2: RDS MySQL セットアップ

1. **RDS インスタンス作成**（AWS Console）
   ```
   Engine: MySQL 8.0.35
   Instance class: db.t3.micro
   Allocated storage: 20 GB
   Database name: tablecraft
   Master username: admin
   Master password: [安全なパスワード]
   VPC: Default VPC
   Public access: Yes（開発用、本番では No）
   Security group: MySQL/Aurora (3306)
   ```

2. **データベース初期化**
   ```bash
   # MySQL クライアントで接続
   mysql -h [RDS-ENDPOINT] -u admin -p
   
   # スキーマ実行
   source forDeploy/mysql-schema.sql;
   
   # 確認
   SHOW TABLES;
   ```

### Phase 3: Elastic Beanstalk デプロイ

#### Option A: EB CLI使用（推奨）

1. **EB CLI インストール**
   ```bash
   pip install awsebcli
   ```

2. **アプリケーション初期化**
   ```bash
   cd forDeploy
   eb init
   ```
   
   設定例：
   ```
   Region: 10) ap-northeast-1
   Application Name: tablecraft
   Platform: Java 11 running on 64bit Amazon Linux 2
   CodeCommit: n
   SSH: y
   ```

3. **環境作成**
   ```bash
   eb create tablecraft-prod
   ```

4. **環境変数設定**
   ```bash
   eb setenv \
     RDS_HOSTNAME=[RDS-ENDPOINT] \
     RDS_DB_NAME=tablecraft \
     RDS_USERNAME=admin \
     RDS_PASSWORD=[RDS-PASSWORD] \
     RDS_PORT=3306
   ```

5. **デプロイ実行**
   ```bash
   eb deploy
   ```

6. **動作確認**
   ```bash
   eb open
   # または
   eb status
   ```

#### Option B: AWS Console使用

1. **Elastic Beanstalk コンソールを開く**
   
2. **アプリケーション作成**
   - Application name: `tablecraft`
   - Platform: `Java 11 running on 64bit Amazon Linux 2`
   - Application code: `Upload your code`
   - Source: `forDeploy-package.zip`

3. **環境変数設定**（Configuration → Software）
   ```
   RDS_HOSTNAME: [RDS-ENDPOINT]
   RDS_DB_NAME: tablecraft
   RDS_USERNAME: admin
   RDS_PASSWORD: [RDS-PASSWORD]
   RDS_PORT: 3306
   ```

4. **デプロイ実行**

### Phase 4: 動作確認

1. **ヘルスチェック**
   ```bash
   curl https://[EB-URL]/api/health
   ```
   
   期待される応答：
   ```json
   {
     "status": "UP",
     "database": "Connected",
     "timestamp": "2025-11-21T...",
     "environment": "prod",
     "externalConfig": "Available"
   }
   ```

2. **API動作確認**
   ```bash
   curl -X GET https://[EB-URL]/api/config/data/users \
     -H "Content-Type: application/json" \
     -d '{"tableName":"users"}'
   ```

3. **フロントエンド確認**
   - ブラウザで EB URL にアクセス
   - React アプリケーションが表示されることを確認

## 🔧 設定のポイント

### 外部設定ファイル参照

TableCraftの特徴である「外部設定ファイル参照」は以下で実現：

1. **Elastic Beanstalk 配置時**
   ```yaml
   # .ebextensions/02_external_config.config
   files:
     "/opt/elasticbeanstalk/deployment/app/config/table-config.json":
       source: config/table-config.json
   ```

2. **Spring Boot 設定**
   ```properties
   # application-prod.properties
   spring.config.additional-location=file:./config/
   ```

3. **動作確認**
   ```bash
   curl https://[EB-URL]/api/info
   ```

### 本番用設定の差分

| 項目 | 開発環境 | 本番環境 |
|------|----------|----------|
| データベース | MySQL 8.0 | RDS MySQL |
| ポート | 8082 | 5000 |
| プロファイル | dev | prod |
| SSL | 無効 | 有効 |
| ログレベル | DEBUG | INFO |
| 設定ファイル | JAR内蔵 | 外部参照 |

## 🎛️ 運用管理

### 設定ファイル更新

外部設定ファイルを更新する場合：

1. **設定ファイル変更**
   ```bash
   # ローカルで forDeploy/config/*.json を編集
   ```

2. **再パッケージ & デプロイ**
   ```powershell
   .\build-for-aws.ps1 -SkipFrontendBuild -SkipBackendBuild
   eb deploy
   ```

### ログ確認

```bash
# リアルタイムログ
eb logs --all

# 特定のインスタンスのログ
eb ssh
sudo tail -f /var/log/eb-engine.log
```

### スケーリング

```bash
# 最小/最大インスタンス数変更
eb config
```

## 💰 コスト概算

### 無料枠適用時
- **Elastic Beanstalk**: 無料（EC2料金のみ）
- **EC2 t3.micro**: $0/月（12ヶ月間無料）
- **RDS db.t3.micro**: $0/月（12ヶ月間無料）
- **データ転送**: 1GB/月まで無料

### 有料期間
- **EC2 t3.micro**: 約 $8.5/月
- **RDS db.t3.micro**: 約 $14/月  
- **Application Load Balancer**: 約 $18/月
- **データ転送**: 約 $0.09/GB

**月額総額**: 約 $40-50/月

## ⚠️ トラブルシューティング

### よくある問題

1. **データベース接続エラー**
   ```
   解決: セキュリティグループでポート3306を開放
   確認: eb config → VPC settings
   ```

2. **外部設定ファイル読み込み失敗**
   ```
   解決: .ebextensions/02_external_config.config の確認
   確認: /api/info エンドポイントで設定ファイル状態確認
   ```

3. **メモリ不足エラー**
   ```
   解決: JVMOptions を調整
   設定: .ebextensions/01_environment.config
   ```

4. **フロントエンドが表示されない**
   ```
   解決: static ファイルのビルド・コピー確認
   確認: backend/src/main/resources/static/ の内容
   ```

## 🔐 セキュリティ考慮事項

1. **環境変数での機密情報管理**
   - RDS パスワードは EB 環境変数で管理
   - IAM ロールによるリソースアクセス制御

2. **ネットワークセキュリティ**
   - RDS はプライベートサブネットに配置（本番）
   - セキュリティグループで最小権限の原則

3. **アプリケーションセキュリティ**
   - HTTPS 終端（Application Load Balancer）
   - Spring Security 追加検討

---

*更新日: 2025年11月21日*  
*対象ブランチ: branch/forAWS/feature*