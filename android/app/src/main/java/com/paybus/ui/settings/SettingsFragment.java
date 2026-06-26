package com.paybus.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.paybus.R;
import com.paybus.utils.SessionManager;

public class SettingsFragment extends Fragment {

    private SwitchMaterial themeSwitch;
    private RadioGroup languageGroup;
    private SessionManager session;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        session = new SessionManager(requireContext());

        themeSwitch = view.findViewById(R.id.themeSwitch);
        languageGroup = view.findViewById(R.id.languageGroup);

        String currentTheme = session.getThemeMode();
        themeSwitch.setChecked("dark".equals(currentTheme));

        String currentLang = session.getLanguage();
        if (currentLang != null) {
            switch (currentLang) {
                case "uz":
                    view.findViewById(R.id.radioUz).performClick();
                    break;
                case "ru":
                    view.findViewById(R.id.radioRu).performClick();
                    break;
                case "en":
                    view.findViewById(R.id.radioEn).performClick();
                    break;
                default:
                    view.findViewById(R.id.radioUz).performClick();
                    break;
            }
        } else {
            view.findViewById(R.id.radioUz).performClick();
        }

        RadioButton radioUz = view.findViewById(R.id.radioUz);
        RadioButton radioRu = view.findViewById(R.id.radioRu);
        RadioButton radioEn = view.findViewById(R.id.radioEn);

        radioUz.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) session.saveLanguage("uz");
        });
        radioRu.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) session.saveLanguage("ru");
        });
        radioEn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) session.saveLanguage("en");
        });

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                session.saveThemeMode("dark");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                session.saveThemeMode("light");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            requireActivity().recreate();
        });

        return view;
    }
}
