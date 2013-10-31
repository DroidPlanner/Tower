package com.droidplanner.drone.variables.mission.waypoints;

import android.content.Context;

import com.droidplanner.R;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.fragments.markers.helpers.MarkerWithText;
import com.droidplanner.fragments.mission.DialogMission;
import com.droidplanner.fragments.mission.DialogMissionWaypoint;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

public class Waypoint extends GenericWaypoint implements MarkerSource {
	public double delay;
	public double acceptanceRadius;
	public double yawAngle;
	public double orbitalRadius;
	public boolean orbitCCW;

	public Waypoint(LatLng coord, double altitude) {
		super(coord, altitude);
	}

	@Override
	protected BitmapDescriptor getIcon(Context context) {
		return BitmapDescriptorFactory.fromBitmap(MarkerWithText
				.getMarkerWithTextAndDetail(R.drawable.ic_wp_map, "text",
						"detail", context));
	}	
	
	@Override
	public DialogMission getDialog() {
		return new DialogMissionWaypoint();
	}
	
}