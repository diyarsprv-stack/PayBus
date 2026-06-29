package com.paybus;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.paybus.utils.LocaleHelper;
import com.paybus.utils.SessionManager;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        SessionManager session = new SessionManager(newBase);
        String lang = session.getLanguage();
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang));
    }
}
