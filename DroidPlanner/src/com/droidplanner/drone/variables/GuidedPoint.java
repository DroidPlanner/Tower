package com.droidplanner.drone.variables;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.widget.Toast;

import com.droidplanner.MAVLink.MavLinkModes;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;
import com.droidplanner.fragments.helpers.MapPath.PathSource;
import com.droidplanner.fragments.markers.GuidedMarker;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.helpers.units.Altitude;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GuidedPoint extends DroneVariable implements MarkerSource, PathSource {
	private LatLng coord;
	private Altitude altitude;

	public interface OnGuidedListener {
		public void onGuidedPoint();
	}

	public GuidedPoint(Drone myDrone) {
		super(myDrone);
	}

	public void newGuidedPointWithCurrentAlt(LatLng coord) {
		altitude = new Altitude(myDrone.altitude.getAltitude());
		newGuidedPointwithLastAltitude(coord);
	}

	public void newGuidedPointwithLastAltitude(LatLng coord) {
		this.coord = coord;
		sendGuidedPoint();
	}

	private void sendGuidedPoint() {
		myDrone.notifyGuidedPointChange();
		MavLinkModes.setGuidedMode(myDrone, coord.latitude, coord.longitude,
				this.altitude.valueInMeters());
		Toast.makeText(myDrone.context, "Guided Mode (" + altitude + ")",
				Toast.LENGTH_SHORT).show();
	}

	public LatLng getCoord() {
		return coord;
	}

	public void invalidateCoord() {
		if (isValid()) {
			coord = null;
			altitude = null;
			myDrone.notifyGuidedPointChange();			
		}
	}

	public boolean isValid() {
		return (coord != null) & (altitude != null);
	}

	@Override
	public MarkerOptions build(Context context) {
		return GuidedMarker.build(this, altitude, context);
	}

	@Override
	public void update(Marker markerFromGcp, Context context) {
		GuidedMarker.update(markerFromGcp, this, altitude, context);
	}

	@Override
	public List<LatLng> getPathPoints() {
		List<LatLng> path = new ArrayList<LatLng>();
		if (isValid()) {
			path.add(myDrone.GPS.getPosition());
			path.add(coord);
		}
		return path;
	}
}