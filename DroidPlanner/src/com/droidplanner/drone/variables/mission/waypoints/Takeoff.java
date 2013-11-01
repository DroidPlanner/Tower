package com.droidplanner.drone.variables.mission.waypoints;

import android.content.Context;

import com.droidplanner.R;
import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.fragments.markers.helpers.MarkerWithText;
import com.droidplanner.fragments.mission.MissionDetailFragment;
import com.droidplanner.fragments.mission.MissionTakeoffFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class Takeoff extends SpatialCoordItem implements MarkerSource {

	public double yawAngle;
	public double minPitch;

	public Takeoff(MissionItem item) {
		super(item);
	}

	@Override
	protected BitmapDescriptor getIcon(Context context) {
		return BitmapDescriptorFactory.fromBitmap(MarkerWithText
				.getMarkerWithTextAndDetail(R.drawable.ic_wp_map, "text",
						"detail", context));
	}

	@Override
	public MissionDetailFragment getDetailFragment() {
		MissionDetailFragment fragment = new MissionTakeoffFragment();
		fragment.setItem(this);
		return fragment;
	}

}