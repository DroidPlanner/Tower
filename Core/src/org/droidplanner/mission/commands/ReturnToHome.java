package org.droidplanner.mission.commands;

import java.util.List;

import org.droidplanner.helpers.units.Altitude;
import org.droidplanner.mission.MissionItem;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;

public abstract class ReturnToHome extends MissionCMD {

	protected Altitude returnAltitude;

	public ReturnToHome(MissionItem item) {
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