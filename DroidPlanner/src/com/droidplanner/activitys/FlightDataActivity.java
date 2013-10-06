package com.droidplanner.activitys;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.droidplanner.DroidPlannerApp.OnWaypointUpdateListner;
import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperFlightActivity;
import com.droidplanner.drone.DroneInterfaces.ModeChangedListener;
import com.droidplanner.fragments.FlightMapFragment;

public class FlightDataActivity extends SuperFlightActivity implements
		OnWaypointUpdateListner, ModeChangedListener
{

	@Override
	public int getNavigationItem() {
		return 1;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_flightdata);

		mapFragment = ((FlightMapFragment) getFragmentManager()
				.findFragmentById(R.id.flightMapFragment));
		mapFragment.updateFragment();

		drone.mission.missionListner = this;
		drone.setDroneTypeChangedListner(this);
		drone.setModeChangedListener(this);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_flightdata, menu);
		getMenuInflater().inflate(R.menu.menu_map_type, menu);
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
	public void onAltitudeChanged(double newAltitude)
	{
		// delegate to super first - sets app vars etc
		super.onAltitudeChanged(newAltitude);

		mapFragment.updateFragment();
	}

	@Override
	public void onModeChanged()
	{
		mapFragment.onModeChanged();
	}
}
