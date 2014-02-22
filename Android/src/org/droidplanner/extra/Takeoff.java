package org.droidplanner.extra;


import org.droidplanner.R;
import org.droidplanner.drone.variables.missionD.MissionItemD;
import org.droidplanner.drone.variables.missionD.waypoints.TakeoffD;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import org.droidplanner.fragments.mission.MissionDetailFragment;
import org.droidplanner.fragments.mission.MissionTakeoffFragment;


public class Takeoff extends TakeoffD implements MarkerSource {

	@Override
	public MissionDetailFragment getDetailFragment() {
		MissionDetailFragment fragment = new MissionTakeoffFragment();
		fragment.setItem(this);
		return fragment;
	}

	@Override
	protected int getIconDrawable() {
		return R.drawable.ic_wp_takeoff;
	}
	
	@Override
	protected int getIconDrawableSelected() {
		return R.drawable.ic_wp_takeof_selected;
	}
}