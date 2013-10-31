package com.droidplanner.drone.variables.mission.commands;

import com.droidplanner.fragments.mission.DialogMission;
import com.droidplanner.fragments.mission.DialogMissionRTL;
import com.droidplanner.helpers.units.Altitude;

public class ReturnToHome extends MissionCMD{
	private Altitude returnAltitude;
	
	@Override
	public DialogMission getDialog() {
		return new DialogMissionRTL();
	}

	public Altitude  getHeight() {
		return returnAltitude;
	}

	public void setHeight(Altitude altitude) {
		returnAltitude = altitude;
	}
}
