package org.droidplanner.core.mission.commands;

import java.util.List;

import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;

import com.MAVLink.common.msg_mission_item;

public abstract class MissionCMD extends MissionItem {

	public MissionCMD(Mission mission) {
		super(mission);
	}

	public MissionCMD(MissionItem item) {
		super(item);
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		return super.packMissionItem();
	}

}