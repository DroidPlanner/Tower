package org.droidplanner.android.maps.providers;

import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.providers.google_map.GoogleMapFragment;
import org.droidplanner.android.maps.providers.google_map.GoogleMapProviderPreferences;
import org.droidplanner.android.maps.providers.mapbox.MapBoxFragment;
import org.droidplanner.android.maps.providers.mapbox.MapBoxProviderPreferences;

/**
 * Contains a listing of the various map providers supported, and implemented in
 * DroidPlanner.
 */
public enum DPMapProvider {
	/**
	 * Provide access to google map v2. Requires the google play services.
	 */
	GOOGLE_MAP {
		@Override
		public DPMap getMapFragment() {
			return new GoogleMapFragment();
		}

		@Override
		public MapProviderPreferences getMapProviderPreferences() {
			return new GoogleMapProviderPreferences();
		}
	},

    /**
     * Provides access to mapbox map.
     */
    MAPBOX{
        @Override
    public DPMap getMapFragment(){
            return new MapBoxFragment();
        }

        @Override
    public MapProviderPreferences getMapProviderPreferences(){
            return new MapBoxProviderPreferences();
        }
    };

	/**
	 * @return the fragment implementing the map.
	 */
	public abstract DPMap getMapFragment();

	/**
	 * @return the set of preferences supported by the map.
	 */
	public abstract MapProviderPreferences getMapProviderPreferences();

	/**
	 * Returns the map type corresponding to the given map name.
	 * 
	 * @param mapName
	 *            name of the map type
	 * @return {@link DPMapProvider} object.
	 */
	public static DPMapProvider getMapProvider(String mapName) {
		if (mapName == null) {
			return null;
		}

		try {
			return DPMapProvider.valueOf(mapName);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * By default, Google Map is the map provider.
	 */
	public static final DPMapProvider DEFAULT_MAP_PROVIDER = GOOGLE_MAP;
}
