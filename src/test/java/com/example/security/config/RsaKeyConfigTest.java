package com.example.security.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RsaKeyConfigTest {

    @InjectMocks
    private RsaKeyConfig rsaKeyConfig;

    @Test
    void jwtKeyProperties_shouldReturnValidProperties() {
        // when
        JwtKeyProperties properties = rsaKeyConfig.jwtKeyProperties();
        
        // then
        assertNotNull(properties);
        assertNotNull(properties.getPublicKey());
        assertNotNull(properties.getPrivateKey());
    }
} 