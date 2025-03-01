package com.example.security.controller.token;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.security.repository.user.UserRepository;
import com.example.security.service.jwt.JwtService;

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
     * ユーザーリポジトリ
     */
    private final UserRepository userRepository;

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
        final String refreshToken;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }

        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            var user = userRepository.findByEmail(userEmail)
                    .orElseThrow();

            if (jwtService.isTokenValid(refreshToken, user)) {
                var jwtToken = jwtService.generateToken(user);

                return ResponseEntity.ok(
                        new TokenRefreshResponse(
                                jwtToken.token(),
                                jwtToken.refreshToken() // 新しいリフレッシュトークンを返す
                        ));
            }
        }

        return ResponseEntity.badRequest().build();
    }
}