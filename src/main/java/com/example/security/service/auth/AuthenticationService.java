package com.example.security.service.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.security.model.Role;
import com.example.security.model.User;
import com.example.security.repository.user.UserRepository;
import com.example.security.service.jwt.JwtService;
import com.example.security.service.jwt.JwtService.JwtToken;

import lombok.RequiredArgsConstructor;

/**
 * 認証サービス
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * 認証リクエスト
     *
     * @param email    メールアドレス
     * @param password パスワード
     */
    public record AuthenticationRequest(String email, String password) {
    }

    /**
     * 登録リクエスト
     *
     * @param firstname 名前
     * @param lastname  姓
     * @param email     メールアドレス
     * @param password  パスワード
     */
    public record RegisterRequest(String firstname, String lastname, String email, String password) {
    }

    /**
     * 認証レスポンス
     *
     * @param accessToken  アクセストークン
     * @param refreshToken リフレッシュトークン
     */
    public record AuthenticationResponse(String accessToken, String refreshToken) {
    }

    /**
     * 登録
     *
     * @param request 登録リクエスト
     * @return 認証レスポンス
     */
    @SuppressWarnings("null")
    public AuthenticationResponse register(RegisterRequest request) {
        User user = User.builder()
                .firstname(request.firstname())
                .lastname(request.lastname())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();
        User savedUser = repository.save(user);

        JwtToken jwtToken = jwtService.generateToken(savedUser);

        return new AuthenticationResponse(jwtToken.token(), jwtToken.refreshToken());
    }

    /**
     * 認証
     *
     * @param request 認証リクエスト
     * @return 認証レスポンス
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        JwtToken jwtToken = jwtService.generateToken(authentication);

        return new AuthenticationResponse(jwtToken.token(), jwtToken.refreshToken());
    }
}