package com.oceanview.dao;

import java.util.List;
import java.util.Map;

public interface UserDAO {
    boolean authenticate(String username, String password);
    boolean register(String username, String password, String role);
    List<Map<String, Object>> getAllUsers();
    boolean deleteUser(int userId);
}
