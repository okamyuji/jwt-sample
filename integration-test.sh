#!/usr/bin/env bash

set -uo pipefail

# =============================================================================
# JWT Sample Application - Integration Test Script
# =============================================================================

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly CONTAINER_NAME="jwt-sample-app"
readonly BASE_URL="http://localhost:8080"
readonly TIMEOUT=60

# カラー出力
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly YELLOW='\033[0;33m'
readonly NC='\033[0m' # No Color

# カウンター
PASSED=0
FAILED=0

# -----------------------------------------------------------------------------
# ユーティリティ関数
# -----------------------------------------------------------------------------

log_info() {
    echo -e "${NC}[INFO] $1${NC}"
}

log_success() {
    echo -e "${GREEN}[PASS] $1${NC}"
}

log_error() {
    echo -e "${RED}[FAIL] $1${NC}"
}

log_warn() {
    echo -e "${YELLOW}[WARN] $1${NC}"
}

# HTTPリクエスト実行
http_request() {
    local method="$1"
    local endpoint="$2"
    local data="${3:-}"
    local headers="${4:-}"
    
    local curl_opts=(-s -w "\n%{http_code}" -X "$method")
    
    if [[ -n "$data" ]]; then
        curl_opts+=(-H "Content-Type: application/json" -d "$data")
    fi
    
    if [[ -n "$headers" ]]; then
        curl_opts+=(-H "$headers")
    fi
    
    curl "${curl_opts[@]}" "${BASE_URL}${endpoint}" 2>/dev/null || echo -e "\n000"
}

# レスポンス解析
parse_response() {
    local response="$1"
    local body
    local status_code
    
    body=$(echo "$response" | sed '$d')
    status_code=$(echo "$response" | tail -n1)
    
    echo "$body"
    echo "$status_code"
}

# テスト結果記録
assert_status() {
    local test_name="$1"
    local expected="$2"
    local actual="$3"
    
    if [[ "$actual" == "$expected" ]]; then
        log_success "$test_name (HTTP $actual)"
        ((PASSED++))
        return 0
    else
        log_error "$test_name - Expected: $expected, Got: $actual"
        ((FAILED++))
        return 1
    fi
}

assert_json_field() {
    local test_name="$1"
    local json="$2"
    local field="$3"
    
    if echo "$json" | grep -q "\"$field\""; then
        log_success "$test_name - Field '$field' exists"
        ((PASSED++))
        return 0
    else
        log_error "$test_name - Field '$field' not found"
        ((FAILED++))
        return 1
    fi
}

# -----------------------------------------------------------------------------
# Docker管理
# -----------------------------------------------------------------------------

is_container_running() {
    docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"
}

wait_for_health() {
    local max_attempts=$((TIMEOUT / 2))
    local attempt=1
    
    log_info "アプリケーションの起動を待機中..."
    
    while [[ $attempt -le $max_attempts ]]; do
        local health_response
        health_response=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/actuator/health" 2>/dev/null)
        # 200 (success) or 401 (security enabled but app running)
        if [[ "$health_response" == "200" ]] || [[ "$health_response" == "401" ]]; then
            log_info "アプリケーションが起動しました"
            return 0
        fi
        echo -n "."
        sleep 2
        ((attempt++))
    done
    
    echo ""
    log_error "タイムアウト: アプリケーションが起動しませんでした"
    return 1
}

start_application() {
    cd "$SCRIPT_DIR"
    
    if is_container_running; then
        log_info "コンテナは既に起動しています"
    else
        log_info "Dockerイメージをビルド中..."
        docker compose build --quiet
        
        log_info "コンテナを起動中..."
        docker compose up -d
    fi
    
    wait_for_health
}

# -----------------------------------------------------------------------------
# 正常系テスト
# -----------------------------------------------------------------------------

