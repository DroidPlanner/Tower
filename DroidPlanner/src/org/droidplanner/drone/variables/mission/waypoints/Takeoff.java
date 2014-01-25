package org.droidplanner.drone.variables.mission.waypoints;

import java.util.List;

import org.droidplanner.drone.variables.mission.MissionItem;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import org.droidplanner.fragments.mission.MissionDetailFragment;
import org.droidplanner.fragments.mission.MissionTakeoffFragment;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;
import org.droidplanner.R;

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

	@Override
	protected int getIconDrawable() {
		return R.drawable.ic_wp_takeoff;
	}
	
	@Override
	protected int getIconDrawableSelected() {
		return R.drawable.ic_wp_takeof_selected;
	}
}