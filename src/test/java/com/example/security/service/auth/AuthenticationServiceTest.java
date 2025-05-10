package com.example.security.service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.security.model.Role;
import com.example.security.model.User;
import com.example.security.repository.user.UserRepository;
import com.example.security.service.auth.AuthenticationService.AuthenticationRequest;
import com.example.security.service.auth.AuthenticationService.AuthenticationResponse;
import com.example.security.service.auth.AuthenticationService.RegisterRequest;
import com.example.security.service.jwt.JwtService;
import com.example.security.service.jwt.JwtService.JwtToken;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;
    
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationService authService;

    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final String TOKEN = "test.jwt.token";
    private static final String FIRSTNAME = "John";
    private static final String LASTNAME = "Doe";
    
    private JwtToken jwtToken;

    @BeforeEach
    void setUp() {
        // パスワードエンコーダーのモック設定
        lenient().when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_PASSWORD);
        
        // JWTサービスのモック設定
        jwtToken = new JwtToken(TOKEN, TOKEN, null);
    }

    @Test
    void register_shouldCreateUserAndReturnToken() {
        // given
        RegisterRequest request = new RegisterRequest(FIRSTNAME, LASTNAME, EMAIL, PASSWORD);
        
        User savedUser = User.builder()
                .firstname(FIRSTNAME)
                .lastname(LASTNAME)
                .email(EMAIL)
                .password(ENCODED_PASSWORD)
                .role(Role.USER)
                .build();
        
        when(repository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn(jwtToken);

        // when
        AuthenticationResponse response = authService.register(request);

        // then
        assertNotNull(response);
        assertEquals(TOKEN, response.accessToken());
        assertEquals(TOKEN, response.refreshToken());
    }

    @Test
    void authenticate_shouldReturnTokenForValidCredentials() {
        // given
        AuthenticationRequest request = new AuthenticationRequest(EMAIL, PASSWORD);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(any(Authentication.class))).thenReturn(jwtToken);

        // when
        AuthenticationResponse response = authService.authenticate(request);

        // then
        assertNotNull(response);
        assertEquals(TOKEN, response.accessToken());
        assertEquals(TOKEN, response.refreshToken());
    }
} 