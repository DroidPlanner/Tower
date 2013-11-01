package com.droidplanner.drone.variables.mission.commands;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.droidplanner.fragments.mission.MissionDetailFragment;
import com.droidplanner.fragments.mission.MissionRTLFragment;
import com.droidplanner.helpers.units.Altitude;

public class ReturnToHome extends MissionCMD{
	private Altitude returnAltitude;
	
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

	@Override
	public msg_mission_item packMissionItem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMessageItem) {
		// TODO Auto-generated method stub
		
	}
}
