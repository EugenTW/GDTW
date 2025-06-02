package com.gdtw.general.helper;

import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtHelper {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    private static final long EXPIRATION_TIME_MILLIS = 3L * 60 * 1000;

    public JwtHelper(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    public String generateToken(String code, String stage) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(code)
                .claim("stage", stage)
                .issuedAt(now)
                .expiresAt(now.plusMillis(EXPIRATION_TIME_MILLIS))
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public Map<String, Object> validateToken(String token) {
        if (token == null || token.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            Jwt jwt = jwtDecoder.decode(token);
            Map<String, Object> claims = new HashMap<>(jwt.getClaims());
            claims.put("subject", jwt.getSubject());
            return claims;
        } catch (JwtException e) {
            return Collections.emptyMap();
        }
    }

}
