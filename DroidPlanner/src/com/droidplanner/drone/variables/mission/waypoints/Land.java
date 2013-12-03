package com.droidplanner.drone.variables.mission.waypoints;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.droidplanner.R;
import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.fragments.mission.MissionDetailFragment;
import com.droidplanner.fragments.mission.MissionLandFragment;

public class Land extends SpatialCoordItem implements MarkerSource {

	private double yawAngle;

	public Land(MissionItem item) {
		super(item);
	}

	@Override
	public MissionDetailFragment getDetailFragment() {
		MissionDetailFragment fragment = new MissionLandFragment();
		fragment.setItem(this);
		return fragment;
	}

	@Override
	public msg_mission_item packMissionItem() {
		msg_mission_item mavMsg = super.packMissionItem();
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_LAND;
		mavMsg.param4 = (float) getYawAngle();
		return mavMsg;
	} 

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
		setYawAngle(mavMsg.param4);
	}

	public double getYawAngle() {
		return yawAngle;
	}

	public void setYawAngle(double yawAngle) {
		this.yawAngle = yawAngle;
	}

	@Override
	protected int getIconDrawable() {
		return R.drawable.ic_wp_land;
	}
	
	@Override
	protected int getIconDrawableSelected() {
		return R.drawable.ic_wp_lan_selected;
	}

}