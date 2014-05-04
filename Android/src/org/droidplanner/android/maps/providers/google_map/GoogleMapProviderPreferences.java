package org.droidplanner.android.maps.providers.google_map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import org.droidplanner.R;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.maps.providers.MapProviderPreferences;

import static org.droidplanner.android.utils.Constants.*;

/**
 * This is the google map provider preferences.
 * It stores and handles all preferences related to google map.
 */
public class GoogleMapProviderPreferences extends MapProviderPreferences {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_google_map);
        setupPreferences();
    }

    private void setupPreferences() {
        final Context context = getActivity().getApplicationContext();
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        final String mapTypeKey = getString(R.string.pref_map_type_key);
        final Preference mapTypePref = findPreference(mapTypeKey);
        if (mapTypePref != null) {
            mapTypePref.setSummary(sharedPref.getString(mapTypeKey, ""));
            mapTypePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mapTypePref.setSummary(newValue.toString());
                    return true;
                }
            });
        }
    }

    @Override
    public DPMapProvider getMapProvider() {
        return DPMapProvider.GOOGLE_MAP;
    }
}
