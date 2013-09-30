package com.droidplanner.drone.variables;

import android.content.Context;

import com.MAVLink.Messages.ApmCommands;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.MAVLink.Messages.enums.MAV_FRAME;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.fragments.markers.WaypointMarker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


	 public class waypoint implements MarkerSource {
	
	public msg_mission_item missionItem = new msg_mission_item();

	public int homeType;

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
		setTarget(1, 1);
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

	public void setTarget(int target_system, int target_component) {
		missionItem.target_system = (byte) target_system;
		missionItem.target_component = (byte) target_component;
	}

	public Float getParam1() {
		return missionItem.param1;
	}

	public Float getParam2() {
		return missionItem.param2;
	}

	public Float getParam3() {
		return missionItem.param3;
	}

	public Float getParam4() {
		return missionItem.param4;
	}

	public int getAutoContinue() {
		return missionItem.autocontinue;
	}

	public void setAutoContinue(Integer i) {
		missionItem.autocontinue = i.byteValue();
	}

	@Override
	public MarkerOptions build(Context context) {
		return WaypointMarker.build(this,context);
	}

	@Override
		public void update(Marker marker, Context context) {
		WaypointMarker.update(marker, this,context);
	}

}