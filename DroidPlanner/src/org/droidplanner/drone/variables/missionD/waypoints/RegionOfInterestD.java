package org.droidplanner.drone.variables.missionD.waypoints;

import java.util.List;

import org.droidplanner.drone.variables.missionD.MissionItemD;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;

public abstract class RegionOfInterestD extends SpatialCoordItemD {

	public RegionOfInterestD(MissionItemD item) {
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