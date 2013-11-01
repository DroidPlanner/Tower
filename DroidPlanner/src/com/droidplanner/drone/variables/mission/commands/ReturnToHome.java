package com.droidplanner.drone.variables.mission.commands;

import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.fragments.mission.MissionDetailFragment;
import com.droidplanner.fragments.mission.MissionRTLFragment;
import com.droidplanner.helpers.units.Altitude;

public class ReturnToHome extends MissionCMD{
	private Altitude returnAltitude;
	
	public ReturnToHome(MissionItem item) {
		super(item);
		returnAltitude = new Altitude(0);
	}

	@Override
	public MissionDetailFragment getDetailFragment() {
		MissionDetailFragment fragment = new MissionRTLFragment();
		fragment.setItem(this);
		return fragment;
	}

	public Altitude  getHeight() {
		return returnAltitude;
	}

	public void setHeight(Altitude altitude) {
		returnAltitude = altitude;
	}
}
