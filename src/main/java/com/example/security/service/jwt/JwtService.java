package com.example.security.service.jwt;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.example.security.config.JwtConfig;

import lombok.RequiredArgsConstructor;

/**
 * JWTサービス
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtConfig jwtConfig;

    /**
     * トークンレスポンス
     */
    public record JwtToken(String token, String refreshToken, Instant expiresAt) {
    }

    /**
     * 認証情報からトークンを生成
     */
    public JwtToken generateToken(Authentication authentication) {
        return generateToken(authentication.getName(),
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));
    }

    /**
     * UserDetailsからトークンを生成
     */
    public JwtToken generateToken(UserDetails userDetails) {
        return generateToken(userDetails.getUsername(),
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));
    }

    /**
     * ユーザー名と権限リストからトークンを生成
     */
    public JwtToken generateToken(String username, Iterable<String> roles) {
        // 現在の時刻
        Instant now = Instant.now();

        // アクセストークンの有効期限
        Instant accessTokenExpiry = now.plus(jwtConfig.getExpiration(), ChronoUnit.MILLIS);

        // リフレッシュトークンの有効期限
        Instant refreshTokenExpiry = now.plus(jwtConfig.getRefreshExpiration(), ChronoUnit.MILLIS);

        // アクセストークンに含めるクレーム
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);

        // アクセストークンの生成
        String accessToken = createToken(username, now, accessTokenExpiry, claims);

        // リフレッシュトークンの生成（権限情報は含めない）
        String refreshToken = createToken(username, now, refreshTokenExpiry, new HashMap<>());

        return new JwtToken(accessToken, refreshToken, accessTokenExpiry);
    }

    /**
     * トークンの生成
     */
    private String createToken(String subject, Instant issuedAt, Instant expiresAt, Map<String, Object> claims) {
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .subject(subject)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt);

        // カスタムクレームの追加
        claims.forEach(claimsBuilder::claim);

        return jwtEncoder.encode(JwtEncoderParameters.from(claimsBuilder.build())).getTokenValue();
    }
}