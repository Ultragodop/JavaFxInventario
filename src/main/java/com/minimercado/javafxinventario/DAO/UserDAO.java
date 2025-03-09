package com.minimercado.javafxinventario.DAO;

import java.util.HashMap;
import java.util.Map;

/**
 * Data Access Object for user management operations.
 * Provides methods to interact with user data storage.
 */
public class UserDAO {
    
    private Map<String, String> userPasswords = new HashMap<>();
    private Map<String, String> userRoles = new HashMap<>();
    private Map<String, String> userCodes = new HashMap<>();
    
    public UserDAO() {
        // Initialize with default test users
        // In a real implementation, this would connect to a database
        userPasswords.put("admin", "$2a$10$dMl1i0O6RcomQGPrzKRgne8iBl1rOf2VGCgdC1U8EnO7LSy6XInFu"); // "admin123"
        userPasswords.put("cajero", "$2a$10$L0/e6UXnN5l1WdoCbo5Dau3m8LXyaCuK1OzhqHP98pZZ8GnQvhTqu"); // "caja123"
        
        userRoles.put("admin", "admin");
        userRoles.put("cajero", "cajero");
        
        userCodes.put("admin", "ADMIN2023");
    }
    
    /**
     * Check if a user exists in the system
     * 
     * @param username Username to check
     * @return true if user exists, false otherwise
     */
    public boolean userExists(String username) {
        return userPasswords.containsKey(username);
    }
    
    /**
     * Get the hashed password for a user
     * 
     * @param username Username to get password for
     * @return Hashed password or null if user doesn't exist
     */
    public String getUserHashedPassword(String username) {
        return userPasswords.get(username);
    }
    
    /**
     * Add a new user to the system
     * 
     * @param username Username of the new user
     * @param hashedPassword BCrypt hashed password
     * @param role User role (admin, cajero, etc.)
     * @param adminCode Admin code for verification (if applicable)
     * @return true if user was added successfully
     */
    public boolean addUser(String username, String hashedPassword, String role, String adminCode) {
        if (userExists(username)) {
            return false;
        }
        
        userPasswords.put(username, hashedPassword);
        userRoles.put(username, role);
        
        if ("admin".equals(role) && adminCode != null) {
            userCodes.put(username, adminCode);
        }
        
        return true;
    }
    
    /**
     * Check if a user has admin privileges
     * 
     * @param username Username to check
     * @return true if user is an admin, false otherwise
     */
    public boolean isAdmin(String username) {
        String role = userRoles.get(username);
        return role != null && role.equals("admin");
    }
    
    /**
     * Remove a user from the system
     * 
     * @param username Username to remove
     * @return true if user was removed, false if user didn't exist
     */
    public boolean removeUser(String username) {
        if (!userExists(username)) {
            return false;
        }
        
        userPasswords.remove(username);
        userRoles.remove(username);
        userCodes.remove(username);
        
        return true;
    }
    
    /**
     * Get the role of a user
     * 
     * @param username Username to check
     * @return User role or null if user doesn't exist
     */
    public String getUserRole(String username) {
        return userRoles.get(username);
    }
}
