package org.droidplanner.core.mission.waypoints;

import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;

public class Takeoff extends SpatialCoordItem {

	public Takeoff(MissionItem item) {
		super(item);
	}
	
	public Takeoff(Mission mission, Altitude alt) {
		super(mission, new Coord3D(0, 0,alt));	
	}
	
	public Takeoff(msg_mission_item msg, Mission mission) {
		super(mission, null);
		unpackMAVMessage(msg);
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

}