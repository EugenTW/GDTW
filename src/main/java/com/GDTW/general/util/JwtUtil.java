package com.GDTW.general.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

public class JwtUtil {

    private static final KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
    private static final PrivateKey privateKey = keyPair.getPrivate();
    private static final PublicKey publicKey = keyPair.getPublic();
    private static final long EXPIRATION_TIME = 15 * 60 * 1000;       // 15 mins

    // Generate JWT Token with an additional 'stage' claim
    public static String generateToken(String code, String stage) {
        return Jwts.builder()
                .setSubject(code)
                .claim("stage", stage)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    // Validate and parse JWT Token, return Claims if valid, or null if invalid
    public static Claims validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            // Token is invalid or expired
            return null;
        }
    }

}
