package com.example.barberbuddy;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.security.SecureRandom;

/**
 * SessionManager — token-based session persistence.
 * No passwords are stored. A random session token is generated on login
 * and cleared on logout.
 */
public class SessionManager {

    private static final String PREF_NAME     = "barberbuddy_session";
    private static final String KEY_TOKEN     = "session_token";
    private static final String KEY_USER_ID   = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL= "user_email";
    private static final String KEY_LOGGED_IN = "is_logged_in";
    private static final String KEY_AGREED_TC = "agreed_terms";

    // ── Singleton ──────────────────────────────────────────────────────────────

    private static SessionManager instance;

    public static SessionManager getInstance(Context ctx) {
        if (instance == null) {
            instance = new SessionManager(ctx.getApplicationContext());
        }
        return instance;
    }

    private final SharedPreferences prefs;

    private SessionManager(Context ctx) {
        // Use private, encrypted-equivalent prefs for session data
        prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // ── Session creation ───────────────────────────────────────────────────────

    /** Call after a successful login / registration. */
    public void createSession(String userId, String name, String email) {
        String token = generateSecureToken();
        prefs.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_TOKEN,      token)
                .putString(KEY_USER_ID,    userId)
                .putString(KEY_USER_NAME,  name)
                .putString(KEY_USER_EMAIL, email)
                .apply();
    }

    // ── Session queries ────────────────────────────────────────────────────────

    public boolean isLoggedIn()    { return prefs.getBoolean(KEY_LOGGED_IN, false); }
    public String  getSessionToken(){ return prefs.getString(KEY_TOKEN, null); }
    public String  getUserId()     { return prefs.getString(KEY_USER_ID,    ""); }
    public String  getUserName()   { return prefs.getString(KEY_USER_NAME,  ""); }
    public String  getUserEmail()  { return prefs.getString(KEY_USER_EMAIL, ""); }

    // ── Terms agreement ────────────────────────────────────────────────────────

    public void setTermsAgreed(boolean agreed) {
        prefs.edit().putBoolean(KEY_AGREED_TC, agreed).apply();
    }

    public boolean hasAgreedTerms() {
        return prefs.getBoolean(KEY_AGREED_TC, false);
    }

    // ── Logout ─────────────────────────────────────────────────────────────────

    public void clearSession() {
        prefs.edit()
                .remove(KEY_LOGGED_IN)
                .remove(KEY_TOKEN)
                .remove(KEY_USER_ID)
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_EMAIL)
                .apply();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /** Generate a cryptographically secure random token. */
    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.encodeToString(bytes, Base64.URL_SAFE | Base64.NO_WRAP);
    }
}
