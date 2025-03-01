package com.example.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * JWT設定
 */
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtConfig {

    /**
     * シークレットキー
     */
    private String secret;

    /**
     * トークンの有効期限
     */
    private long expiration;

    /**
     * リフレッシュトークンの有効期限
     */
    private long refreshExpiration;
}