package org.droidplanner.core.mission.commands;

import java.util.List;

import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;

public class CameraTrigger extends MissionCMD {
	private Length distance = new Length(0);

	public CameraTrigger(MissionItem item) {
		super(item);
	}

	public CameraTrigger(msg_mission_item msg, Mission mission) {
		super(mission);
		unpackMAVMessage(msg);
	}

	public CameraTrigger(Mission mission, Length triggerDistance) {
		super(mission);
		this.distance = triggerDistance;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_DO_SET_CAM_TRIGG_DIST;
		mavMsg.param1 = (float) distance.valueInMeters() ;
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		distance = new Length(mavMsg.param1);
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.CAMERA_TRIGGER;
	}

	public Length getTriggerDistance() {
		return distance;
	}

	public void setTriggerDistance(Length triggerDistance) {
		this.distance = triggerDistance;
	}
}