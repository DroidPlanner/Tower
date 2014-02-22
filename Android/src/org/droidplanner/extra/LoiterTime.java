package org.droidplanner.extra;


import org.droidplanner.R;
import org.droidplanner.drone.variables.missionD.MissionItemD;
import org.droidplanner.drone.variables.missionD.waypoints.LoiterTimeD;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import org.droidplanner.fragments.mission.MissionDetailFragment;
import org.droidplanner.fragments.mission.MissionLoiterTFragment;


public class LoiterTime extends LoiterTimeD implements MarkerSource {
	public LoiterTime(MissionItemD item) {
		super(item);
	}

	@Override
	public MissionDetailFragment getDetailFragment() {
		MissionDetailFragment fragment = new MissionLoiterTFragment();
		fragment.setItem(this);
		return fragment;
	}

	@Override
	protected int getIconDrawable() {
		return R.drawable.ic_wp_loiter;
	}
	
	@Override
	protected int getIconDrawableSelected() {
		return R.drawable.ic_wp_loiter_selected;
	}
	
}