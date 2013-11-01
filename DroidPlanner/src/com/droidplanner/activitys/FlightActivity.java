package com.droidplanner.activitys;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperUI;
import com.droidplanner.drone.variables.mission.waypoints.SpatialCoordItem;
import com.droidplanner.fragments.MissionControlFragment.OnMissionControlInteraction;
import com.droidplanner.fragments.RCFragment;
import com.droidplanner.fragments.helpers.OnMapInteractionListener;
import com.droidplanner.polygon.PolygonPoint;
import com.google.android.gms.maps.model.LatLng;

public class FlightActivity extends SuperUI implements
		OnMapInteractionListener, OnMissionControlInteraction {
	private FragmentManager fragmentManager;
	private RCFragment rcFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight);
		fragmentManager = getFragmentManager();

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
	public boolean onMarkerClick(SpatialCoordItem wp) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onJoystickSelected() {
		toggleRCFragment();		
	}

	@Override
	public void onPlanningSelected() {		
		Intent navigationIntent;
		navigationIntent = new Intent(this, EditorActivity.class);
		startActivity(navigationIntent);
	}

	@Override
	public void onArmSelected() {
		// TODO Auto-generated method stub	
		/*
		 * 			if (drone.MavClient.isConnected()) {
				if (!drone.state.isArmed()) {
					armBtn.setImageResource(R.drawable.arma);
					drone.tts.speak("Arming the vehicle, please standby");
				}
				MavLinkArm.sendArmMessage(drone, !drone.state.isArmed());
			}
		 */
	}

	@Override
	public void onDisArmSelected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectSelected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisConnectSelected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRTLSelected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLandSelected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTakeOffSelected() {
		// TODO Auto-generated method stub
		
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
	

}
