package org.droidplanner.drone.variables.mission.waypoints;

import org.droidplanner.drone.variables.mission.MissionItem;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import org.droidplanner.fragments.mission.MissionDetailFragment;
import org.droidplanner.fragments.mission.MissionLoiterTFragment;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;

public class LoiterTime extends Loiter implements MarkerSource {
	double time;
	
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

	@Override
	public msg_mission_item packMissionItem() {
		msg_mission_item mavMsg = super.packMissionItem();
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_LOITER_TIME;
		mavMsg.param1 = (float) getTime();
		return mavMsg;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
		setTime(mavMsg.param1);
	}

	
}