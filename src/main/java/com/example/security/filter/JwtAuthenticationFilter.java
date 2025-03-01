package com.example.security.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.security.service.jwt.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * JWT認証フィルター
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * ロガー
     */
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /**
     * JWTサービス
     */
    private final JwtService jwtService;

    /**
     * ユーザー詳細サービス
     */
    private final UserDetailsService userDetailsService;

    /**
     * フィルター内部
     * 
     * @param request リクエスト
     * @param response レスポンス
     * @param filterChain フィルターチェーン
     * @throws ServletException サーブレット例外
     * @throws IOException IO例外
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Authorizationヘッダーがないか、形式が無効な場合は次のフィルターに進む
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // "Bearer "の後のトークン部分を抽出
        jwt = authHeader.substring(7);

        try {
            // トークンからユーザー名（またはEメール）を抽出
            userEmail = jwtService.extractUsername(jwt);

            // ユーザー名が存在し、まだ認証されていない場合
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // トークンが有効な場合
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // 認証オブジェクトを作成
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    // リクエスト詳細を設定
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // SecurityContextに認証情報を設定
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // トークンが無効な場合は、エラーを記録するだけで認証は行わない
            logger.error("JWT token validation failed: {}", e.getMessage());
        }

        // フィルターチェーンを続行
        filterChain.doFilter(request, response);
    }
}