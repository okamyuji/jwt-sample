package com.example.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.security.filter.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * セキュリティ設定
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        /**
         * JWT認証フィルター
         */
        private final JwtAuthenticationFilter jwtAuthFilter;

        /**
         * 認証プロバイダー
         */
        private final AuthenticationProvider authenticationProvider;

        /**
         * セキュリティフィルターチェーン
         * 
         * @param http HTTPセキュリティ
         * @return セキュリティフィルターチェーン
         * @throws Exception 例外
         */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                // Java 21のTextBlock機能を使用して、URL許可リストを定義
                String[] publicUrls = """
                                /api/v1/auth/**
                                /v3/api-docs/**
                                /swagger-ui/**
                                /swagger-ui.html
                                /swagger-resources/**
                                /actuator/**
                                """.trim().split("\\s+");

                return http
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(publicUrls).permitAll()
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider)
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                                .build();
        }
}