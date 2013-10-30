package com.droidplanner.drone.variables.mission.commands;

import com.droidplanner.dialogs.mission.DialogMission;
import com.droidplanner.dialogs.mission.DialogMissionRTL;
import com.droidplanner.helpers.units.Altitude;

public class ReturnToHome extends MissionCMD{
	private Altitude returnAltitude;
	
	@Override
	public DialogMission getDialog() {
		return new DialogMissionRTL();
	}
}
