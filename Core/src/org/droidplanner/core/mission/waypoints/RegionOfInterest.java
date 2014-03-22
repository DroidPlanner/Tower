package org.droidplanner.core.mission.waypoints;

import java.util.List;

import org.droidplanner.core.mission.MissionItem;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;

public class RegionOfInterest extends SpatialCoordItem {

	public RegionOfInterest(MissionItem item) {
		super(item);
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		// TODO Auto-generated method stub
		return super.packMissionItem();
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		// TODO Auto-generated method stub
		super.unpackMAVMessage(mavMsg);
	}

}