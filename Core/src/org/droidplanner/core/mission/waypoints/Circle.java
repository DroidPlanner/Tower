package org.droidplanner.core.mission.waypoints;

import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;

public class Circle extends SpatialCoordItem {

	private double radius = 7.0;
	private int turns = 1;

	public Circle(MissionItem item) {
		super(item);
	}

	public Circle(Mission mission, Coord3D coord) {
		super(mission, coord);
	}

	public Circle(msg_mission_item msg, Mission mission) {
		super(mission, null);
		unpackMAVMessage(msg);
	}

	public void setTurns(int turns) {
		this.turns = (int)Math.abs(turns);
	}
	
	public int getNumeberOfTurns() {
		return turns;
	}

	public double getRadius() {
		return radius;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_LOITER_TURNS;
		mavMsg.param1 = Math.abs(turns);
		mavMsg.param3 = (turns > 0) ? 1 : -1;
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
		setTurns((int) mavMsg.param1);
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.CIRCLE;
	}

}