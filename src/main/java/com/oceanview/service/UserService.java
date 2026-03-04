package com.oceanview.service;

import com.oceanview.dao.UserDAO;
import com.oceanview.dao.impl.UserDAOImpl;

/**
 * Service Layer for User Authentication.
 * Delegates to the UserDAO for database operations.
 */
public class UserService {

    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAOImpl();
    }

    /**
     * Authenticates a user by checking credentials against the database.
     */
    public boolean login(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            return false;
        }
        return userDAO.authenticate(username, password);
    }
}