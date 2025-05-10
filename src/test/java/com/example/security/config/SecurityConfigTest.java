package com.example.security.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.math.BigInteger;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtKeyProperties jwtKeyProperties;
    
    @Mock
    private UserDetailsService userDetailsService;
    
    @InjectMocks
    private SecurityConfig securityConfig;
    
    @BeforeEach
    void setUp() {
        RSAPublicKey publicKey = mock(RSAPublicKey.class);
        RSAPrivateKey privateKey = mock(RSAPrivateKey.class);
        
        // Using lenient stubs to avoid unnecessary stubbing warnings
        lenient().when(jwtKeyProperties.getPublicKey()).thenReturn(publicKey);
        lenient().when(jwtKeyProperties.getPrivateKey()).thenReturn(privateKey);
        
        // Adding necessary methods for RSAKey.Builder
        BigInteger modulus = new BigInteger("1234567890");
        BigInteger publicExponent = new BigInteger("65537");
        BigInteger privateExponent = new BigInteger("12345");
        
        lenient().when(publicKey.getModulus()).thenReturn(modulus);
        lenient().when(publicKey.getPublicExponent()).thenReturn(publicExponent);
        lenient().when(privateKey.getModulus()).thenReturn(modulus);
        lenient().when(privateKey.getPrivateExponent()).thenReturn(privateExponent);
    }
    
    @Test
    void jwtDecoder_shouldReturnValidDecoder() {
        // when
        JwtDecoder decoder = securityConfig.jwtDecoder();
        
        // then
        assertNotNull(decoder);
    }
    
    @Test
    void jwtEncoder_shouldReturnValidEncoder() {
        // when
        JwtEncoder encoder = securityConfig.jwtEncoder();
        
        // then
        assertNotNull(encoder);
    }
    
    @Test
    void jwtAuthenticationConverter_shouldReturnValidConverter() {
        // when
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverter();
        
        // then
        assertNotNull(converter);
    }
    
    @Test
    void passwordEncoder_shouldReturnValidEncoder() {
        // when
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        
        // then
        assertNotNull(encoder);
    }
    
    @Test
    void authenticationManager_shouldReturnValidManager() {
        // when
        AuthenticationManager manager = securityConfig.authenticationManager();
        
        // then
        assertNotNull(manager);
    }
} 