package com.droidplanner.drone.variables.mission.waypoints;

import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.fragments.mission.MissionDetailFragment;
import com.droidplanner.fragments.mission.MissionLoiterFragment;

public class LoiterInfinite extends Loiter implements MarkerSource {
	
	public LoiterInfinite(MissionItem item) {
		super(item);
	}

	@Override
	public MissionDetailFragment getDetailFragment() {
		MissionDetailFragment fragment = new MissionLoiterFragment();
		fragment.setItem(this);
		return fragment;
	}

}