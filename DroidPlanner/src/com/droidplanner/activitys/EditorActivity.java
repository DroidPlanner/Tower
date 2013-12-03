package com.droidplanner.activitys;

import java.util.List;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.OnEditorInteraction;
import com.droidplanner.activitys.helpers.SuperUI;
import com.droidplanner.drone.DroneInterfaces.OnWaypointChangedListner;
import com.droidplanner.drone.variables.mission.Mission;
import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.drone.variables.mission.waypoints.SpatialCoordItem;
import com.droidplanner.fragments.EditorToolsFragment;
import com.droidplanner.fragments.EditorToolsFragment.EditorTools;
import com.droidplanner.fragments.EditorToolsFragment.OnEditorToolSelected;
import com.droidplanner.fragments.PlanningMapFragment;
import com.droidplanner.fragments.helpers.GestureMapFragment;
import com.droidplanner.fragments.helpers.GestureMapFragment.OnPathFinishedListner;
import com.droidplanner.fragments.helpers.MapProjection;
import com.droidplanner.fragments.helpers.OnMapInteractionListener;
import com.droidplanner.fragments.mission.MissionDetailFragment;
import com.droidplanner.fragments.mission.MissionDetailFragment.OnWayPointTypeChangeListener;
import com.droidplanner.polygon.PolygonPoint;
import com.google.android.gms.maps.model.LatLng;

public class EditorActivity extends SuperUI implements
		OnMapInteractionListener, OnPathFinishedListner, OnEditorToolSelected,
		OnWayPointTypeChangeListener, OnWaypointChangedListner, OnEditorInteraction ,Callback{


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

		removeItemDetail(); // When doing things like screen rotation remove the detail window
		
		mission = drone.mission;
		gestureMapFragment.setOnPathFinishedListner(this);
		mission.onMissionUpdate();
		
		mission.addOnMissionUpdateListner(this);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mission.removeOnMissionUpdateListner(this);
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
	public boolean onMarkerClick(MissionItem wp) {
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
	public void onMoveWaypoint(SpatialCoordItem waypoint, LatLng latLng) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMovingWaypoint(SpatialCoordItem source, LatLng latLng) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMovePolygonPoint(PolygonPoint source, LatLng newCoord) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMapClick(LatLng point) {
		switch (getTool()) {
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

	public EditorTools getTool() {
		return editorToolsFragment.getTool();
	}

	@Override
	public void editorToolChanged(EditorTools tools) {
		removeItemDetail();
		switch (tools) {
		case DRAW:
		case POLY:
			gestureMapFragment.enableGestureDetection();
			break;
		case MARKER:
		case TRASH:
			gestureMapFragment.disableGestureDetection();
			break;
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
			fragmentManager.beginTransaction().remove(itemDetailFragment)
					.commit();
			itemDetailFragment = null;
		}
	}

	@Override
	public void onPathFinished(List<Point> path) {
		List<LatLng> points = MapProjection.projectPathIntoMap(path,
				planningMapFragment.mMap);
		switch (getTool()) {
		case DRAW:
			drone.mission.addWaypointsWithDefaultAltitude(points);
			break;
		case POLY:
			drone.mission.addSurveyPolygon(points);
			break;
		default:			
			break;
		}
		editorToolsFragment.setTool(EditorTools.MARKER);
	}

	@Override
	public void onWaypointTypeChanged(MissionItem newItem, MissionItem oldItem) {
		mission.replace(oldItem, newItem);
		showItemDetail(newItem);
	}

	@Override
	public void onMissionUpdate() {
		//Remove detail window if item is removed
		if (itemDetailFragment!=null) {
			if (!mission.hasItem(itemDetailFragment.getItem())) {
				removeItemDetail();
			}
		}
	}



	private static final int MENU_DELETE = 1;

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		// TODO Auto-generated method stub
		Log.d("LIST", "you onActionItemClicked ");
		
		if (item.getItemId()==MENU_DELETE) {
			//deleteSelected();
		}
		mode.finish();
		return false;
	}

	@Override
	public boolean onCreateActionMode(ActionMode arg0, Menu menu) {
		Log.d("LIST", "you onCreateActionMode ");
		menu.add( 0, MENU_DELETE, 0, "Delete" );
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode arg0) {
		Log.d("LIST", "you onDestroyActionMode ");
		
	}

	@Override
	public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
		Log.d("LIST", "you onPrepareActionMode ");
		return false;
	}
	
	@Override
	public void onItemLongClick(MissionItem item) {
		startActionMode(this);
	}

	@Override
	public void onItemClick(MissionItem item) {
		switch (editorToolsFragment.getTool()) {
		default:
			showItemDetail(item);
			break;
		case TRASH:
			mission.removeWaypoint(item);
			break;
		}
	}

}
