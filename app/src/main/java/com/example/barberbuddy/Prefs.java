package com.example.barberbuddy;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {

    private static final String NAME = "barberbuddy_prefs";
    private static final String KEY_ONBOARDED = "onboarded";

    public static boolean isOnboarded(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ONBOARDED, false);
    }

    public static void setOnboarded(Context context, boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ONBOARDED, value).apply();
    }
}