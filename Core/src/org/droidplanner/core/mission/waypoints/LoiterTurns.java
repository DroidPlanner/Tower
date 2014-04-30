package org.droidplanner.core.mission.waypoints;

import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;

public class LoiterTurns extends Loiter {

	private int turns;

	public LoiterTurns(MissionItem item) {
		super(item);
	}

	public LoiterTurns(Mission mission, Coord3D coord) {
		super(mission, coord);
	}

	public int getTurns() {
		return turns;
	}

	public void setTurns(int turns) {
		this.turns = turns;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_LOITER_TURNS;
		mavMsg.param1 = (float) getTurns();
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
		setTurns((int) mavMsg.param1);
	}

    @Override
    public MissionItemType getType() {
        return MissionItemType.LOITERN;
    }

}