package com.example.security.service.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import com.example.security.config.JwtConfig;
import com.example.security.service.jwt.JwtService.JwtToken;

/**
 * JWTサービスのテスト
 */
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private JwtConfig jwtConfig;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private JwtService jwtService;

    private static final String USERNAME = "test@example.com";
    private static final String TOKEN = "test.jwt.token";
    private static final long EXPIRATION = 3600000; // 1時間

    @BeforeEach
    void setUp() {
        when(jwtConfig.getExpiration()).thenReturn(EXPIRATION);

        // jwtEncoderのモック設定
        Jwt jwt = Jwt.withTokenValue(TOKEN)
                .header("alg", "RS256")
                .subject(USERNAME)
                .claim("roles", Collections.singletonList("USER"))
                .build();
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);
    }

    @Test
    void generateToken_fromUserDetails_shouldReturnValidToken() {
        // given
        UserDetails userDetails = User.withUsername(USERNAME)
                .password("password")
                .authorities("ROLE_USER")
                .build();

        // when
        JwtToken result = jwtService.generateToken(userDetails);

        // then
        assertNotNull(result);
        assertEquals(TOKEN, result.token());
        assertNotNull(result.expiresAt());
    }

    @Test
    void generateToken_fromUsernameAndRoles_shouldReturnValidToken() {
        // given
        List<String> roles = Arrays.asList("ROLE_USER");

        // when
        JwtToken result = jwtService.generateToken(USERNAME, roles);

        // then
        assertNotNull(result);
        assertEquals(TOKEN, result.token());
        assertNotNull(result.expiresAt());
    }
}