package com.droidplanner.drone.variables.mission.waypoints;

import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.fragments.mission.MissionDetailFragment;
import com.droidplanner.fragments.mission.MissionLoiterTFragment;
import com.google.android.gms.maps.model.LatLng;

public class LoiterTime extends Loiter implements MarkerSource {
	double time;
	
	public LoiterTime(LatLng coord, double altitude) {
		super(coord, altitude);
	}

	public LoiterTime(MissionItem item) {
		super(item);
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}
	
	@Override
	public MissionDetailFragment getDetailFragment() {
		MissionDetailFragment fragment = new MissionLoiterTFragment();
		fragment.setItem(this);
		return fragment;
	}

	
}