package org.droidplanner.drone.variables.missionD.commands;

import java.util.List;

import org.droidplanner.drone.variables.missionD.MissionItemD;
import org.droidplanner.helpers.units.Altitude;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;

public abstract class ReturnToHomeD extends MissionCMD {

	protected Altitude returnAltitude;

	public ReturnToHomeD(MissionItemD item) {
		super(item);
		returnAltitude = new Altitude(0);
	}

	public Altitude getHeight() {
		return returnAltitude;
	}

	public void setHeight(Altitude altitude) {
		returnAltitude = altitude;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMessageItem) {
		// TODO Auto-generated method stub
		
	}

}