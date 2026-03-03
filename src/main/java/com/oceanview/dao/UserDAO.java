package com.oceanview.dao;
public interface UserDAO {
    boolean authenticate(String username, String password);
}
