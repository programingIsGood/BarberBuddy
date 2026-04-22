package com.example.barberbuddy;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {

    private static final String NAME = "barberbuddy_prefs";
    private static final String KEY_ONBOARDED = "onboarded";
    private static final String KEY_CAMERA_ASKED  = "camera_permission_asked";

    public static boolean isOnboarded(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ONBOARDED, false);
    }

    public static void setOnboarded(Context context, boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ONBOARDED, value).apply();
    }

    public static boolean hasCameraPermissionBeenAsked(Context ctx) {
        return prefs(ctx).getBoolean(KEY_CAMERA_ASKED, false);
    }

    public static void setCameraPermissionAsked(Context ctx) {
        prefs(ctx).edit().putBoolean(KEY_CAMERA_ASKED, true).apply();
    }
    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

}