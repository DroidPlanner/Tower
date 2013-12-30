package com.droidplanner.activitys;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperUI;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.droidplanner.drone.DroneInterfaces.OnDroneListner;
import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.drone.variables.mission.waypoints.SpatialCoordItem;
import com.droidplanner.fragments.FlightActionsFragment;
import com.droidplanner.fragments.FlightActionsFragment.OnMissionControlInteraction;
import com.droidplanner.fragments.FlightMapFragment;
import com.droidplanner.fragments.RCFragment;
import com.droidplanner.fragments.TelemetryFragment;
import com.droidplanner.fragments.helpers.DroneMap;
import com.droidplanner.fragments.helpers.OnMapInteractionListener;
import com.droidplanner.fragments.mode.ModeAcroFragment;
import com.droidplanner.fragments.mode.ModeAltholdFragment;
import com.droidplanner.fragments.mode.ModeAutoFragment;
import com.droidplanner.fragments.mode.ModeCircleFragment;
import com.droidplanner.fragments.mode.ModeDriftFragment;
import com.droidplanner.fragments.mode.ModeGuidedFragment;
import com.droidplanner.fragments.mode.ModeLandFragment;
import com.droidplanner.fragments.mode.ModeLoiterFragment;
import com.droidplanner.fragments.mode.ModePositionFragment;
import com.droidplanner.fragments.mode.ModeRTLFragment;
import com.droidplanner.fragments.mode.ModeStabilizeFragment;
import com.droidplanner.polygon.PolygonPoint;
import com.google.android.gms.maps.model.LatLng;

public class FlightActivity extends SuperUI implements
		OnMapInteractionListener, OnMissionControlInteraction, OnDroneListner{
	private static FragmentManager fragmentManager;
	private RCFragment rcFragment;
	private View failsafeTextView;
	private Fragment modeInfoPanel;
	private Fragment mapFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight);
		fragmentManager = getFragmentManager();
		modeInfoPanel = fragmentManager.findFragmentById(R.id.modeInfoPanel);
		failsafeTextView = findViewById(R.id.failsafeTextView);

        //Load the activity fragments
        Fragment modeRtl = fragmentManager.findFragmentById(R.id.modeInfoPanel);
        if(modeRtl == null){
            modeRtl = new ModeRTLFragment();
            fragmentManager.beginTransaction().add(R.id.modeInfoPanel, modeRtl).commit();
        }

        mapFragment = fragmentManager.findFragmentById(R.id.mapFragment);
        if(mapFragment == null){
            mapFragment = new FlightMapFragment();
            fragmentManager.beginTransaction().add(R.id.mapFragment, mapFragment).commit();
        }

        Fragment telemetryFragment = fragmentManager.findFragmentById(R.id.telemetryFragment);
        if(telemetryFragment == null){
            telemetryFragment = new TelemetryFragment();
            fragmentManager.beginTransaction().add(R.id.telemetryFragment,
                    telemetryFragment).commit();
        }

        Fragment editorTools = fragmentManager.findFragmentById(R.id.editorToolsFragment);
        if(editorTools == null){
            editorTools = new FlightActionsFragment();
            fragmentManager.beginTransaction().add(R.id.editorToolsFragment, editorTools).commit();
        }
	}

	@Override
	protected void onStart() {
		super.onStart();
		onModeChanged(drone);	// Update the mode detail panel;
	}
	
	@Override
	public void onAddPoint(LatLng point) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMoveHome(LatLng coord) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMoveWaypoint(SpatialCoordItem waypoint, LatLng latLng) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMovePolygonPoint(PolygonPoint source, LatLng newCoord) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMapClick(LatLng point) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onMarkerClick(MissionItem wp) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onJoystickSelected() {
		toggleRCFragment();
	}

	@Override
	public void onPlanningSelected() {
		((DroneMap) mapFragment ).saveCameraPosition();
		Intent navigationIntent;
		navigationIntent = new Intent(this, EditorActivity.class);
		startActivity(navigationIntent);
	}

	private void toggleRCFragment() {
		if (rcFragment == null) {
			rcFragment = new RCFragment();
			fragmentManager.beginTransaction()
					.add(R.id.containerRC, rcFragment).commit();
		} else {
			fragmentManager.beginTransaction().remove(rcFragment).commit();
			rcFragment = null;
		}
	}

	@Override
	public void onMovingWaypoint(SpatialCoordItem source, LatLng latLng) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		super.onDroneEvent(event,drone);
		switch (event) {
		case FAILSAFE:
			onFailsafeChanged(drone);
			break;
		case MODE:
			onModeChanged(drone);
			break;
		default:
			break;
		}
		
	}

	public void onFailsafeChanged(Drone drone) {
		if (drone.state.isFailsafe()) {
			failsafeTextView.setVisibility(View.VISIBLE);
		} else {
			failsafeTextView.setVisibility(View.GONE);
		}
	}

	public void onModeChanged(Drone drone) {
		switch (drone.state.getMode()) {
		case ROTOR_RTL:
			modeInfoPanel = new ModeRTLFragment();
			break;
		case ROTOR_AUTO:
			modeInfoPanel = new ModeAutoFragment();
			break;
		case ROTOR_LAND:
			modeInfoPanel = new ModeLandFragment();
			break;
		case ROTOR_LOITER:
			modeInfoPanel = new ModeLoiterFragment();
			break;
		case ROTOR_STABILIZE:
			modeInfoPanel = new ModeStabilizeFragment();
			break;
		case ROTOR_ACRO:
			modeInfoPanel = new ModeAcroFragment();
			break;
		case ROTOR_ALT_HOLD:
			modeInfoPanel = new ModeAltholdFragment();
			break;
		case ROTOR_CIRCLE:
			modeInfoPanel = new ModeCircleFragment();
			break;
		case ROTOR_GUIDED:
			modeInfoPanel = new ModeGuidedFragment();
			break;
		case ROTOR_POSITION:
			modeInfoPanel = new ModePositionFragment();
			break;
		case ROTOR_TOY:
			modeInfoPanel = new ModeDriftFragment();
			break;
		default:
			//TODO do something better than just nothing
			return;
		}
		fragmentManager.beginTransaction()
				.replace(R.id.modeInfoPanel, modeInfoPanel).commit();		
	}

}
