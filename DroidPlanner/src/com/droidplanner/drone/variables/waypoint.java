package com.droidplanner.drone.variables;

import com.MAVLink.Messages.ApmCommands;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.MAVLink.Messages.enums.MAV_FRAME;
import com.google.android.gms.maps.model.LatLng;

public class waypoint {
	public msg_mission_item missionItem = new msg_mission_item();

	public waypoint(LatLng c, Double h) {
		this(c.latitude, c.longitude, h);
	}

	public waypoint(Double Lat, Double Lng, Double h) {
		setCoord(new LatLng(Lat, Lng));
		setHeight(h);

		missionItem.current = 0; // TODO use correct parameter for HOME
		missionItem.frame = MAV_FRAME.MAV_FRAME_GLOBAL;
		missionItem.command = MAV_CMD.MAV_CMD_NAV_WAYPOINT;
		missionItem.param1 = 0; // TODO use correct parameter
		missionItem.param2 = 0; // TODO use correct parameter
		missionItem.param3 = 0; // TODO use correct parameter
		missionItem.param4 = 0; // TODO use correct parameter
		
		missionItem.autocontinue = 1; // TODO use correct parameter
		missionItem.target_system = 1;
		missionItem.target_component = 1;
	}

	public waypoint(msg_mission_item msg) {
		missionItem = msg;
	}

	public Double getHeight() {
		return (double) missionItem.z;
	}

	public LatLng getCoord() {
		return new LatLng(missionItem.x, missionItem.y);
	}

	public void setHeight(Double height) {
		missionItem.z = height.floatValue();
	}

	public void setCoord(LatLng coord) {
		missionItem.x = (float) coord.latitude;
		missionItem.y = (float) coord.longitude;
	}

	public ApmCommands getCmd() {
		return ApmCommands.getCmd(missionItem.command);
	}

	public void setNumber(int i) {
		missionItem.seq = (short) i;
	}

	public short getNumber() {
		return missionItem.seq;
	}

	public void setCmd(ApmCommands cmd) {
		missionItem.command = (short) cmd.getType();
	}

	public int getFrame() {
		return missionItem.frame;
	}

	public void setFrame(int i) {
		missionItem.frame = (byte) i;
	}

	public void setParameters(float parm1, float parm2, float parm3, float parm4) {
		missionItem.param1 = parm1;
		missionItem.param2 = parm2;
		missionItem.param3 = parm3;
		missionItem.param4 = parm4;
	}

	public void setCurrent(byte b) {
		missionItem.current = b;
	}

	public MAVLinkPacket pack() {
		return missionItem.pack();
	}
}