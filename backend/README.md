# Spring Boot REST API

このプロジェクトは、Spring Bootを使用して構築されたREST APIサーバです。ユーザー情報のCRUD操作を提供します。

## プロジェクト構成

- `src/main/java/com/example/api/Application.java`: アプリケーションのエントリポイントです。
- `src/main/java/com/example/api/controller/UserController.java`: REST APIのエンドポイントを定義します。
- `src/main/java/com/example/api/entity/User.java`: ユーザーエンティティを定義します。
- `src/main/java/com/example/api/repository/UserRepository.java`: データベース操作を行うリポジトリインターフェースです。
- `src/main/java/com/example/api/service/UserService.java`: ビジネスロジックを実装するサービスクラスです。
- `src/main/resources/application.properties`: データベース接続情報を定義します。
- `src/main/resources/data.sql`: 初期データをデータベースに挿入するためのSQLスクリプトです。
- `pom.xml`: Mavenプロジェクトの設定ファイルです。

## セットアップ手順

1. プロジェクトをクローンまたはダウンロードします。
2. `application.properties`ファイルを編集し、データベース接続情報を設定します。
3. Mavenを使用して依存関係をインストールします。
   ```
   mvn clean install
   ```
4. アプリケーションを起動します。
   ```
   mvn spring-boot:run
   ```
5. APIエンドポイントにアクセスして、CRUD操作を実行します。

## APIエンドポイント

- `POST /users`: 新しいユーザーを作成します。
- `GET /users/{id}`: 指定したIDのユーザーを取得します。
- `PUT /users/{id}`: 指定したIDのユーザー情報を更新します。
- `DELETE /users/{id}`: 指定したIDのユーザーを削除します。

## ライセンス

このプロジェクトはMITライセンスの下で提供されています。