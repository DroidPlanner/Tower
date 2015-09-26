package org.droidplanner.android.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.GuidedState;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.model.AbstractCommandListener;

import org.droidplanner.android.R;
import org.droidplanner.android.dialogs.GuidedDialog;
import org.droidplanner.android.dialogs.GuidedDialog.GuidedDialogListener;
import org.droidplanner.android.graphic.map.GraphicHome;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.utils.DroneHelper;
import org.droidplanner.android.utils.prefs.AutoPanMode;

import timber.log.Timber;

public class FlightMapFragment extends DroneMap implements DPMap.OnMapLongClickListener,
        DPMap.OnMarkerClickListener, DPMap.OnMarkerDragListener, GuidedDialogListener {

    public interface OnGuidedClickListener {
        void onGuidedClick(LatLong coord);
    }

    private static final int MAX_TOASTS_FOR_LOCATION_PRESS = 3;

    private static final String PREF_USER_LOCATION_FIRST_PRESS = "pref_user_location_first_press";
    private static final int DEFAULT_USER_LOCATION_FIRST_PRESS = 0;

    private static final String PREF_DRONE_LOCATION_FIRST_PRESS = "pref_drone_location_first_press";
    private static final int DEFAULT_DRONE_LOCATION_FIRST_PRESS = 0;

    /**
     * The map should zoom on the user location the first time it's acquired. This flag helps
     * enable the behavior.
     */
    private static boolean didZoomOnUserLocation = false;


    private static final IntentFilter eventFilter = new IntentFilter(AttributeEvent.STATE_ARMING);

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (AttributeEvent.STATE_ARMING.equals(action)) {
                final State droneState = drone.getAttribute(AttributeType.STATE);
                if (droneState.isArmed()) {
                    mMapFragment.clearFlightPath();
                }
            }
        }
    };

    private OnGuidedClickListener guidedClickListener;

    public void setGuidedClickListener(OnGuidedClickListener guidedClickListener) {
        this.guidedClickListener = guidedClickListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        View view = super.onCreateView(inflater, viewGroup, bundle);

        mMapFragment.setOnMapLongClickListener(this);
        mMapFragment.setOnMarkerDragListener(this);
        mMapFragment.setOnMarkerClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapFragment.selectAutoPanMode(mAppPrefs.getAutoPanMode());

        if (!didZoomOnUserLocation) {
            super.goToMyLocation();
            didZoomOnUserLocation = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapFragment.selectAutoPanMode(AutoPanMode.DISABLED);
    }

    @Override
    protected int getMaxFlightPathSize() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.valueOf(prefs.getString("pref_max_flight_path_size", "0"));
    }

    @Override
    public boolean setAutoPanMode(AutoPanMode target) {
        // Update the map panning preferences.
        if (mAppPrefs != null)
            mAppPrefs.setAutoPanMode(target);

        if (mMapFragment != null)
            mMapFragment.selectAutoPanMode(target);
        return true;
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onApiDisconnected() {
        super.onApiDisconnected();
        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

    @Override
    public void onMapLongClick(LatLong coord) {
        if (drone != null && drone.isConnected()) {
            final GuidedState guidedState = drone.getAttribute(AttributeType.GUIDED_STATE);
            if (guidedState.isInitialized()) {
                if(guidedClickListener != null)
                    guidedClickListener.onGuidedClick(coord);
            } else {
                GuidedDialog dialog = new GuidedDialog();
                dialog.setCoord(DroneHelper.CoordToLatLang(coord));
                dialog.setListener(this);
                dialog.show(getChildFragmentManager(), "GUIDED dialog");
            }
        }
    }

    @Override
    public void onForcedGuidedPoint(LatLng coord) {
        try {
            drone.sendGuidedPoint(DroneHelper.LatLngToCoord(coord), true);
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
        if (!(markerInfo instanceof GraphicHome)) {
            drone.sendGuidedPoint(markerInfo.getPosition(), false);
        }
    }

    @Override
    public boolean onMarkerClick(MarkerInfo markerInfo) {
        if(markerInfo == null)
            return false;
        drone.sendGuidedPoint(markerInfo.getPosition(), false);
        return true;
    }

    @Override
    protected boolean isMissionDraggable() {
        return false;
    }

    @Override
    public void goToMyLocation() {
        super.goToMyLocation();
        int pressCount = mAppPrefs.prefs.getInt(PREF_USER_LOCATION_FIRST_PRESS,
                DEFAULT_USER_LOCATION_FIRST_PRESS);
        if (pressCount < MAX_TOASTS_FOR_LOCATION_PRESS) {
            Toast.makeText(context, R.string.user_autopan_long_press, Toast.LENGTH_LONG).show();
            mAppPrefs.prefs.edit().putInt(PREF_USER_LOCATION_FIRST_PRESS, pressCount + 1).apply();
        }
    }

    @Override
    public void goToDroneLocation() {
        super.goToDroneLocation();

        if(this.drone == null)
            return;

        final Gps droneGps = this.drone.getAttribute(AttributeType.GPS);
        if (droneGps == null || !droneGps.isValid())
            return;

        final int pressCount = mAppPrefs.prefs.getInt(PREF_DRONE_LOCATION_FIRST_PRESS,
                DEFAULT_DRONE_LOCATION_FIRST_PRESS);
        if (pressCount < MAX_TOASTS_FOR_LOCATION_PRESS) {
            Toast.makeText(context, R.string.drone_autopan_long_press, Toast.LENGTH_LONG).show();
            mAppPrefs.prefs.edit().putInt(PREF_DRONE_LOCATION_FIRST_PRESS, pressCount + 1).apply();
        }
    }

}
