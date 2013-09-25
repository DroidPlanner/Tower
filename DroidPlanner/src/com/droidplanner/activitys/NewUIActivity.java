package com.droidplanner.activitys;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.fragments.PlanningFragment;
import com.droidplanner.fragments.RCFragment;
import com.droidplanner.fragments.helpers.OnMapInteractionListener;
import com.droidplanner.polygon.PolygonPoint;
import com.google.android.gms.maps.model.LatLng;

public class NewUIActivity extends SuperActivity implements
		OnMapInteractionListener {
	private FragmentManager fragmentManager;
	private RCFragment rcFragment;
	private PlanningFragment planningFragment;

	@Override
	public int getNavigationItem() {
		return 7;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_newui);
		fragmentManager = getFragmentManager();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_newui, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_ui_rc:
			toggleRCFragment();
			return true;
		case R.id.menu_ui_b2:
			togglePlanningFragment();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
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
	
	private void togglePlanningFragment() {
		if (planningFragment == null) {
			planningFragment = new PlanningFragment();
			fragmentManager.beginTransaction()
					.add(R.id.containerPlanning, planningFragment).commit();
		} else {
			fragmentManager.beginTransaction().remove(planningFragment).commit();
			planningFragment = null;
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
	public void onMoveWaypoint(waypoint waypoint, LatLng latLng) {
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

}
