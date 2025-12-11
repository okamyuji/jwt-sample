# ============================================
# Stage 1: Build
# ============================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Maven Wrapperと設定ファイルをコピー
COPY mvnw pom.xml ./
COPY .mvn .mvn

# 依存関係のダウンロード（キャッシュ効率化）
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# ソースコードをコピーしてビルド
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# JARファイルを展開してレイヤー分離（Spring Boot Layered JAR）
RUN java -Djarmode=layertools -jar target/*.jar extract --destination extracted

# ============================================
# Stage 2: Runtime
# ============================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# セキュリティ: 非rootユーザーで実行 + curlインストール（ヘルスチェック用）
RUN apk add --no-cache curl && \
    addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -D appuser

WORKDIR /app

# Spring Boot Layered JARの各レイヤーをコピー（キャッシュ効率化、所有権設定）
COPY --from=builder --chown=appuser:appgroup /app/extracted/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /app/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /app/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /app/extracted/application/ ./

# 非rootユーザーに切り替え
USER appuser

# ヘルスチェック用
EXPOSE 8080

# JVMメモリ設定（コンテナ環境最適化）
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# アプリケーション起動
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]

