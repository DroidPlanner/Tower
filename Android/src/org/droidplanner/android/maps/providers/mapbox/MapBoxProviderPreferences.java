package org.droidplanner.android.maps.providers.mapbox;

import android.os.Bundle;

import org.droidplanner.R;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.maps.providers.MapProviderPreferences;

/**
 * Provides access to the mapbox map preferences.
 */
public class MapBoxProviderPreferences extends MapProviderPreferences {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_mapbox);
    }

    @Override
    public DPMapProvider getMapProvider() {
        return DPMapProvider.MAPBOX;
    }
}
