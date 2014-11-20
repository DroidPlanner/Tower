package org.droidplanner.core.mission.commands;

import java.util.List;

import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;

public class SetServo extends MissionCMD {

	private int pwm;
	private int channel;

	public SetServo(MissionItem item) {
		super(item);
	}

	public SetServo(msg_mission_item msg, Mission mission) {
		super(mission);
		unpackMAVMessage(msg);
	}

	public SetServo(Mission mission, int channel, int pwm) {
		super(mission);
		this.channel = channel;
		this.pwm = pwm;
	}
	

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		channel = (int) mavMsg.param1;
		pwm = (int) mavMsg.param2;

	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.SET_SERVO;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_DO_SET_SERVO;
		mavMsg.param1 = channel;
		mavMsg.param2 = pwm;
		return list;
	}

	public int getPwm() {
		return pwm;
	}

	public int getChannel() {
		return channel;
	}

	public void setPwm(int pwm) {
		this.pwm = pwm;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

}
