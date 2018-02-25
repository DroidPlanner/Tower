package org.droidplanner.services.android.impl.core.mission.commands;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;

import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemType;

import java.util.List;

public class ChangeSpeedImpl extends MissionCMD {
	private double speed = 5; //meters per second

	public ChangeSpeedImpl(MissionItemImpl item) {
		super(item);
	}

	public ChangeSpeedImpl(msg_mission_item msg, MissionImpl missionImpl) {
		super(missionImpl);
		unpackMAVMessage(msg);
	}

	public ChangeSpeedImpl(MissionImpl missionImpl, double speed) {
		super(missionImpl);
		this.speed = speed;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_DO_CHANGE_SPEED;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
		mavMsg.param2 = (float) speed;
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		speed = mavMsg.param2;
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.CHANGE_SPEED;
	}

    /**
     * @return the set speed in meters per second.
     */
	public double getSpeed() {
		return speed;
	}

    /**
     * Set the speed
     * @param speed speed in meters per second.
     */
	public void setSpeed(double speed) {
		this.speed = speed;
	}
}