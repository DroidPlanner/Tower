package com.droidplanner.drone.variables.mission.waypoints;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

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

	public GenericWaypoint(MissionItem item) {
		coordinate = new LatLng(0, 0);
		altitude = new Altitude(0);
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
}