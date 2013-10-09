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
import com.droidplanner.fragments.EditorToolsFragment.EditorTools;
import com.droidplanner.fragments.EditorToolsFragment.OnEditorToolSelected;
import com.droidplanner.fragments.EditorToolsFragment;
import com.droidplanner.fragments.MissionFragment;
import com.droidplanner.fragments.PlanningMapFragment;
import com.droidplanner.fragments.helpers.GestureMapFragment;
import com.droidplanner.fragments.helpers.GestureMapFragment.OnPathFinishedListner;
import com.droidplanner.fragments.helpers.MapProjection;
import com.droidplanner.fragments.helpers.OnMapInteractionListener;
import com.droidplanner.polygon.PolygonPoint;
import com.google.android.gms.maps.model.LatLng;

public class NewEditorActivity extends NewSuperUI implements
		OnMapInteractionListener, OnPathFinishedListner, OnEditorToolSelected {

	private PlanningMapFragment planningMapFragment;
	private GestureMapFragment gestureMapFragment;
	private MissionFragment missionFragment;
	private Mission mission;
	private EditorToolsFragment editorToolsFragment;

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
		editorToolsFragment = (EditorToolsFragment) getFragmentManager()
				.findFragmentById(R.id.editorToolsFragment);

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
		switch (editorToolsFragment.getTool()) {
		case MARKER:
			mission.addWaypoint(point);
			break;
		case DRAW:
			break;
		case POLY:
			break;
		case TRASH:
			break;
		}
	}

	@Override
	public void editorToolChanged(EditorTools tools) {
		if (tools == EditorTools.DRAW) {
			gestureMapFragment.enableGestureDetection();
		}else {
			gestureMapFragment.disableGestureDetection();
		}

	}

	@Override
	public void onPathFinished(List<Point> path) {
		List<LatLng> points = MapProjection.projectPathIntoMap(path,
				planningMapFragment.mMap);
		drone.mission.addWaypointsWithDefaultAltitude(points);	
	}

}
