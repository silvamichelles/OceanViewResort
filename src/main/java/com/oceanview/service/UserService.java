package com.oceanview.service;

import com.oceanview.network.WebServiceClient;

/**
 * Service Layer acts as a Proxy for the distributed Web Service.
 * Controllers call this class without knowing it fetches data from a REST API.
 */
public class UserService {

    public boolean login(String username, String password) {
        try {
            // DAO එක වෙනුවට Web Service එකට Request එකක් යැවීම
            return WebServiceClient.loginViaApi(username, password);
        } catch (Exception e) {
            System.err.println("API Error during login: " + e.getMessage());
            return false; // API එක වැඩ නැත්නම් False යවයි
        }
    }

    public boolean registerUser(String username, String password, String role) {
        try {
            // DAO එක වෙනුවට Web Service එකට Request එකක් යැවීම
            return WebServiceClient.registerViaApi(username, password, role);
        } catch (Exception e) {
            System.err.println("API Error during registration: " + e.getMessage());
            return false;
        }
    }
}