package org.droidplanner.core.mission.commands;

import java.util.List;

import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;

import com.MAVLink.common.msg_mission_item;

public class EpmGripper extends MissionCMD {
	// TODO Update mavlink and use the correct enum here
	public final static short MAV_CMD_DO_GRIPPER = 211;
	public final static int GRIPPER_ACTION_RELEASE = 0;
	public final static int GRIPPER_ACTION_GRAB = 1;

	private boolean release = true;

	public EpmGripper(MissionItem item) {
		super(item);
	}

	public EpmGripper(msg_mission_item msg, Mission mission) {
		super(mission);
		unpackMAVMessage(msg);
	}

	public EpmGripper(Mission mission, boolean release) {
		super(mission);
		this.release = release;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD_DO_GRIPPER;
		mavMsg.param2 = release ? GRIPPER_ACTION_RELEASE : GRIPPER_ACTION_GRAB;
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		if (mavMsg.param2 == GRIPPER_ACTION_GRAB) {
			release = false;
		} else if (mavMsg.param2 == GRIPPER_ACTION_RELEASE) {
			release = true;
		}
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.EPM_GRIPPER;
	}

	public boolean isRelease() {
		return release;
	}

	public void setAsRelease(boolean release) {
		this.release = release;
	}
}