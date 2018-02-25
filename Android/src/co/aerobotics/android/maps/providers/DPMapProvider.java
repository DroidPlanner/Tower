package co.aerobotics.android.maps.providers;

import co.aerobotics.android.maps.DPMap;
import co.aerobotics.android.maps.GoogleMapFragment;
import co.aerobotics.android.maps.providers.baidu_map.BaiduMapFragment;
import co.aerobotics.android.maps.providers.baidu_map.BaiduMapPrefFragment;
import co.aerobotics.android.maps.providers.google_map.GoogleMapPrefFragment;

import co.aerobotics.android.maps.providers.google_map.GoogleMapPrefFragment;

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
			return new GoogleMapPrefFragment();
		}
	},

	BAIDU_MAP {
		@Override
		public DPMap getMapFragment() {	return new BaiduMapFragment(); }

		@Override
		public MapProviderPreferences getMapProviderPreferences() {
			return new BaiduMapPrefFragment();
		}
	};

	private static DPMapProvider[] ENABLED_PROVIDERS = {
		GOOGLE_MAP,
		BAIDU_MAP
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

	public static DPMapProvider[] getEnabledProviders(){
		return ENABLED_PROVIDERS;
	}

	/**
	 * By default, Google Map is the map provider.
	 */
	public static final DPMapProvider DEFAULT_MAP_PROVIDER = GOOGLE_MAP;
}
