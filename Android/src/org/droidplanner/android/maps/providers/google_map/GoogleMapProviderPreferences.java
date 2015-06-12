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

import com.google.android.gms.maps.GoogleMap;

/**
 * This is the google map provider preferences. It stores and handles all
 * preferences related to google map.
 */
public class GoogleMapProviderPreferences extends MapProviderPreferences {

	private static final String MAP_TYPE_SATELLITE = "Satellite";
	private static final String MAP_TYPE_HYBRID = "Hybrid";
	private static final String MAP_TYPE_NORMAL = "Normal";
	private static final String MAP_TYPE_TERRAIN = "Terrain";

	private static final String PREF_GOOGLE_TILE_PROVIDER_SETTINGS = "pref_google_tile_provider_settings";

	private static final String PREF_MAP_TYPE = "pref_map_type";
	private static final String DEFAULT_MAP_TYPE = MAP_TYPE_SATELLITE;

	private static final String PREF_MAPBOX_TILE_PROVIDER_SETTINGS = "pref_mapbox_tile_provider_settings";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_google_maps);
		setupPreferences();
	}

	private void setupPreferences() {
		final Context context = getActivity().getApplicationContext();
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

		final String mapTypeKey = PREF_MAP_TYPE;
		final Preference mapTypePref = findPreference(mapTypeKey);
		if (mapTypePref != null) {
			mapTypePref.setSummary(sharedPref.getString(mapTypeKey, DEFAULT_MAP_TYPE));
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

	public static int getMapType(Context context){
		if(context == null)
			return GoogleMap.MAP_TYPE_SATELLITE;

		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		final String mapType = sharedPref.getString(PREF_MAP_TYPE, DEFAULT_MAP_TYPE);
		switch(mapType){
			case MAP_TYPE_HYBRID:
				return GoogleMap.MAP_TYPE_HYBRID;

			case MAP_TYPE_NORMAL:
				return GoogleMap.MAP_TYPE_NORMAL;

			case MAP_TYPE_TERRAIN:
				return GoogleMap.MAP_TYPE_TERRAIN;

			case MAP_TYPE_SATELLITE:
			default:
				return GoogleMap.MAP_TYPE_SATELLITE;
		}
	}
}
