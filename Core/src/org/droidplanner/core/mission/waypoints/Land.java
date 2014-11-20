package org.droidplanner.core.mission.waypoints;

import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;

public class Land extends SpatialCoordItem {

	public Land(MissionItem item) {
		super(item);
		setAltitude(new Altitude(0.0));
	}

	public Land(Mission mission) {
		this(mission,new Coord2D(0,0));
	}

	public Land(Mission mMission, Coord2D coord) {
		super(mMission, new Coord3D(coord,new Altitude(0)));
	}
	
	public Land(msg_mission_item msg, Mission mission) {
		super(mission, null);
		unpackMAVMessage(msg);
	}


	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_LAND;
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.LAND;
	}

}