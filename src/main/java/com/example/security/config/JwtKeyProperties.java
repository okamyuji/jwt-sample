package com.example.security.config;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * JWT鍵設定
 */
@Component
@ConfigurationProperties(prefix = "jwt.key")
@Data
public class JwtKeyProperties {
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;
} 