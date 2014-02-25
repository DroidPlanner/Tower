package org.droidplanner.drone.variables.mission.waypoints;

import java.util.List;

import org.droidplanner.R;
import org.droidplanner.drone.variables.mission.Mission;
import org.droidplanner.drone.variables.mission.MissionItem;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import org.droidplanner.fragments.mission.MissionDetailFragment;
import org.droidplanner.fragments.mission.MissionTakeoffFragment;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;

public class Takeoff extends SpatialCoordItem implements MarkerSource {

	public Takeoff(MissionItem item) {
		super(item);
	}

	public Takeoff(msg_mission_item msg, Mission mission) {
		super(mission, null, null);
		unpackMAVMessage(msg);
	}

	@Override
	public MissionDetailFragment getDetailFragment() {
		MissionDetailFragment fragment = new MissionTakeoffFragment();
		fragment.setItem(this);
		return fragment;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_TAKEOFF;
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
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