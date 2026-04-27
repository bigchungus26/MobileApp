package com.example.smarttracker.util;

import android.content.Context;
import android.content.SharedPreferences;

//small helper that stores the logged-in user info using SharedPreferences
public class SessionManager {

    //file name and keys we store under
    private static final String PREF_NAME = "smarttracker";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";

    private final SharedPreferences prefs;

    //open the SharedPreferences file in private mode
    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    //write the user info after a successful login or register
    public void saveSession(int userId, String name, String email) {
        prefs.edit()
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .apply();
    }

    //getters return -1 or empty defaults when nothing was saved
    public int getUserId() { return prefs.getInt(KEY_USER_ID, -1); }
    public String getUserName() { return prefs.getString(KEY_USER_NAME, "User"); }
    public String getUserEmail() { return prefs.getString(KEY_USER_EMAIL, ""); }

    //a saved user id means the user is logged in
    public boolean isLoggedIn() { return getUserId() != -1; }

    //wipe the saved values when logging out
    public void logout() { prefs.edit().clear().apply(); }
}
