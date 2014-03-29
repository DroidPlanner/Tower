package org.droidplanner.android.fragments;

import org.droidplanner.android.dialogs.GuidedDialog;
import org.droidplanner.android.dialogs.GuidedDialog.GuidedDialogListener;
import org.droidplanner.android.fragments.helpers.DroneMap;
import org.droidplanner.android.graphic.DroneHelper;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class FlightMapFragment extends DroneMap implements
		OnMapLongClickListener, OnMarkerClickListener, OnMarkerDragListener,
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

		mMap.setOnMapLongClickListener(this);
		mMap.setOnMarkerDragListener(this);
		mMap.setOnMarkerClickListener(this);
		return view;
	}

	private void getPreferences() {
		Context context = this.getActivity();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		maxFlightPathSize = Integer.valueOf(prefs.getString(
				"pref_max_flight_path_size", "0"));
		isAutoPanEnabled = prefs.getBoolean("pref_auto_pan_enabled", false);
		guidedModeOnLongPress = prefs.getBoolean("pref_guided_mode_on_long_press", true);
	}

	@Override
	public void update() {
		super.update();
	}



	private void animateCamera(LatLng coord) {
		if (!hasBeenZoomed) {
			hasBeenZoomed = true;
			mMap.animateCamera(CameraUpdateFactory
					.newLatLngZoom(coord, ZOOM_LEVEL));
		}
		if (isAutoPanEnabled) {
			mMap.animateCamera(CameraUpdateFactory
					.newLatLngZoom(coord, ZOOM_LEVEL));
		}
	}

	@Override
	public void onMapLongClick(LatLng coord) {
		getPreferences();
		if (drone.MavClient.isConnected()) {
			if (drone.guidedPoint.isInitialized()) {
				drone.guidedPoint.newGuidedCoord(DroneHelper
						.LatLngToCoord(coord));
			} else {
				if (guidedModeOnLongPress) {
					GuidedDialog dialog = new GuidedDialog();
					dialog.setCoord(coord);
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
	public void onMarkerDragStart(Marker marker) {
	}

	@Override
	public void onMarkerDrag(Marker marker) {
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		drone.guidedPoint.newGuidedCoord(DroneHelper.LatLngToCoord(marker
				.getPosition()));
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		drone.guidedPoint.newGuidedCoord(DroneHelper.LatLngToCoord(marker
				.getPosition()));
		return true;
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		LatLng position = DroneHelper.CoordToLatLang(drone.GPS.getPosition());
		switch (event) {
        case ARMING:
            // Clear the previous flight path when arming.
            if (drone.state.isArmed()) {
                clearFlightPath();
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
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coord,
					ZOOM_LEVEL));
		}
		if (isAutoPanEnabled) {
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coord,
					ZOOM_LEVEL));
		}
	}

    @Override
    protected boolean isMissionDraggable() {
        return false;
    }

}
