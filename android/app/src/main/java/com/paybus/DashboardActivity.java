package com.paybus;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.paybus.ui.history.HistoryFragment;
import com.paybus.ui.map.MapFragment;
import com.paybus.ui.profile.ProfileFragment;
import com.paybus.ui.reminder.ReminderFragment;
import com.paybus.ui.settings.SettingsFragment;
import com.paybus.utils.SessionManager;

public class DashboardActivity extends BaseActivity {

    private BottomNavigationView bottomNav;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        session = new SessionManager(this);
        if (session.getThemeMode() != null && session.getThemeMode().equals("dark")) {
            setTheme(R.style.Theme_PayBus_Dark);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                return true;
            } else if (id == R.id.nav_reminder) {
                loadFragment(new ReminderFragment());
                return true;
            } else if (id == R.id.nav_map) {
                loadFragment(new MapFragment());
                return true;
            } else if (id == R.id.nav_history) {
                loadFragment(new HistoryFragment());
                return true;
            } else if (id == R.id.nav_settings) {
                loadFragment(new SettingsFragment());
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_profile);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commitAllowingStateLoss();
    }
}
