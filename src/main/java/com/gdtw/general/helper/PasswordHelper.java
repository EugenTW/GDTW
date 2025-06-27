package com.gdtw.general.helper;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHelper {

    private final Argon2PasswordEncoder argon2Encoder;
    private final BCryptPasswordEncoder bcryptEncoder;

    public PasswordHelper() {
        this.argon2Encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        this.bcryptEncoder = new BCryptPasswordEncoder();
    }

    // ==== Argon2 ====
    public String encodeArgon2(String rawPassword) {
        return argon2Encoder.encode(rawPassword);
    }

    public boolean matchesArgon2(String rawPassword, String encodedPassword) {
        return argon2Encoder.matches(rawPassword, encodedPassword);
    }

    // ==== BCrypt ====
    public String encodeBCrypt(String rawPassword) {
        return bcryptEncoder.encode(rawPassword);
    }

    public boolean matchesBCrypt(String rawPassword, String encodedPassword) {
        return bcryptEncoder.matches(rawPassword, encodedPassword);
    }

}
