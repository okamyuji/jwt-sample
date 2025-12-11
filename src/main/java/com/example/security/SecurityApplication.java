package com.example.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.example.security.config.JwtConfig;

/**
 * セキュリティアプリケーション
 */
@SpringBootApplication
@EnableConfigurationProperties(JwtConfig.class)
public class SecurityApplication {

    /**
     * メインメソッド
     *
     * @param args 引数
     */
    public static void main(String[] args) {
        SpringApplication.run(SecurityApplication.class, args);
    }
}