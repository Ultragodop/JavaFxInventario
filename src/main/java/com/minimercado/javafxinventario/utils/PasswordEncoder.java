package com.minimercado.javafxinventario.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Adapter class for Spring Security's BCryptPasswordEncoder.
 * Use this class to encode and verify passwords throughout the application.
 */
public class PasswordEncoder {
    
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    /**
     * Encodes a password using BCrypt hashing.
     * @param rawPassword The plain text password to encode
     * @return The BCrypt hashed password
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }
    
    /**
     * Verifies a raw password against an encoded password.
     * @param rawPassword The plain text password to check
     * @param encodedPassword The hashed password to check against
     * @return true if the passwords match, false otherwise
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
