package com.droidplanner.activitys;

import java.util.List;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.droidplanner.DroidPlannerApp.OnWaypointUpdateListner;
import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperFlightActivity;
import com.droidplanner.fragments.FlightMapFragment;
import com.droidplanner.fragments.FlightMapFragment.OnDroneClickListner;
import com.droidplanner.fragments.helpers.FlightGestureMapFragment;
import com.droidplanner.fragments.helpers.FlightGestureMapFragment.OnPathFinishedListner;
import com.droidplanner.fragments.helpers.MapProjection;
import com.google.android.gms.maps.model.LatLng;

public class FlightDataActivity extends SuperFlightActivity implements
		OnWaypointUpdateListner, OnPathFinishedListner, OnDroneClickListner {

	private FlightGestureMapFragment gestureMapFragment;

	@Override
	public int getNavigationItem() {
		return 1;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.flightdata);

		mapFragment = ((FlightMapFragment) getFragmentManager()
				.findFragmentById(R.id.flightMapFragment));
		mapFragment.updateFragment();
		mapFragment.setOnDroneClickListner(this);
		
		gestureMapFragment = ((FlightGestureMapFragment) getFragmentManager()
				.findFragmentById(R.id.flightGestureMapFragment));

		gestureMapFragment.setOnPathFinishedListner(this);

		drone.mission.missionListner = this;
		drone.setDroneTypeChangedListner(this);

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_flightdata, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_zoom:
			mapFragment.zoomToLastKnowPosition();
			return true;
		case R.id.menu_clearFlightPath:
			mapFragment.clearFlightPath();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	public void onDroneClick() {
		gestureMapFragment.enableGestureDetection();
	}
	
	@Override
	public void onPathFinished(List<Point> path) {
		List<LatLng> points = MapProjection.projectPathIntoMap(path,
				mapFragment.mMap);
		drone.mission.clearWaypoints();
		drone.mission.addWaypointsWithDefaultAltitude(points);
		mapFragment.updateMissionPath(drone);
		drone.mission.sendMissionToAPM(true);
	}


}
