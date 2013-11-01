package com.droidplanner.drone.variables.mission.waypoints;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.fragments.markers.GenericMarker;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.helpers.units.Altitude;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public abstract class GenericWaypoint extends MissionItem implements
		MarkerSource {
	protected abstract BitmapDescriptor getIcon(Context context);
	
	LatLng coordinate;
	Altitude altitude;

	public GenericWaypoint(LatLng coord, double altitude) {
		this.coordinate = coord;
		this.altitude = new Altitude(altitude);
	}


	@Override
	public MarkerOptions build(Context context) {
		return GenericMarker.build(coordinate).icon(getIcon(context));
	}
	
	@Override
	public void update(Marker marker, Context context) {
		marker.setPosition(coordinate);
		marker.setIcon(getIcon(context));
	}

	@Override
	public List<MarkerSource> getMarkers() throws Exception {
		ArrayList<MarkerSource> marker = new ArrayList<MarkerSource>();
		marker.add(this);
		return marker;
	}

	@Override
	public List<LatLng> getPath() throws Exception {
		ArrayList<LatLng> points = new ArrayList<LatLng>();
		points.add(coordinate);
		return points;
	}

	public void setCoordinate(LatLng position) {
		coordinate = position;
	}
	
	public LatLng getCoordinate() {
		return coordinate;
	}

	public Altitude getAltitude() {
		return altitude;
	}

	public void setAltitude(Altitude altitude) {
		this.altitude = altitude;
	}
	
	@Override
	public msg_mission_item packMissionItem() {
		msg_mission_item mavMsg = new msg_mission_item();
		mavMsg.autocontinue = 1;
		mavMsg.target_component = 1;
		mavMsg.target_system = 1;
		mavMsg.x = (float) getCoordinate().latitude;
		mavMsg.y = (float) getCoordinate().longitude;
		mavMsg.z = (float) getAltitude().valueInMeters();
//		mavMsg.compid = 
		return mavMsg;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		LatLng coord = new LatLng(mavMsg.x,mavMsg.y);
		Altitude alt = new Altitude(mavMsg.z);
		setCoordinate(coord);
		setAltitude(alt);
	}
}