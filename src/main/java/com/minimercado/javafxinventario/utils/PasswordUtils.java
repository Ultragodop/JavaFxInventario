package com.minimercado.javafxinventario.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and verification without Spring Security dependency.
 */
public class PasswordUtils {
    
    private static final int SALT_LENGTH = 16;
    private static final String HASH_ALGORITHM = "SHA-512";
    private static final int ITERATIONS = 10000;
    
    /**
     * Generates a salted hash for the given password
     * @param password The password to hash
     * @return The hashed password with salt
     */
    public static String hashPassword(String password) {
        try {
            // Generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Hash the password with the salt
            byte[] hash = hashWithSalt(password.toCharArray(), salt, ITERATIONS, HASH_ALGORITHM);
            
            // Format: iterations:algorithm:base64(salt):base64(hash)
            return ITERATIONS + ":" + HASH_ALGORITHM + ":" + 
                   Base64.getEncoder().encodeToString(salt) + ":" +
                   Base64.getEncoder().encodeToString(hash);
                   
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Verifies a password against a stored hash
     * @param password The password to verify
     * @param storedHash The stored hash to verify against
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // Split the stored hash into its components
            String[] parts = storedHash.split(":");
            
            if (parts.length != 4) {
                return false; // Invalid stored hash format
            }
            
            int iterations = Integer.parseInt(parts[0]);
            String algorithm = parts[1];
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] hash = Base64.getDecoder().decode(parts[3]);
            
            // Hash the entered password with the same salt
            byte[] testHash = hashWithSalt(password.toCharArray(), salt, iterations, algorithm);
            
            // Compare the hashes (constant time comparison)
            return MessageDigest.isEqual(hash, testHash);
            
        } catch (Exception e) {
            return false; // If any exception occurs, verification fails
        }
    }
    
    /**
     * Hashes a password with the given salt using PBKDF2
     */
    private static byte[] hashWithSalt(char[] password, byte[] salt, int iterations, String algorithm) 
            throws NoSuchAlgorithmException {
        
        // Convert password to bytes
        byte[] passwordBytes = new byte[password.length];
        for (int i = 0; i < password.length; i++) {
            passwordBytes[i] = (byte) password[i];
        }
        
        // Create message digest and add salt
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        digest.reset();
        digest.update(salt);
        byte[] hash = digest.digest(passwordBytes);
        
        // Additional iterations
        for (int i = 0; i < iterations - 1; i++) {
            digest.reset();
            hash = digest.digest(hash);
        }
        
        return hash;
    }
}
