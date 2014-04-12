package org.droidplanner.android.maps.types;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.types.google_map.GoogleMapFragment;
import org.droidplanner.android.maps.types.osm.OSMapFragment;

/**
 * Contains a listing of the various map types supported, and implemented in DroidPlanner.
 */
public enum DPMapType {
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
        public PreferenceCategory getMapPreferences(Context context) {
            return super.getMapPreferences(context);
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
        public PreferenceCategory getMapPreferences(Context context) {
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
    public PreferenceCategory getMapPreferences(Context context){
        final PreferenceCategory prefCategory = new PreferenceCategory(context);
        final String title = name().toLowerCase().replace('_', ' ') + " preferences";
        prefCategory.setTitle(title);
        return prefCategory;
    }

    /**
     * Returns the map type corresponding to the given map name.
     * @param mapName name of the map type
     * @return {@link DPMapType} object.
     */
    public static DPMapType getMapType(String mapName){
        return DPMapType.valueOf(mapName);
    }
}
