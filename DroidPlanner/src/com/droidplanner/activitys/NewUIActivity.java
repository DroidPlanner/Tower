package com.droidplanner.activitys;

import android.os.Bundle;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.fragments.helpers.OnMapInteractionListener;
import com.droidplanner.polygon.PolygonPoint;
import com.google.android.gms.maps.model.LatLng;

public class NewUIActivity extends SuperActivity implements OnMapInteractionListener {

	@Override
	public int getNavigationItem() {
		return 7;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_newui);
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
