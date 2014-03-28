package org.droidplanner.drone.variables.mission.commands;

import java.util.List;

import org.droidplanner.drone.variables.mission.Mission;
import org.droidplanner.drone.variables.mission.MissionItem;
import org.droidplanner.fragments.mission.MissionDetailFragment;
import org.droidplanner.fragments.mission.MissionRTLFragment;
import org.droidplanner.helpers.units.Altitude;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.MAVLink.Messages.enums.MAV_FRAME;

public class ReturnToHome extends MissionCMD {
	private Altitude returnAltitude;

	public ReturnToHome(MissionItem item) {
		super(item);
		returnAltitude = new Altitude(0);
	}
	
	public ReturnToHome(Mission mission) {
	    super(mission);
	}

	public ReturnToHome(msg_mission_item msg, Mission mission) {
		super(mission);
		unpackMAVMessage(msg);
	}

	@Override
	public MissionDetailFragment getDetailFragment() {
		MissionDetailFragment fragment = new MissionRTLFragment();
		fragment.setItem(this);
		return fragment;
	}

	public Altitude getHeight() {
		return returnAltitude;
	}

	public void setHeight(Altitude altitude) {
		returnAltitude = altitude;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
		mavMsg.z = (float) returnAltitude.valueInMeters();
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMessageItem) {
		returnAltitude = new Altitude(mavMessageItem.z);
	}
}
