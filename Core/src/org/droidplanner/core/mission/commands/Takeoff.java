package org.droidplanner.core.mission.commands;

import java.util.List;

import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.MAVLink.Messages.enums.MAV_FRAME;

public class Takeoff extends MissionCMD {

	private Altitude finishedAlt = new Altitude(10);
	
	public Takeoff(MissionItem item) {
		super(item);
	}

	public Takeoff(msg_mission_item msg, Mission mission) {
		super(mission);
		unpackMAVMessage(msg);
	}

	public Takeoff(Mission mission, Altitude altitude) {
		super(mission);
		finishedAlt = altitude;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_TAKEOFF;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
		mavMsg.z = (float) finishedAlt.valueInMeters();
		return list;		
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		finishedAlt = new Altitude(mavMsg.z);
	}

    @Override
    public MissionItemType getType() {
        return MissionItemType.TAKEOFF;
    }

	public Altitude getFinishedAlt() {
		return finishedAlt;
	}

	public void setFinishedAlt(Altitude finishedAlt) {
		this.finishedAlt = finishedAlt;
	}
}