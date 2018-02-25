package org.droidplanner.services.android.impl.core.mission.commands;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;

import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemType;

import java.util.List;

public class SetServoImpl extends MissionCMD {

	private int pwm;
	private int channel;

	public SetServoImpl(MissionItemImpl item) {
		super(item);
	}

	public SetServoImpl(msg_mission_item msg, MissionImpl missionImpl) {
		super(missionImpl);
		unpackMAVMessage(msg);
	}

	public SetServoImpl(MissionImpl missionImpl, int channel, int pwm) {
		super(missionImpl);
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