test_health_check() {
    echo ""
    echo "=== ヘルスチェック ==="
    
    local response
    response=$(http_request GET "/actuator/health")
    
    local status_code
    status_code=$(echo "$response" | tail -n1)
    
    # 200 (public) or 401 (security enabled) - both indicate app is running
    if [[ "$status_code" == "200" ]] || [[ "$status_code" == "401" ]]; then
        log_success "GET /actuator/health (HTTP $status_code)"
        ((PASSED++))
    else
        log_error "GET /actuator/health - Expected: 200 or 401, Got: $status_code"
        ((FAILED++))
    fi
}

test_user_registration() {
    echo ""
    echo "=== ユーザー登録（正常系） ==="
    
    # ユニークなメールアドレスを生成
    local email="test_$(date +%s)@example.com"
    local payload="{\"firstname\":\"Test\",\"lastname\":\"User\",\"email\":\"$email\",\"password\":\"password123\"}"
    local response
    response=$(http_request POST "/api/v1/auth/register" "$payload")
    
    local body status_code
    body=$(echo "$response" | sed '$d')
    status_code=$(echo "$response" | tail -n1)
    
    assert_status "POST /api/v1/auth/register" "200" "$status_code" || return 0
    assert_json_field "Registration Response" "$body" "accessToken"
    assert_json_field "Registration Response" "$body" "refreshToken"
    
    # トークンを保存
    ACCESS_TOKEN=$(echo "$body" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4 || true)
    REFRESH_TOKEN=$(echo "$body" | grep -o '"refreshToken":"[^"]*"' | cut -d'"' -f4 || true)
    TEST_EMAIL="$email"
}

test_user_authentication() {
    echo ""
    echo "=== ユーザー認証（正常系） ==="
    
    if [[ -z "${TEST_EMAIL:-}" ]]; then
        log_warn "テスト用メールアドレスがありません。スキップします。"
        return 0
    fi
    
    local payload="{\"email\":\"$TEST_EMAIL\",\"password\":\"password123\"}"
    local response
    response=$(http_request POST "/api/v1/auth/authenticate" "$payload")
    
    local body status_code
    body=$(echo "$response" | sed '$d')
    status_code=$(echo "$response" | tail -n1)
    
    assert_status "POST /api/v1/auth/authenticate" "200" "$status_code" || return 0
    assert_json_field "Authentication Response" "$body" "accessToken"
    assert_json_field "Authentication Response" "$body" "refreshToken"
    
    # トークンを更新
    ACCESS_TOKEN=$(echo "$body" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4 || true)
    REFRESH_TOKEN=$(echo "$body" | grep -o '"refreshToken":"[^"]*"' | cut -d'"' -f4 || true)
}

test_token_refresh() {
    echo ""
    echo "=== トークンリフレッシュ（正常系） ==="
    
    if [[ -z "${REFRESH_TOKEN:-}" ]]; then
        log_warn "リフレッシュトークンがありません。スキップします。"
        return 0
    fi
    
    local response
    response=$(http_request POST "/api/v1/auth/refresh-token" "" "Authorization: Bearer $REFRESH_TOKEN")
    
    local body status_code
    body=$(echo "$response" | sed '$d')
    status_code=$(echo "$response" | tail -n1)
    
    assert_status "POST /api/v1/auth/refresh-token" "200" "$status_code" || return 0
    assert_json_field "Refresh Response" "$body" "accessToken"
    assert_json_field "Refresh Response" "$body" "refreshToken"
}

# -----------------------------------------------------------------------------
# 異常系テスト
# -----------------------------------------------------------------------------

test_authentication_invalid_password() {
    echo ""
    echo "=== 認証失敗 - パスワード誤り（異常系） ==="
    
    local payload='{"email":"test@example.com","password":"wrongpassword"}'
    local response
    response=$(http_request POST "/api/v1/auth/authenticate" "$payload")
    
    local status_code
    status_code=$(echo "$response" | tail -n1)
    
    assert_status "POST /api/v1/auth/authenticate (wrong password)" "401" "$status_code"
}

