package com.example.security.util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.stereotype.Component;

/**
 * 鍵生成ユーティリティ
 */
@Component
public class KeyGeneratorUtil {

    /**
     * RSA鍵ペアを生成する
     *
     * @return RSA鍵ペア
     * @throws Exception 例外
     */
    public static KeyPair generateRsaKey() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * RSA鍵ペアを含むオブジェクト
     */
    public record RsaKeyProperties(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
        public static RsaKeyProperties generate() {
            try {
                KeyPair keyPair = generateRsaKey();
                RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
                RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
                return new RsaKeyProperties(publicKey, privateKey);
            } catch (Exception e) {
                throw new RuntimeException("鍵の生成に失敗しました", e);
            }
        }
    }
}