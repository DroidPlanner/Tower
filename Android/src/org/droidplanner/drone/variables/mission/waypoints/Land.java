package org.droidplanner.drone.variables.mission.waypoints;


import org.droidplanner.R;
import org.droidplanner.drone.variables.missionD.MissionItemD;
import org.droidplanner.drone.variables.missionD.waypoints.LandD;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import org.droidplanner.fragments.mission.MissionDetailFragment;
import org.droidplanner.fragments.mission.MissionLandFragment;


public class Land extends LandD implements MarkerSource {

	@Override
	public MissionDetailFragment getDetailFragment() {
		MissionDetailFragment fragment = new MissionLandFragment();
		fragment.setItem(this);
		return fragment;
	}

	@Override
	protected int getIconDrawable() {
		return R.drawable.ic_wp_land;
	}
	
	@Override
	protected int getIconDrawableSelected() {
		return R.drawable.ic_wp_lan_selected;
	}

}