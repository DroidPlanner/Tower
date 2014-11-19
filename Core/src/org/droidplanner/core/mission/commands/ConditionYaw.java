package org.droidplanner.core.mission.commands;

import java.util.List;

import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;

public class ConditionYaw extends MissionCMD {
	private boolean isRelative = false; 
	private double angle = 0;
	private double angularSpeed = 0;

	public ConditionYaw(MissionItem item) {
		super(item);
	}

	public ConditionYaw(msg_mission_item msg, Mission mission) {
		super(mission);
		unpackMAVMessage(msg);
	}

	public ConditionYaw(Mission mission, double angle, boolean isRelative) {
		super(mission);
		setAngle(angle);
		setRelative(isRelative);
	}
	
	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_CONDITION_YAW;
		mavMsg.param1 = (float) GeoTools.warpToPositiveAngle(angle);
		mavMsg.param2 = (float) Math.abs(angularSpeed);
		mavMsg.param3 = (angularSpeed < 0) ? 1 : -1;
		mavMsg.param4 = isRelative ? 1: 0;
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		isRelative = mavMsg.param4 != 0;
		angle = mavMsg.param1;
		angularSpeed = mavMsg.param2 * (mavMsg.param3>0 ? -1: +1);
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.CONDITION_YAW;
	}	

	public void setAngle(double angle) {
		this.angle = angle;
	}
	
	public void setRelative(boolean isRelative){
		this.isRelative = isRelative;
	}
	
	public void setAngularSpeed(double angularSpeed) {
		this.angularSpeed = angularSpeed;
	}

	public double getAngle() {
		return angle;
	}

	public double getAngularSpeed() {
		return angularSpeed;
	}
	
	public boolean isRelative(){
		return isRelative;
	}


}