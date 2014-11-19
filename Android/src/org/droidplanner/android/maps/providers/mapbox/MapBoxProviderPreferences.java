package org.droidplanner.android.maps.providers.mapbox;

import org.droidplanner.android.R;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.maps.providers.MapProviderPreferences;

import android.os.Bundle;

/**
 * Provides access to the mapbox map preferences.
 */
public class MapBoxProviderPreferences extends MapProviderPreferences {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_mapbox);
	}

	@Override
	public DPMapProvider getMapProvider() {
		return DPMapProvider.MAPBOX;
	}
}
