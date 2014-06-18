package org.droidplanner.android.maps.providers.osm;

import android.os.Bundle;

import org.droidplanner.R;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.maps.providers.MapProviderPreferences;

/**
 * This is the Open Street Map provider preferences.
 * It stores, and handles all preferences related to the Open Street Map.
 */
public class OSMapProviderPreferences extends MapProviderPreferences {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_osm);
    }

    @Override
    public DPMapProvider getMapProvider() {
        return DPMapProvider.OPEN_STREET_MAP;
    }
}
