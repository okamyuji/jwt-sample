package com.example.security.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.junit.jupiter.api.Test;

import com.example.security.util.KeyGeneratorUtil.RsaKeyProperties;

class KeyGeneratorUtilTest {

    @Test
    void generateRsaKey_shouldReturnValidKeyPair() throws Exception {
        // when
        KeyPair keyPair = KeyGeneratorUtil.generateRsaKey();

        // then
        assertNotNull(keyPair);
        assertTrue(keyPair.getPublic() instanceof RSAPublicKey);
        assertTrue(keyPair.getPrivate() instanceof RSAPrivateKey);
    }

    @Test
    void rsaKeyProperties_generate_shouldReturnValidProperties() {
        // when
        RsaKeyProperties properties = RsaKeyProperties.generate();

        // then
        assertNotNull(properties);
        assertNotNull(properties.publicKey());
        assertNotNull(properties.privateKey());
    }
}