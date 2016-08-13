package org.droidplanner.android.maps.providers.baidu_map;

import org.droidplanner.android.R;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.maps.providers.MapProviderPreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;

import com.baidu.mapapi.map.BaiduMap;

/**
 * This is the baidu map provider preferences. It stores and handles all
 * preferences related to baidu map.
 */
public class BaiduMapPrefFragment extends MapProviderPreferences {
	// Common defines
	private static final String PREF_MAP_TYPE = "pref_baidu_map_type";
	private static final String MAP_TYPE_SATELLITE = "satellite";
	private static final String MAP_TYPE_NORMAL = "normal";
	private static final String MAP_TYPE_NONE = "none";
	private static final String DEFAULT_MAP_TYPE = MAP_TYPE_SATELLITE;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_baidu_maps);
		setupPreferences();
	}

    @Override
    public DPMapProvider getMapProvider() {
        return DPMapProvider.BAIDU_MAP;
    }
    private void setupPreferences() {
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
		setupMapTypePreferences(sharedPref);
	}

	public int getMapType(Context context) {
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String mapType = sharedPref.getString(PREF_MAP_TYPE, DEFAULT_MAP_TYPE);

		if (mapType.equalsIgnoreCase(MAP_TYPE_SATELLITE)) {
			return BaiduMap.MAP_TYPE_SATELLITE;
		}
		else if (mapType.equalsIgnoreCase(MAP_TYPE_NORMAL)) {
			return BaiduMap.MAP_TYPE_NORMAL;
		}
		else if (mapType.equalsIgnoreCase(MAP_TYPE_NONE)) {
			return BaiduMap.MAP_TYPE_NONE;
		}
		else {
			return BaiduMap.MAP_TYPE_SATELLITE;
		}
	}

    private void setupMapTypePreferences(SharedPreferences sharedPref) {
		final String mapTypeKey = PREF_MAP_TYPE;
		final Preference mapTypePref = findPreference(mapTypeKey);
		if(mapTypePref != null) {
			final String mapType = sharedPref.getString(mapTypeKey, DEFAULT_MAP_TYPE);
			mapTypePref.setSummary(mapType);
			mapTypePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					mapTypePref.setSummary(newValue.toString());
					return true;
				}
			});
		}
    }
}
