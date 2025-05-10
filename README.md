# JWT認証サンプル - Spring Security OAuth2 Resource Server 実装

Zennの記事 [Spring Boot 3でのJWT認証実装：Spring Security標準機能の活用](https://zenn.dev/okamyuji/articles/0bfcc5a9b17cb5)のソースコードです。

## 最新の更新内容（2025年5月10日）

記事の更新にともなって、以下の変更を実施しました

- カスタムJWTフィルターを削除し、Spring Security OAuth2 Resource Serverの標準機能を使用
- 非対称鍵（RSA）を使用したJWT認証の実装
- Java 21の新機能（Record、TextBlocks）を活用
- セキュリティ強化とベストプラクティスの適用
- 包括的なユニットテストの追加（モデル、リポジトリ、サービス、コントローラ）

## 主な機能

- JWT（JSON Web Token）を使用した認証
- アクセストークンとリフレッシュトークン
- RSA公開鍵/秘密鍵による署名
- Spring Security OAuth2 Resource Serverの活用
- ステートレスな認証

## 技術スタック

- Java 21
- Spring Boot 3.4.3
- Spring Security 6
- Spring Data JPA
- H2 Database（テスト用）
- JUnit 5 & Mockito（テスト用）

## 使い方

1. リポジトリをクローン
2. mvnw clean package でビルド
3. mvnw spring-boot:run で実行
4. http://localhost:8080/api/v1/auth/register でユーザー登録
5. http://localhost:8080/api/v1/auth/authenticate で認証トークン取得
6. Authorization: Bearer <token> ヘッダーを使用してAPIにアクセス

## テスト実行

プロジェクトには以下のような包括的なテストが含まれています：

```bash
# すべてのテストを実行
mvnw test

# 特定のテストクラスのみ実行
mvnw test -Dtest=UserTest
mvnw test -Dtest=UserRepositoryTest
mvnw test -Dtest=JwtServiceTest
```

テストカバレッジには以下が含まれます：

- モデル層テスト（UserTest, RoleTest）
- リポジトリ層テスト（UserRepositoryTest）
- サービス層テスト（JwtService, AuthenticationService）
- コントローラ層テスト（TokenRefreshController）
- 設定テスト（JwtConfig, RsaKeyConfig, SecurityConfig）
- ユーティリティテスト（KeyGeneratorUtil）

## API エンドポイント

| エンドポイント | メソッド | 説明 | 認証要否 |
|--------------|--------|-----|---------|
| `/api/v1/auth/register` | POST | 新規ユーザー登録 | 不要 |
| `/api/v1/auth/authenticate` | POST | ユーザー認証・JWT取得 | 不要 |
| `/api/v1/auth/refresh-token` | POST | トークン更新 | 必要 (リフレッシュトークン) |

## ライセンス

MIT
