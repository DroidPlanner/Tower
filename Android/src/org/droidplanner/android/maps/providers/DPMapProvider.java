package org.droidplanner.android.maps.providers;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceCategory;

import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.providers.google_map.GoogleMapFragment;
import org.droidplanner.android.maps.providers.osm.OSMapFragment;

/**
 * Contains a listing of the various map providers supported, and implemented in DroidPlanner.
 */
public enum DPMapProvider {
    /**
     * Provide access to google map v2.
     * Requires the google play services.
     */
    GOOGLE_MAP {
        @Override
        public DPMap getMapFragment() {
            return new GoogleMapFragment();
        }

        @Override
        public Preference[] getMapPreferences(Context context) {
            final Preference testPreference = new Preference(context);
            testPreference.setTitle("Test Preference");
            return new Preference[]{testPreference};
        }
    },

    /**
     * Provides access to open street map.
     */
    OPEN_STREET_MAP {
        @Override
        public DPMap getMapFragment() {
            return new OSMapFragment();
        }

        @Override
        public Preference[] getMapPreferences(Context context) {
            return super.getMapPreferences(context);
        }
    };

    /**
     * @return the fragment implementing the map.
     */
    public abstract DPMap getMapFragment();

    /**
     * @return the set of preferences supported by the map.
     */
    public Preference[] getMapPreferences(Context context){
        return sEmptyPrefsSet;
    }

    /**
     * Returns the map type corresponding to the given map name.
     * @param mapName name of the map type
     * @return {@link DPMapProvider} object.
     */
    public static DPMapProvider getMapProvider(String mapName){
        try {
            return DPMapProvider.valueOf(mapName);
        }catch(IllegalArgumentException e){
            return null;
        }
    }

    /**
     * By default, Google Map is the map provider.
     */
    public static final DPMapProvider DEFAULT_MAP_PROVIDER = GOOGLE_MAP;

    /**
     * Used to avoid allocating new arrays of length 0 when an empty set of preferences is needed.
     */
    private static final Preference[] sEmptyPrefsSet = new Preference[0];
}
