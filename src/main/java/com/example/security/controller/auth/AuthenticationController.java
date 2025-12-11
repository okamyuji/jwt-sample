package com.example.security.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.security.service.auth.AuthenticationService;
import com.example.security.service.auth.AuthenticationService.AuthenticationRequest;
import com.example.security.service.auth.AuthenticationService.AuthenticationResponse;
import com.example.security.service.auth.AuthenticationService.RegisterRequest;

import lombok.RequiredArgsConstructor;

/**
 * 認証関連のコントローラー
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    /**
     * 認証サービス
     */
    private final AuthenticationService service;

    /**
     * 新規登録
     *
     * @param request 登録リクエスト
     * @return 認証レスポンス
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    /**
     * 認証
     *
     * @param request 認証リクエスト
     * @return 認証レスポンス
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }
}