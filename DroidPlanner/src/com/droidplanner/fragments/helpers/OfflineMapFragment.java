package com.droidplanner.fragments.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.droidplanner.DroidPlannerApp;
import com.droidplanner.drone.DroneInterfaces;
import com.droidplanner.helpers.LocalMapTileProvider;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

import java.util.List;

public class OfflineMapFragment extends MapFragment
		implements DroneInterfaces.MapConfigListener {

	public static final String PREF_MAP_TYPE = "pref_map_type";

	public static final String MAP_TYPE_SATELLITE = "Satellite";
	public static final String MAP_TYPE_HYBRID = "Hybrid";
	public static final String MAP_TYPE_NORMAL = "Normal";
	public static final String MAP_TYPE_TERRAIN = "Terrain";

    /**
     * Map interface.
     */
	private GoogleMap mMap;

    /**
     * Map left padding.
     * @since 1.2.0
     */
    private int mMapLeftPadding;

    /**
     * Map right padding.
     * @since 1.2.0
     */
    private int mMapRightPadding;

    /**
     * Map top padding.
     * @since 1.2.0
     */
    private int mMapTopPadding;

    /**
     * Map bottom padding.
     * @since 1.2.0
     */
    private int mMapBottomPadding;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);

		((DroidPlannerApp) getActivity().getApplication()).drone.setMapConfigListener(this);

		setupMap();
		return view;
	}

	private void setupMap() {
		mMap = getMap();
		if (isMapLayoutFinished()) { // TODO it should wait for the map layout
										// before setting it up, instead of just
										// skipping the setup
			setupMapUI();
			setupMapOverlay();
		}
	}

	private void setupMapUI() {
		mMap.setMyLocationEnabled(true);
		UiSettings mUiSettings = mMap.getUiSettings();
		mUiSettings.setMyLocationButtonEnabled(true);
		mUiSettings.setCompassEnabled(true);
		mUiSettings.setTiltGesturesEnabled(false);
	}

	private void setupMapOverlay() {
		if (isOfflineMapEnabled()) {
			setupOfflineMapOverlay();
		} else {
			setupOnlineMapOverlay();
		}
	}

	private boolean isOfflineMapEnabled() {
		Context context = this.getActivity();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs.getBoolean("pref_advanced_use_offline_maps", false);
	}

	private void setupOnlineMapOverlay() {
		mMap.setMapType(getMapType());
	}

	private int getMapType() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		String mapType = prefs.getString(PREF_MAP_TYPE, "");

		if (mapType.equalsIgnoreCase(MAP_TYPE_SATELLITE)) {
			return GoogleMap.MAP_TYPE_SATELLITE;
		}
		if (mapType.equalsIgnoreCase(MAP_TYPE_HYBRID)) {
			return GoogleMap.MAP_TYPE_HYBRID;
		}
		if (mapType.equalsIgnoreCase(MAP_TYPE_NORMAL)) {
			return GoogleMap.MAP_TYPE_NORMAL;
		}
		if (mapType.equalsIgnoreCase(MAP_TYPE_TERRAIN)) {
			return GoogleMap.MAP_TYPE_TERRAIN;
		} else {
			return GoogleMap.MAP_TYPE_SATELLITE;
		}
	}

	private void setupOfflineMapOverlay() {
		mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
		TileOverlay tileOverlay = mMap.addTileOverlay(new TileOverlayOptions()
				                                              .tileProvider(new LocalMapTileProvider()));
		tileOverlay.setZIndex(-1);
		tileOverlay.clearTileCache();
	}

	public void zoomToExtents(List<LatLng> pointsList) {
		if (!pointsList.isEmpty()) {
			LatLngBounds bounds = getBounds(pointsList);
			CameraUpdate animation;
			if (isMapLayoutFinished())
				animation = CameraUpdateFactory.newLatLngBounds(bounds, 100);
			else
				animation = CameraUpdateFactory.newLatLngBounds(bounds, 480,
						360, 100);
			getMap().animateCamera(animation);
		}
	}

	protected void clearMap() {
		GoogleMap mMap = getMap();
		mMap.clear();
		setupMapOverlay();
	}

	private LatLngBounds getBounds(List<LatLng> pointsList) {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for (LatLng point : pointsList) {
			builder.include(point);
		}
		return builder.build();
	}

	public double getMapRotation() {
		if (isMapLayoutFinished()) {
			return mMap.getCameraPosition().bearing;
		} else {
			return 0;
		}
	}

	private boolean isMapLayoutFinished() {
		return getMap() != null;
	}

	@Override
	public void onMapTypeChanged() {
		setupMap();
	}

    /**
     * Updates the map left padding.
     * @param leftPadding map left padding
     * @since 1.2.0
     */
    public void setLeftPadding(int leftPadding){
        if(mMap == null)
            return;

        mMapLeftPadding = leftPadding;
        mMap.setPadding(mMapLeftPadding, mMapTopPadding, mMapRightPadding, mMapBottomPadding);
    }

    /**
     * Updates the map top padding.
     * @param topPadding map top padding
     * @since 1.2.0
     */
    public void setTopPadding(int topPadding){
        if(mMap == null)
            return;

        mMapTopPadding = topPadding;
        mMap.setPadding(mMapLeftPadding, mMapTopPadding, mMapRightPadding, mMapBottomPadding);
    }

    /**
     * Updates the map right padding.
     * @param rightPadding map right padding
     * @since 1.2.0
     */
    public void setRightPadding(int rightPadding){
        if(mMap == null)
            return;

        mMapRightPadding = rightPadding;
        mMap.setPadding(mMapLeftPadding, mMapTopPadding, mMapRightPadding, mMapBottomPadding);
    }

    /**
     * Updates the map bottom padding.
     * @param bottomPadding map bottom padding
     * @since 1.2.0
     */
    public void setBottomPadding(int bottomPadding){
        if(mMap == null)
            return;

        mMapBottomPadding = bottomPadding;
        mMap.setPadding(mMapLeftPadding, mMapTopPadding, mMapRightPadding, mMapBottomPadding);
    }
}
