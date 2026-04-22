package com.example.barberbuddy;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Patterns;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * UserManager — local auth logic.
 *
 * Passwords are NEVER stored in plain text. Each password is hashed with
 * SHA-256 + a per-user random salt before it is persisted.
 *
 * For production, replace the SharedPreferences store with a call to your
 * backend (AWS Cognito, Firebase Auth, etc.).
 */
public class UserManager {

    // Minimum password rules
    private static final int  MIN_PASSWORD_LEN   = 8;
    private static final Pattern HAS_UPPERCASE   = Pattern.compile("[A-Z]");
    private static final Pattern HAS_DIGIT       = Pattern.compile("[0-9]");
    private static final Pattern HAS_SPECIAL     = Pattern.compile("[!@#\\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    private static final String USER_PREFS = "barberbuddy_users";

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Register a new user.
     * @return null on success, or an error message string on failure.
     */
    public static String register(Context ctx, String name, String email, String password) {
        // --- Basic field validation ---
        if (name.isEmpty())  return "Name is required.";
        if (name.length() < 2) return "Name must be at least 2 characters.";

        String emailError = validateEmail(email);
        if (emailError != null) return emailError;

        String passError = validatePassword(password);
        if (passError != null) return passError;

        SharedPreferences prefs = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);

        // --- Check for existing account ---
        if (prefs.contains(key(email, "salt"))) {
            return "An account with this email already exists.";
        }

        // --- Hash password with a fresh salt ---
        String salt   = generateSalt();
        String hashed = hashPassword(password, salt);

        if (hashed == null) return "Registration failed. Please try again.";

        // --- Persist user ---
        String userId = UUID.randomUUID().toString();
        prefs.edit()
                .putString(key(email, "id"),       userId)
                .putString(key(email, "name"),     name)
                .putString(key(email, "salt"),     salt)
                .putString(key(email, "password"), hashed)
                .apply();

        return null; // success
    }

    /**
     * Log in an existing user.
     * @return null on success, or an error message string on failure.
     */
    public static String login(Context ctx, String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            return "Please enter your email and password.";
        }

        String emailError = validateEmail(email);
        if (emailError != null) return emailError;

        SharedPreferences prefs = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);

        String salt   = prefs.getString(key(email, "salt"),     null);
        String stored = prefs.getString(key(email, "password"), null);

        if (salt == null || stored == null) {
            // No account found — same message to avoid account enumeration
            return "Invalid email or password.";
        }

        String hashed = hashPassword(password, salt);
        if (hashed == null || !hashed.equals(stored)) {
            return "Invalid email or password.";
        }

        // --- Create session ---
        String userId = prefs.getString(key(email, "id"),   UUID.randomUUID().toString());
        String name   = prefs.getString(key(email, "name"), "");
        SessionManager.getInstance(ctx).createSession(userId, name, email);

        return null; // success
    }

    // ── Validation helpers ─────────────────────────────────────────────────────

    public static String validateEmail(String email) {
        if (email.isEmpty()) return "Email is required.";
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Please enter a valid email address.";
        }
        return null;
    }

    public static String validatePassword(String password) {
        if (password.isEmpty()) return "Password is required.";
        if (password.length() < MIN_PASSWORD_LEN) {
            return "Password must be at least " + MIN_PASSWORD_LEN + " characters.";
        }
        if (!HAS_UPPERCASE.matcher(password).find()) {
            return "Password must contain at least one uppercase letter.";
        }
        if (!HAS_DIGIT.matcher(password).find()) {
            return "Password must contain at least one number.";
        }
        if (!HAS_SPECIAL.matcher(password).find()) {
            return "Password must contain at least one special character.";
        }
        return null;
    }

    /**
     * Calculates password strength: 0 (weak) – 4 (strong).
     */
    public static int passwordStrength(String password) {
        if (password == null || password.isEmpty()) return 0;
        int score = 0;
        if (password.length() >= MIN_PASSWORD_LEN)           score++;
        if (HAS_UPPERCASE.matcher(password).find())          score++;
        if (HAS_DIGIT.matcher(password).find())              score++;
        if (HAS_SPECIAL.matcher(password).find())            score++;
        return score;
    }

    // ── Crypto ────────────────────────────────────────────────────────────────

    private static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return null;
        }
    }

    // ── Key helper ─────────────────────────────────────────────────────────────

    /** Namespaced prefs key so each user gets isolated storage. */
    private static String key(String email, String field) {
        return email.toLowerCase().trim() + "." + field;
    }
}
