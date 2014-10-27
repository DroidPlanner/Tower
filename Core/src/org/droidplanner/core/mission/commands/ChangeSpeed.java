package org.droidplanner.core.mission.commands;

import java.util.List;

import org.droidplanner.core.helpers.units.Speed;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.MAVLink.Messages.enums.MAV_FRAME;

public class ChangeSpeed extends MissionCMD {
	private Speed speed = new Speed(5);

	public ChangeSpeed(MissionItem item) {
		super(item);
	}

	public ChangeSpeed(msg_mission_item msg, Mission mission) {
		super(mission);
		unpackMAVMessage(msg);
	}

	public ChangeSpeed(Mission mission, Speed speed) {
		super(mission);
		this.speed = speed;
	}

    public ChangeSpeed(Mission mission){
        super(mission);
    }

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_DO_CHANGE_SPEED;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
		mavMsg.param2 = (float) speed.valueInMetersPerSecond();
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		speed = new Speed(mavMsg.param2);
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.CHANGE_SPEED;
	}

	public Speed getSpeed() {
		return speed;
	}

	public void setSpeed(Speed speed) {
		this.speed = speed;
	}
}