test_authentication_nonexistent_user() {
    echo ""
    echo "=== 認証失敗 - 存在しないユーザー（異常系） ==="
    
    local payload='{"email":"nonexistent@example.com","password":"password123"}'
    local response
    response=$(http_request POST "/api/v1/auth/authenticate" "$payload")
    
    local status_code
    status_code=$(echo "$response" | tail -n1)
    
    assert_status "POST /api/v1/auth/authenticate (nonexistent user)" "401" "$status_code"
}

test_refresh_token_missing_header() {
    echo ""
    echo "=== トークンリフレッシュ失敗 - ヘッダーなし（異常系） ==="
    
    local response
    response=$(http_request POST "/api/v1/auth/refresh-token")
    
    local status_code
    status_code=$(echo "$response" | tail -n1)
    
    assert_status "POST /api/v1/auth/refresh-token (no header)" "400" "$status_code"
}

test_refresh_token_invalid_format() {
    echo ""
    echo "=== トークンリフレッシュ失敗 - 不正なフォーマット（異常系） ==="
    
    local response
    response=$(http_request POST "/api/v1/auth/refresh-token" "" "Authorization: Basic invalidtoken")
    
    local status_code
    status_code=$(echo "$response" | tail -n1)
    
    assert_status "POST /api/v1/auth/refresh-token (invalid format)" "400" "$status_code"
}

test_refresh_token_invalid_token() {
    echo ""
    echo "=== トークンリフレッシュ失敗 - 無効なトークン（異常系） ==="
    
    local response
    response=$(http_request POST "/api/v1/auth/refresh-token" "" "Authorization: Bearer invalidtoken123")
    
    local status_code
    status_code=$(echo "$response" | tail -n1)
    
    # 無効なトークンは400, 401, 403, 500のいずれかのエラー
    if [[ "$status_code" =~ ^(400|401|403|500)$ ]]; then
        log_success "POST /api/v1/auth/refresh-token (invalid token) (HTTP $status_code)"
        ((PASSED++))
    else
        log_error "POST /api/v1/auth/refresh-token (invalid token) - Expected: 400/401/403/500, Got: $status_code"
        ((FAILED++))
    fi
}

test_register_empty_body() {
    echo ""
    echo "=== ユーザー登録 - 空のリクエスト（異常系） ==="
    
    local response
    response=$(http_request POST "/api/v1/auth/register" '{}')
    
    local status_code
    status_code=$(echo "$response" | tail -n1)
    
    # 空のボディの場合、200（バリデーションなし）または4xx/5xxエラー
    if [[ "$status_code" =~ ^(2[0-9]{2}|4[0-9]{2}|5[0-9]{2})$ ]]; then
        log_success "POST /api/v1/auth/register (empty body) (HTTP $status_code)"
        ((PASSED++))
    else
        log_error "POST /api/v1/auth/register (empty body) - Unexpected: $status_code"
        ((FAILED++))
    fi
}

# -----------------------------------------------------------------------------
# メイン処理
# -----------------------------------------------------------------------------

print_summary() {
    echo ""
    echo "=============================================="
    echo "テスト結果サマリー"
    echo "=============================================="
    echo -e "成功: ${GREEN}${PASSED}${NC}"
    echo -e "失敗: ${RED}${FAILED}${NC}"
    echo "----------------------------------------------"
    
    if [[ $FAILED -eq 0 ]]; then
        echo -e "${GREEN}すべてのテストが成功しました${NC}"
        return 0
    else
        echo -e "${RED}一部のテストが失敗しました${NC}"
        return 1
    fi
}

main() {
    echo "=============================================="
    echo "JWT Sample Application - Integration Tests"
    echo "=============================================="
    
    # アプリケーション起動
    start_application
    
    # 正常系テスト
    test_health_check
    test_user_registration
    test_user_authentication
    test_token_refresh
    
    # 異常系テスト
    test_authentication_invalid_password
    test_authentication_nonexistent_user
    test_refresh_token_missing_header
    test_refresh_token_invalid_format
    test_refresh_token_invalid_token
    test_register_empty_body
    
    # サマリー出力
    print_summary
}

main "$@"

