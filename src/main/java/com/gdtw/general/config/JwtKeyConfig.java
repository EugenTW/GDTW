package com.gdtw.general.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtKeyConfig {

    /**
     * Development default: Generates a new RSA key pair on each application startup.
     * All JWT tokens are valid for 3 minutes; tokens will become invalid after a restart.
     * For production deployments, load a persistent key pair from a file or environment variable
     * to avoid premature token invalidation.
     */

    @Bean
    public KeyPair keyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    @Bean
    public RSAPublicKey publicKey(KeyPair keyPair) {
        return (RSAPublicKey) keyPair.getPublic();
    }

    @Bean
    public RSAPrivateKey privateKey(KeyPair keyPair) {
        return (RSAPrivateKey) keyPair.getPrivate();
    }

}

