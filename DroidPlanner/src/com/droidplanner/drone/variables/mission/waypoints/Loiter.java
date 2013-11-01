package com.droidplanner.drone.variables.mission.waypoints;

import android.content.Context;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.droidplanner.R;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.fragments.markers.helpers.MarkerWithText;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

public abstract class Loiter extends GenericWaypoint implements MarkerSource {

	private double orbitalRadius;
	private double yawAngle;
	private boolean orbitCCW;
	
	public Loiter(LatLng coord, double altitude) {
		super(coord, altitude);
	}

	public void setOrbitalRadius(double radius) {
		this.orbitalRadius = radius;
	}
	
	public double getOrbitalRadius(){
		return this.orbitalRadius;
	}

	public boolean isOrbitCCW() {
		return orbitCCW;
	}

	public void setOrbitCCW(boolean orbitCCW) {
		this.orbitCCW = orbitCCW;
	}
	

	public double getYawAngle() {
		return yawAngle;
	}

	public void setYawAngle(double yawAngle) {
		this.yawAngle = yawAngle;
	}

	@Override
	public msg_mission_item packMissionItem() {
		msg_mission_item mavMsg = super.packMissionItem();
		mavMsg.param3 = (float) (isOrbitCCW()?getOrbitalRadius()*-1.0:getOrbitalRadius());
		mavMsg.param4 = (float) getYawAngle();
		return mavMsg;
	} 

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
		setOrbitCCW(mavMsg.param3<0);
		setOrbitalRadius(Math.abs(mavMsg.param3));
	}

	@Override
	protected BitmapDescriptor getIcon(Context context) {
		return BitmapDescriptorFactory.fromBitmap(MarkerWithText
				.getMarkerWithTextAndDetail(R.drawable.ic_wp_loiter, "text",
						"detail", context));
	}	
}