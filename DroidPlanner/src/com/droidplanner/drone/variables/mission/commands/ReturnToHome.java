package com.droidplanner.drone.variables.mission.commands;

import com.droidplanner.fragments.mission.MissionFragment;
import com.droidplanner.fragments.mission.MissionRTLFragment;
import com.droidplanner.helpers.units.Altitude;

public class ReturnToHome extends MissionCMD{
	private Altitude returnAltitude;
	
	@Override
	public MissionFragment getDialog() {
		return new MissionRTLFragment();
	}

	public Altitude  getHeight() {
		return returnAltitude;
	}

	public void setHeight(Altitude altitude) {
		returnAltitude = altitude;
	}
}
