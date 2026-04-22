package com.example.smarttracker.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "smarttracker";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String token, String name, String email, long userId) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .putLong(KEY_USER_ID, userId)
                .apply();
    }

    public String getToken() { return prefs.getString(KEY_TOKEN, null); }
    public String getUserName() { return prefs.getString(KEY_USER_NAME, "User"); }
    public String getUserEmail() { return prefs.getString(KEY_USER_EMAIL, ""); }
    public long getUserId() { return prefs.getLong(KEY_USER_ID, -1); }

    public boolean isLoggedIn() { return getToken() != null; }

    public void logout() { prefs.edit().clear().apply(); }
}
