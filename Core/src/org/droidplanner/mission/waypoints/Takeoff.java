package org.droidplanner.mission.waypoints;

import java.util.List;

import org.droidplanner.mission.MissionItem;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;

public abstract class Takeoff extends SpatialCoordItem {

	private double yawAngle;
	private double minPitch;

	public Takeoff(MissionItem item) {
		super(item);
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_TAKEOFF;
		mavMsg.param1 = (float) getMinPitch();
		mavMsg.param4 = (float) getYawAngle();
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
		setMinPitch(mavMsg.param1);
		setYawAngle(mavMsg.param4);
	}

	public double getYawAngle() {
		return yawAngle;
	}

	public void setYawAngle(double yawAngle) {
		this.yawAngle = yawAngle;
	}

	public double getMinPitch() {
		return minPitch;
	}

	public void setMinPitch(double minPitch) {
		this.minPitch = minPitch;
	}

}