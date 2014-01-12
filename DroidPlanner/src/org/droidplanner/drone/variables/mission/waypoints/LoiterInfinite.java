package org.droidplanner.drone.variables.mission.waypoints;

import org.droidplanner.drone.variables.mission.MissionItem;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import org.droidplanner.fragments.mission.MissionDetailFragment;
import org.droidplanner.fragments.mission.MissionLoiterFragment;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;

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

	@Override
	public msg_mission_item packMissionItem() {
		return super.packMissionItem();
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
	}

}