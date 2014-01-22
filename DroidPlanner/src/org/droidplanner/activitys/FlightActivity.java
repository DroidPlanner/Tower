package org.droidplanner.activitys;

import org.droidplanner.activitys.helpers.SuperUI;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.OnDroneListner;
import org.droidplanner.drone.variables.mission.MissionItem;
import org.droidplanner.drone.variables.mission.waypoints.SpatialCoordItem;
import org.droidplanner.fragments.FlightActionsFragment;
import org.droidplanner.fragments.FlightMapFragment;
import org.droidplanner.fragments.RCFragment;
import org.droidplanner.fragments.FlightActionsFragment.OnMissionControlInteraction;
import org.droidplanner.fragments.helpers.DroneMap;
import org.droidplanner.fragments.helpers.FlightSlidingDrawerContent;
import org.droidplanner.fragments.helpers.OnMapInteractionListener;
import org.droidplanner.fragments.mode.*;
import org.droidplanner.polygon.PolygonPoint;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import org.droidplanner.R;
import com.google.android.gms.maps.model.LatLng;

public class FlightActivity extends SuperUI implements
		OnMapInteractionListener, OnMissionControlInteraction, OnDroneListner{

    private FragmentManager fragmentManager;
	private RCFragment rcFragment;
	private View failsafeTextView;
	private Fragment mapFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight);

		fragmentManager = getSupportFragmentManager();
		failsafeTextView = findViewById(R.id.failsafeTextView);

        mapFragment = fragmentManager.findFragmentById(R.id.mapFragment);
        if(mapFragment == null){
            mapFragment = new FlightMapFragment();
            fragmentManager.beginTransaction().add(R.id.mapFragment, mapFragment).commit();
        }

        Fragment editorTools = fragmentManager.findFragmentById(R.id.editorToolsFragment);
        if(editorTools == null){
            editorTools = new FlightActionsFragment();
            fragmentManager.beginTransaction().add(R.id.editorToolsFragment, editorTools).commit();
        }

        Fragment slidingDrawerContent = fragmentManager.findFragmentById(R.id.sliding_drawer_content);
        if(slidingDrawerContent == null){
            slidingDrawerContent = new FlightSlidingDrawerContent();
            fragmentManager.beginTransaction().add(R.id.sliding_drawer_content,
                    slidingDrawerContent).commit();
        }
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

	@Override
	public CharSequence[][] getHelpItems() {
		return new CharSequence[][] {
				{ "Spline", "DP v3" },
				{ "https://www.youtube.com/watch?v=v9ydP-NWoJE",
						"https://www.youtube.com/watch?v=miwWUgX6nwY" } };
	}

}
