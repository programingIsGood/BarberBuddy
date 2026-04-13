package com.example.barberbuddy;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SavedStylesManager {

    private static final String PREFS = "saved_styles";
    private static final String KEY   = "saved_ids";

    public static void save(Context ctx, int id) {
        Set<String> ids = getIds(ctx);
        ids.add(String.valueOf(id));
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putStringSet(KEY, ids).apply();
    }

    public static void remove(Context ctx, int id) {
        Set<String> ids = getIds(ctx);
        ids.remove(String.valueOf(id));
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putStringSet(KEY, ids).apply();
    }

    public static boolean isSaved(Context ctx, int id) {
        return getIds(ctx).contains(String.valueOf(id));
    }

    public static List<Hairstyle> getSavedStyles(Context ctx) {
        Set<String> ids = getIds(ctx);
        List<Hairstyle> result = new ArrayList<>();
        for (String id : ids) {
            Hairstyle h = HairstyleRepository.getById(Integer.parseInt(id));
            if (h != null) result.add(h);
        }
        return result;
    }

    private static Set<String> getIds(Context ctx) {
        Set<String> saved = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getStringSet(KEY, new HashSet<>());
        return new HashSet<>(saved); // mutable copy
    }
}
