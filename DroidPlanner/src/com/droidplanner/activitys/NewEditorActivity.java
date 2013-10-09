package com.droidplanner.activitys;

import java.util.List;

import android.app.ActionBar;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.droidplanner.R;
import com.droidplanner.drone.variables.Mission;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.fragments.EditorControlFragment.OnEditorControlInteraction;
import com.droidplanner.fragments.MissionFragment;
import com.droidplanner.fragments.PlanningMapFragment;
import com.droidplanner.fragments.helpers.GestureMapFragment;
import com.droidplanner.fragments.helpers.GestureMapFragment.OnPathFinishedListner;
import com.droidplanner.fragments.helpers.OnMapInteractionListener;
import com.droidplanner.polygon.PolygonPoint;
import com.google.android.gms.maps.model.LatLng;

public class NewEditorActivity extends NewSuperUI implements
		OnMapInteractionListener, OnEditorControlInteraction, OnPathFinishedListner {

	private PlanningMapFragment planningMapFragment;
	private GestureMapFragment gestureMapFragment;
	private MissionFragment missionFragment;
	private Mission mission;

	@Override
	public int getNavigationItem() {
		return 7;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_editor);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		planningMapFragment = ((PlanningMapFragment) getFragmentManager()
				.findFragmentById(R.id.mapFragment));
		gestureMapFragment = ((GestureMapFragment) getFragmentManager()
				.findFragmentById(R.id.gestureMapFragment));
		missionFragment = (MissionFragment) getFragmentManager()
				.findFragmentById(R.id.missionFragment1);
		
		mission = drone.mission;
		gestureMapFragment.setOnPathFinishedListner(this);
		missionFragment.setMission(mission);
		planningMapFragment.setMission(mission);
		
		mission.addOnWaypointsChangedListner(missionFragment);
		mission.addOnWaypointsChangedListner(planningMapFragment);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
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
	public void onMovingWaypoint(waypoint source, LatLng latLng) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMovePolygonPoint(PolygonPoint source, LatLng newCoord) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMapClick(LatLng point) {
		mission.addWaypoint(point);
	}
	@Override
	public void editorModeChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPathFinished(List<Point> path) {
		
		
	}

}
