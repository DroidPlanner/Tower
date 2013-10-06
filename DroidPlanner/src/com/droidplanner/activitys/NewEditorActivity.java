package com.droidplanner.activitys;

import android.os.Bundle;

import com.droidplanner.R;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.fragments.EditorControlFragment.OnEditorControlInteraction;
import com.droidplanner.fragments.helpers.OnMapInteractionListener;
import com.droidplanner.polygon.PolygonPoint;
import com.google.android.gms.maps.model.LatLng;

public class NewEditorActivity extends NewSuperUI implements OnMapInteractionListener,OnEditorControlInteraction {
	@Override
	public int getNavigationItem() {
		return 7;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_editor);
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void editorModeChanged() {
		// TODO Auto-generated method stub
		
	}


}
