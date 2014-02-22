package org.droidplanner.mission.commands;

import org.droidplanner.mission.Mission;
import org.droidplanner.mission.MissionItem;

public abstract class MissionCMD extends MissionItem{
	
	public MissionCMD(Mission mission) {
		super(mission);
	}

	public MissionCMD(MissionItem item) {
		super(item);
	}
	
}