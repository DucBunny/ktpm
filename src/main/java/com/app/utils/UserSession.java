package com.app.utils;

public class UserSession {
    private static String email;
    private static String role;
    private static String username;
    private static String currentPage;

    public static void setUserInfo(String email, String role, String username) {
        UserSession.email = email;
        UserSession.role = role;
        UserSession.username = username;
    }

    public static String getEmail() {
        return email;
    }

    public static String getRole() {
        return role;
    }

    public static String getUsername() {
        return username;
    }

    public static String getCurrentPage() {
        return currentPage;
    }

    public static void setCurrentPage(String page) {
        currentPage = page;
    }

    public static void clearSession() {
        email = null;
        role = null;
        username = null;
    }
}
