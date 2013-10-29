package com.droidplanner.drone.variables.mission.waypoints;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.fragments.markers.WaypointMarker;
import com.droidplanner.helpers.units.Length;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public abstract class GenericWaypoint extends MissionItem implements MarkerSource {
	LatLng coordinate;
	Length altitude;

	public GenericWaypoint(LatLng coord, double altitude) {
		this.coordinate = coord;
		this.altitude = new Length(altitude);
	}

	@Override
	public MarkerOptions build(Context context) {
		return WaypointMarker.build(this, context);
	}

	@Override
	public void update(Marker marker, Context context) {
		WaypointMarker.update(marker, this, context);
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
}