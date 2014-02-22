package org.droidplanner.extra;



import org.droidplanner.R;
import org.droidplanner.drone.variables.mission.Mission;
import org.droidplanner.drone.variables.missionD.MissionItemD;
import org.droidplanner.drone.variables.missionD.waypoints.WaypointD;
import org.droidplanner.fragments.mission.MissionDetailFragment;
import org.droidplanner.fragments.mission.MissionWaypointFragment;
import org.droidplanner.helpers.units.Altitude;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.google.android.gms.maps.model.LatLng;

public class Waypoint extends WaypointD {

	@Override
	public MissionDetailFragment getDetailFragment() {
		MissionDetailFragment fragment = new MissionWaypointFragment();
		fragment.setItem(this);
		return fragment;
	}

	@Override
	protected int getIconDrawable() {
		return R.drawable.ic_wp_map;
	}

	@Override
	protected int getIconDrawableSelected() {
		return R.drawable.ic_wp_map_selected;
	}
	
}