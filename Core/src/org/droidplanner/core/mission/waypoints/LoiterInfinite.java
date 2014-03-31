package org.droidplanner.core.mission.waypoints;

import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;

public class LoiterInfinite extends Loiter {

	public LoiterInfinite(MissionItem item) {
		super(item);
	}

	public LoiterInfinite(Mission mission, Coord3D coord) {
		super(mission, coord);
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		return super.packMissionItem();
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
	}

    @Override
    public MissionItemType getType() {
        return MissionItemType.LOITER_INF;
    }

}