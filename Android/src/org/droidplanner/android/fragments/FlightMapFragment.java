package org.droidplanner.android.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.droidplanner.android.dialogs.GuidedDialog;
import org.droidplanner.android.dialogs.GuidedDialog.GuidedDialogListener;
import org.droidplanner.android.graphic.DroneHelper;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.maps.fragments.DroneMap;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.helpers.coordinates.Coord2D;

public class FlightMapFragment extends DroneMap implements
        DPMap.OnMapLongClickListener, DPMap.OnMarkerClickListener, DPMap.OnMarkerDragListener,
		GuidedDialogListener, OnDroneListener {

	private static final int ZOOM_LEVEL = 20;

	public boolean isAutoPanEnabled;
	private boolean guidedModeOnLongPress;

	public boolean hasBeenZoomed = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);

		getPreferences();

		mMapFragment.setOnMapLongClickListener(this);
        mMapFragment.setOnMarkerDragListener(this);
        mMapFragment.setOnMarkerClickListener(this);
		return view;
	}

	private void getPreferences() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		isAutoPanEnabled = prefs.getBoolean("pref_auto_pan_enabled", false);
		guidedModeOnLongPress = prefs.getBoolean("pref_guided_mode_on_long_press", true);
	}

    @Override
    protected int getMaxFlightPathSize(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.valueOf(prefs.getString("pref_max_flight_path_size", "0"));
    }

	@Override
	public void update() {
		super.update();
	}

	private void animateCamera(LatLng coord) {
		if (!hasBeenZoomed) {
			hasBeenZoomed = true;
            mMapFragment.updateCamera(coord, ZOOM_LEVEL);
		}
		if (isAutoPanEnabled) {
            mMapFragment.updateCamera(coord, ZOOM_LEVEL);
		}
	}

	@Override
	public void onMapLongClick(Coord2D coord) {
		getPreferences();
		if (drone.MavClient.isConnected()) {
			if (drone.guidedPoint.isInitialized()) {
				drone.guidedPoint.newGuidedCoord(coord);
			} else {
				if (guidedModeOnLongPress) {
					GuidedDialog dialog = new GuidedDialog();
					dialog.setCoord(DroneHelper.CoordToLatLang(coord));
					dialog.setListener(this);
					dialog.show(getChildFragmentManager(), "GUIDED dialog");
				}
			}
		}
	}

	@Override
	public void onForcedGuidedPoint(LatLng coord) {
		drone.guidedPoint.forcedGuidedCoordinate(DroneHelper
				.LatLngToCoord(coord));
	}

	@Override
	public void onMarkerDragStart(MarkerInfo markerInfo) {
	}

	@Override
	public void onMarkerDrag(MarkerInfo markerInfo) {
	}

	@Override
	public void onMarkerDragEnd(MarkerInfo markerInfo) {
		drone.guidedPoint.newGuidedCoord(markerInfo.getPosition());
	}

	@Override
	public boolean onMarkerClick(MarkerInfo markerInfo) {
		drone.guidedPoint.newGuidedCoord(markerInfo.getPosition());
		return true;
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		LatLng position = DroneHelper.CoordToLatLang(drone.GPS.getPosition());
		switch (event) {
        case ARMING:
            // Clear the previous flight path when arming.
            if (drone.state.isArmed()) {
                mMapFragment.clearFlightPath();
            }
            break;
		case GPS:
			animateCameraIfNeeded(position);
			break;
		default:
			break;
		}
		super.onDroneEvent(event,drone);
	}

	private void animateCameraIfNeeded(LatLng coord) {
		if (!hasBeenZoomed) {
			hasBeenZoomed = true;
            mMapFragment.updateCamera(coord, ZOOM_LEVEL);
		}
		if (isAutoPanEnabled) {
            mMapFragment.updateCamera(coord, ZOOM_LEVEL);
		}
	}

    @Override
    protected boolean isMissionDraggable() {
        return false;
    }

}
