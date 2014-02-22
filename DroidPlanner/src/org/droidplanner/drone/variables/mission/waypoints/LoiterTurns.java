package org.droidplanner.drone.variables.mission.waypoints;


import org.droidplanner.R;
import org.droidplanner.drone.variables.missionD.MissionItemD;
import org.droidplanner.drone.variables.missionD.waypoints.LoiterTurnsD;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import org.droidplanner.fragments.mission.MissionDetailFragment;
import org.droidplanner.fragments.mission.MissionLoiterNFragment;


public class LoiterTurns extends LoiterTurnsD implements MarkerSource {
	public LoiterTurns(MissionItemD item) {
		super(item);
	}

	@Override
	public MissionDetailFragment getDetailFragment() {
		MissionDetailFragment fragment = new MissionLoiterNFragment();
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