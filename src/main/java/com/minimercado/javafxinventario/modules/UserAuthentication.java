package com.minimercado.javafxinventario.modules;

import com.minimercado.javafxinventario.DAO.UserDAO;
import com.minimercado.javafxinventario.utils.PasswordEncoder;

/**
 * Handles user authentication and session management
 */
public class UserAuthentication {
    
    public static UserAuthentication instance;
    public UserDAO userDAO;
    public String currentUser;
    public boolean isAuthenticated;
    public String userRole;
    
    public UserAuthentication() {
        userDAO = new UserDAO();
        isAuthenticated = false;
    }
    
    /**
     * Get singleton instance of UserAuthentication
     * @return The UserAuthentication instance
     */
    public static synchronized UserAuthentication getInstance() {
        if (instance == null) {
            instance = new UserAuthentication();
        }
        return instance;
    }
    
    /**
     * Authenticate a user with username and password
     * @param username Username to authenticate
     * @param password Password to verify
     * @return true if authentication successful, false otherwise
     */
    public boolean authenticate(String username, String password) {
        // Check if user exists
        if (!userDAO.userExists(username)) {
            return false;
        }
        
        // Get stored password hash
        String hashedPassword = userDAO.getUserHashedPassword(username);
        
        // Verify password using our adapter
        boolean authenticated = PasswordEncoder.matches(password, hashedPassword);
        
        if (authenticated) {
            // Set session information
            this.currentUser = username;
            this.isAuthenticated = true;
            this.userRole = userDAO.getUserRole(username);
        }
        
        return authenticated;
    }
    
    /**
     * Log out the current user
     */
    public void logout() {
        currentUser = null;
        isAuthenticated = false;
        userRole = null;
    }
    
    /**
     * Check if the current session is authenticated
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        return isAuthenticated;
    }
    
    /**
     * Get the current logged in username
     * @return Username or null if not logged in
     */
    public String getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Get the role of the current user
     * @return User role or null if not logged in
     */
    public String getUserRole() {
        return userRole;
    }
    
    /**
     * Check if the current user is an admin
     * @return true if admin, false otherwise
     */
    public boolean isAdmin() {
        return "admin".equals(userRole);
    }
    
    /**
     * Check if a specific username has admin privileges
     * @param username The username to check
     * @return true if the user is an admin, false otherwise
     */
    public boolean isAdmin(String username) {
        return userDAO.isAdmin(username);
    }
    
    /**
     * Register a new user
     * @param username Username to register
     * @param password Password for the new user
     * @param role Role for the new user
     * @param adminCode Admin verification code (if registering an admin)
     * @return true if registration successful, false otherwise
     */
    public boolean registerUser(String username, String password, String role, String adminCode) {
        // Check if user already exists
        if (userDAO.userExists(username)) {
            return false;
        }
        
        // Hash the password using our adapter
        String hashedPassword = PasswordEncoder.encode(password);
        
        // Add the user to the database
        return userDAO.addUser(username, hashedPassword, role, adminCode);
    }
    
    /**
     * Register a new admin user
     * @param username Username to register
     * @param password Password for the new admin
     * @param adminCode Admin verification code
     * @return true if registration successful, false otherwise
     */
    public boolean registerAdmin(String username, String password, String adminCode) {
        return registerUser(username, password, "admin", adminCode);
    }
}
