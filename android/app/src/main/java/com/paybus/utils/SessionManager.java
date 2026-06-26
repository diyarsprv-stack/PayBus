package com.paybus.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "PayBusSession";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_PHONE = "phone_number";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_THEME_MODE = "theme_mode";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void savePhoneNumber(String phone) {
        editor.putString(KEY_PHONE, phone).apply();
    }

    public String getPhoneNumber() {
        return prefs.getString(KEY_PHONE, null);
    }

    public void saveFullName(String name) {
        editor.putString(KEY_FULL_NAME, name).apply();
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, null);
    }

    public void saveLanguage(String lang) {
        editor.putString(KEY_LANGUAGE, lang).apply();
    }

    public String getLanguage() {
        return prefs.getString(KEY_LANGUAGE, "uz");
    }

    public void saveThemeMode(String mode) {
        editor.putString(KEY_THEME_MODE, mode).apply();
    }

    public String getThemeMode() {
        return prefs.getString(KEY_THEME_MODE, "light");
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void logout() {
        editor.clear().apply();
    }
}
