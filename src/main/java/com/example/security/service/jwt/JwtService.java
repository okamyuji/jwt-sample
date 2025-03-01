package com.example.security.service.jwt;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.security.config.JwtConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

/**
 * JWTサービス
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtConfig jwtConfig;

    /**
     * トークンレスポンス
     */
    public record JwtToken(String token, String refreshToken, Date expiresAt) {
    }

    /**
     * トークンの抽出
     * 
     * @param token トークン
     * @return ユーザー名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * クレームの抽出
     * 
     * @param token          トークン
     * @param claimsResolver クレーム抽出関数
     * @return クレーム
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * トークンの生成
     * 
     * @param userDetails ユーザー詳細
     * @return トークンレスポンス
     */
    public JwtToken generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * 追加のクレームを含むトークンを生成
     * 
     * @param extraClaims 追加のクレーム
     * @param userDetails ユーザー詳細
     * @return トークンレスポンス
     */
    public JwtToken generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails) {
        // アクセストークンの生成
        String accessToken = buildToken(extraClaims, userDetails, jwtConfig.getExpiration());

        // リフレッシュトークンの生成（通常は最小限のクレームを含む）
        String refreshToken = buildToken(new HashMap<>(), userDetails, jwtConfig.getRefreshExpiration());

        // トークンの有効期限
        Date expiresAt = new Date(System.currentTimeMillis() + jwtConfig.getExpiration());

        return new JwtToken(accessToken, refreshToken, expiresAt);
    }

    /**
     * トークンのビルド
     * 
     * @param extraClaims 追加のクレーム
     * @param userDetails ユーザー詳細
     * @param expiration  トークンの有効期限
     * @return トークン
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * トークンの有効性を確認
     * 
     * @param token       トークン
     * @param userDetails ユーザー詳細
     * @return トークンが有効かどうか
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * トークンの期限切れを確認
     * 
     * @param token トークン
     * @return トークンが期限切れかどうか
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * トークンの期限切れを抽出
     * 
     * @param token トークン
     * @return トークンの期限切れ
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * トークンのクレームを抽出
     * 
     * @param token トークン
     * @return トークンのクレーム
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * SignInKeyの取得
     * 
     * @return SignInKey
     */
    private javax.crypto.SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtConfig.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}