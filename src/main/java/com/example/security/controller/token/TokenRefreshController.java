package com.example.security.controller.token;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.security.service.jwt.JwtService;
import com.example.security.service.jwt.JwtService.JwtToken;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * トークンリフレッシュコントローラー
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class TokenRefreshController {

    /**
     * JWTサービス
     */
    private final JwtService jwtService;

    /**
     * JWTデコーダー
     */
    private final JwtDecoder jwtDecoder;

    /**
     * ユーザー詳細サービス
     */
    private final UserDetailsService userDetailsService;

    /**
     * トークンリフレッシュレスポンス
     *
     * @param accessToken  アクセストークン
     * @param refreshToken リフレッシュトークン
     */
    public record TokenRefreshResponse(String accessToken, String refreshToken) {
    }

    /**
     * トークンリフレッシュ
     *
     * @param request リクエスト
     * @return トークンリフレッシュレスポンス
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenRefreshResponse> refreshToken(
            HttpServletRequest request) {
        // Authorizationヘッダーからリフレッシュトークンを取得
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // トークンの取得と検証
            String refreshToken = authHeader.substring(7);
            var jwt = jwtDecoder.decode(refreshToken);

            // サブジェクトからユーザー名を取得
            String username = jwt.getSubject();

            // ユーザー情報の取得
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 新しいアクセストークンの生成（リフレッシュトークンは再利用）
            JwtToken jwtToken = jwtService.generateToken(userDetails);

            return ResponseEntity.ok(
                    new TokenRefreshResponse(jwtToken.token(), refreshToken));
        } catch (JwtException | BadCredentialsException e) {
            return ResponseEntity.status(401).build();
        }
    }
}
