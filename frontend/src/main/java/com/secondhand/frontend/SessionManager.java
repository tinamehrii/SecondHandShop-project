package com.secondhand.frontend;

import com.google.gson.JsonObject;

/**
 * Keeps the JWT token and the logged-in user in memory
 * while the application is running.
 */
public class SessionManager {

    private static String token;
    private static JsonObject currentUser;

    private SessionManager() {
    }

    public static void login(String newToken, JsonObject user) {
        token = newToken;
        currentUser = user;
    }

    public static void logout() {
        token = null;
        currentUser = null;
    }

    public static String getToken() {
        return token;
    }

    public static JsonObject getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return token != null;
    }

    public static Long getUserId() {
        return currentUser == null ? null : currentUser.get("id").getAsLong();
    }

    public static boolean isAdmin() {
        return currentUser != null
                && currentUser.has("role")
                && currentUser.get("role").getAsString().equals("ADMIN");
    }
}
