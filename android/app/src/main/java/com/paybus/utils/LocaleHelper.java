package com.paybus.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LocaleHelper {

    public static Context setLocale(Context context, String langCode) {
        Locale locale;
        switch (langCode) {
            case "ru":
                locale = new Locale("ru");
                break;
            case "en":
                locale = new Locale("en");
                break;
            default:
                locale = new Locale("uz");
        }
        Locale.setDefault(locale);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }

    public static String getLanguage(Context context) {
        SessionManager session = new SessionManager(context);
        return session.getLanguage();
    }
}
