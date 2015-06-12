package org.droidplanner.android.maps.providers.google_map;

import org.droidplanner.android.R;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.maps.providers.MapProviderPreferences;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;

/**
 * This is the google map provider preferences. It stores and handles all
 * preferences related to google map.
 */
public class GoogleMapProviderPreferences extends MapProviderPreferences {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_google_maps);
		setupPreferences();
	}

	private void setupPreferences() {
		final Context context = getActivity().getApplicationContext();
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

		final String mapTypeKey = DroidPlannerPrefs.PREF_MAP_TYPE;
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
