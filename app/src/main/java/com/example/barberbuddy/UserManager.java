package com.example.barberbuddy;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Handles local user accounts stored in SharedPreferences.
 * Each account is stored as: "user_<email>" → hashed password (simple).
 * Logged-in session: "session_email" in barberbuddy_prefs.
 */
public class UserManager {

    private static final String PREFS_ACCOUNTS = "bb_accounts";
    private static final String PREFS_SESSION  = "barberbuddy_prefs";
    private static final String KEY_SESSION    = "session_email";
    private static final String KEY_NAME_PREFIX = "name_";
    private static final String PREFIX         = "user_";

    // ── Registration ──────────────────────────────────────────────

    /** Returns null on success, or an error message. */
    public static String register(Context ctx, String name, String email, String password) {
        if (name == null || name.trim().isEmpty())
            return "Name is required.";
        if (email == null || !email.contains("@"))
            return "Enter a valid email.";
        if (password == null || password.length() < 6)
            return "Password must be at least 6 characters.";

        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_ACCOUNTS, Context.MODE_PRIVATE);
        String key = PREFIX + email.toLowerCase().trim();

        if (prefs.contains(key))
            return "An account with this email already exists.";

        prefs.edit()
             .putString(key, hash(password))
             .putString(KEY_NAME_PREFIX + email.toLowerCase().trim(), name.trim())
             .apply();
        return null; // success
    }

    // ── Login ──────────────────────────────────────────────────────

    /** Returns null on success, or an error message. */
    public static String login(Context ctx, String email, String password) {
        if (email == null || email.trim().isEmpty())
            return "Enter your email.";
        if (password == null || password.isEmpty())
            return "Enter your password.";

        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_ACCOUNTS, Context.MODE_PRIVATE);
        String key = PREFIX + email.toLowerCase().trim();

        if (!prefs.contains(key))
            return "No account found with this email.";

        String stored = prefs.getString(key, "");
        if (!stored.equals(hash(password)))
            return "Incorrect password.";

        // Save session
        ctx.getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE)
           .edit().putString(KEY_SESSION, email.toLowerCase().trim()).apply();
        return null; // success
    }

    // ── Session helpers ────────────────────────────────────────────

    public static boolean isLoggedIn(Context ctx) {
        return getLoggedInEmail(ctx) != null;
    }

    public static String getLoggedInEmail(Context ctx) {
        String e = ctx.getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE)
                      .getString(KEY_SESSION, null);
        return (e != null && !e.isEmpty()) ? e : null;
    }

    public static String getLoggedInName(Context ctx) {
        String email = getLoggedInEmail(ctx);
        if (email == null) return "User";
        return ctx.getSharedPreferences(PREFS_ACCOUNTS, Context.MODE_PRIVATE)
                  .getString(KEY_NAME_PREFIX + email, "User");
    }

    public static void logout(Context ctx) {
        ctx.getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE)
           .edit().remove(KEY_SESSION).apply();
    }

    // ── Utility ────────────────────────────────────────────────────

    /** Very simple hash — sufficient for local offline storage. */
    private static String hash(String input) {
        int h = 31;
        for (char c : input.toCharArray()) h = h * 31 + c;
        return String.valueOf(h);
    }
}
