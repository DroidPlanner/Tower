package org.droidplanner.android.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.droidplanner.android.dialogs.GuidedDialog;
import org.droidplanner.android.dialogs.GuidedDialog.GuidedDialogListener;
import org.droidplanner.android.utils.DroneHelper;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.helpers.coordinates.Coord2D;

public class FlightMapFragment extends DroneMap implements
        DPMap.OnMapLongClickListener, DPMap.OnMarkerClickListener, DPMap.OnMarkerDragListener,
		GuidedDialogListener, OnDroneListener {

    private static final int MAX_TOASTS_FOR_LOCATION_PRESS = 3;

    private static final String PREF_USER_LOCATION_FIRST_PRESS = "pref_user_location_first_press";
    private static final int DEFAULT_USER_LOCATION_FIRST_PRESS = 0;

    private static final String PREF_DRONE_LOCATION_FIRST_PRESS = "pref_drone_location_first_press";
    private static final int DEFAULT_DRONE_LOCATION_FIRST_PRESS = 0;

    private DroidPlannerPrefs mAppPrefs;

	private boolean guidedModeOnLongPress;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);

        mAppPrefs = new DroidPlannerPrefs(context);

		mMapFragment.setOnMapLongClickListener(this);
        mMapFragment.setOnMarkerDragListener(this);
        mMapFragment.setOnMarkerClickListener(this);
		return view;
	}

    @Override
    public void onResume(){
        super.onResume();
        mMapFragment.selectAutoPanMode(mAppPrefs.getAutoPanMode());
        guidedModeOnLongPress = mAppPrefs.isGuidedModeOnLongPressEnabled();
    }

    @Override
    public void onPause(){
        super.onPause();
        mMapFragment.selectAutoPanMode(AutoPanMode.DISABLED);
    }

    @Override
    protected int getMaxFlightPathSize(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.valueOf(prefs.getString("pref_max_flight_path_size", "0"));
    }

    @Override
    public boolean setAutoPanMode(AutoPanMode target) {
        //Update the map panning preferences.
        mAppPrefs.setAutoPanMode(target);
        mMapFragment.selectAutoPanMode(target);
        return true;
    }

    @Override
	public void update() {
		super.update();
	}

	@Override
	public void onMapLongClick(Coord2D coord) {
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
		try {
			drone.guidedPoint.forcedGuidedCoordinate(DroneHelper
					.LatLngToCoord(coord));
		} catch (Exception e) {
			Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
		}
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
		switch (event) {
        case ARMING:
            // Clear the previous flight path when arming.
            if (drone.state.isArmed()) {
                mMapFragment.clearFlightPath();
            }
            break;

		}
		super.onDroneEvent(event,drone);
	}

    @Override
    protected boolean isMissionDraggable() {
        return false;
    }

    @Override
    public void goToMyLocation(){
        super.goToMyLocation();
        int pressCount = mAppPrefs.prefs.getInt(PREF_USER_LOCATION_FIRST_PRESS, DEFAULT_USER_LOCATION_FIRST_PRESS);
        if(pressCount < MAX_TOASTS_FOR_LOCATION_PRESS){
            Toast.makeText(context, "Long press to activate user auto panning.",
                    Toast.LENGTH_LONG).show();
            mAppPrefs.prefs.edit().putInt(PREF_USER_LOCATION_FIRST_PRESS, pressCount + 1).apply();
        }
    }

    @Override
    public void goToDroneLocation(){
        super.goToDroneLocation();
        final int pressCount = mAppPrefs.prefs.getInt(PREF_DRONE_LOCATION_FIRST_PRESS,
                DEFAULT_DRONE_LOCATION_FIRST_PRESS);
        if(pressCount < MAX_TOASTS_FOR_LOCATION_PRESS){
            Toast.makeText(context, "Long press to activate drone auto panning.",
                    Toast.LENGTH_LONG).show();
            mAppPrefs.prefs.edit().putInt(PREF_DRONE_LOCATION_FIRST_PRESS, pressCount + 1).apply();
        }
    }

}
