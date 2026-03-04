package com.oceanview.service;

import com.oceanview.dao.UserDAO;
import com.oceanview.dao.impl.UserDAOImpl;

import java.util.List;
import java.util.Map;

/**
 * Service Layer for User Authentication and Management.
 */
public class UserService {

    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAOImpl();
    }

    /** Authenticates a user against the database. */
    public boolean login(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) return false;
        return userDAO.authenticate(username.trim(), password);
    }

    /** Registers a new staff member. Role: 'Staff' or 'Manager'. */
    public boolean register(String username, String password, String role) {
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) return false;
        String r = (role == null || role.trim().isEmpty()) ? "Staff" : role.trim();
        return userDAO.register(username.trim(), password, r);
    }

    /** Returns all users (id, username, role) for the management UI. */
    public List<Map<String, Object>> getAllUsers() {
        return userDAO.getAllUsers();
    }

    /** Removes a user account by ID. */
    public boolean deleteUser(int userId) {
        return userDAO.deleteUser(userId);
    }
}
