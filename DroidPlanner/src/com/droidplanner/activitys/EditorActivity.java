package com.droidplanner.activitys;

import java.util.List;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperUI;
import com.droidplanner.drone.variables.mission.Mission;
import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.drone.variables.mission.waypoints.GenericWaypoint;
import com.droidplanner.fragments.EditorToolsFragment;
import com.droidplanner.fragments.EditorToolsFragment.EditorTools;
import com.droidplanner.fragments.EditorToolsFragment.OnEditorToolSelected;
import com.droidplanner.fragments.PlanningMapFragment;
import com.droidplanner.fragments.helpers.GestureMapFragment;
import com.droidplanner.fragments.helpers.GestureMapFragment.OnPathFinishedListner;
import com.droidplanner.fragments.helpers.MapProjection;
import com.droidplanner.fragments.helpers.OnMapInteractionListener;
import com.droidplanner.fragments.mission.MissionDetailFragment;
import com.droidplanner.polygon.PolygonPoint;
import com.google.android.gms.maps.model.LatLng;

public class EditorActivity extends SuperUI implements
		OnMapInteractionListener, OnPathFinishedListner, OnEditorToolSelected {

	private PlanningMapFragment planningMapFragment;
	private GestureMapFragment gestureMapFragment;
	private Mission mission;
	private EditorToolsFragment editorToolsFragment;
	private MissionDetailFragment itemDetailFragment;
	private FragmentManager fragmentManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editor);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		fragmentManager = getFragmentManager();

		planningMapFragment = ((PlanningMapFragment) fragmentManager
				.findFragmentById(R.id.mapFragment));
		gestureMapFragment = ((GestureMapFragment) fragmentManager
				.findFragmentById(R.id.gestureMapFragment));
		editorToolsFragment = (EditorToolsFragment) fragmentManager
				.findFragmentById(R.id.editorToolsFragment);

		mission = drone.mission;
		gestureMapFragment.setOnPathFinishedListner(this);
		mission.onMissionUpdate();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mission.removeOnMissionUpdateListner(planningMapFragment);
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
	public boolean onMarkerClick(GenericWaypoint wp) {
		showItemDetail(wp);
		return true;
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
	public void onMoveWaypoint(GenericWaypoint waypoint, LatLng latLng) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMovingWaypoint(GenericWaypoint source, LatLng latLng) {
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
			mission.addWaypoint(point, mission.getDefaultAlt());
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
		removeItemDetail();	//TODO remove this, used for debbuging
		if (tools == EditorTools.DRAW) {
			gestureMapFragment.enableGestureDetection();
		} else {
			gestureMapFragment.disableGestureDetection();
		}
	}

	private void showItemDetail(MissionItem item) {
		if (itemDetailFragment == null) {
			addItemDetail(item);
		} else {
			switchItemDetail(item);
		}
	}

	private void addItemDetail(MissionItem item) {
		itemDetailFragment = item.getDetailFragment();
		fragmentManager.beginTransaction()
				.add(R.id.containerItemDetail, itemDetailFragment).commit();
	}

	private void switchItemDetail(MissionItem item) {
		itemDetailFragment = item.getDetailFragment();
		fragmentManager.beginTransaction()
				.replace(R.id.containerItemDetail, itemDetailFragment).commit();
	}

	private void removeItemDetail() {
		if (itemDetailFragment != null) {
			fragmentManager.beginTransaction().remove(itemDetailFragment).commit();
			itemDetailFragment = null;			
		}
	}

	@Override
	public void onPathFinished(List<Point> path) {
		List<LatLng> points = MapProjection.projectPathIntoMap(path,
				planningMapFragment.mMap);
		drone.mission.addWaypointsWithDefaultAltitude(points);
		editorToolsFragment.setTool(EditorTools.MARKER);
	}

}
