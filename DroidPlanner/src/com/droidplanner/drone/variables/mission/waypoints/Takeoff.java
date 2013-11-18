package com.droidplanner.drone.variables.mission.waypoints;

import android.content.Context;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.droidplanner.R;
import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.fragments.markers.helpers.MarkerWithText;
import com.droidplanner.fragments.mission.MissionDetailFragment;
import com.droidplanner.fragments.mission.MissionTakeoffFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class Takeoff extends SpatialCoordItem implements MarkerSource {

	private double yawAngle;
	private double minPitch;

	public Takeoff(MissionItem item) {
		super(item);
	}

	@Override
	public MissionDetailFragment getDetailFragment() {
		MissionDetailFragment fragment = new MissionTakeoffFragment();
		fragment.setItem(this);
		return fragment;
	}

	@Override
	public msg_mission_item packMissionItem() {
		msg_mission_item mavMsg = super.packMissionItem();
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_TAKEOFF;
		mavMsg.param1 = (float) getMinPitch();
		mavMsg.param4 = (float) getYawAngle();
		return mavMsg;
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