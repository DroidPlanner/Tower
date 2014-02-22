package org.droidplanner.activities.helpers;

import android.app.Activity;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import java.util.Locale;

public class UiLanguage {
    private Activity activity;

    public UiLanguage(Activity activity) {
        this.activity = activity;
    }

    public void updateUiLanguage() {
        if (isUiLanguageEnglish()) {
            Configuration config = new Configuration();
            config.locale = Locale.ENGLISH;
            activity.getResources().updateConfiguration(config,
                    activity.getResources().getDisplayMetrics());
        }
    }

    private boolean isUiLanguageEnglish() {
        return PreferenceManager.getDefaultSharedPreferences(
                activity.getApplicationContext()).getBoolean("pref_ui_language_english", false);
    }
}
