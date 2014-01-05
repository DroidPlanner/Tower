package com.droidplanner.drone.variables;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.widget.Toast;

import com.droidplanner.MAVLink.MavLinkModes;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
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

	public void newGuidedPointwithLastAltitude(LatLng coord) {
		this.coord = coord;
		sendGuidedPoint();
	}

	public void updateGuidedPointwithDeltaAltitude(double altChange) {
		double alt = Math.floor(this.altitude.valueInMeters());
		alt = Math.max(alt, 2.0);

		if(altChange < -1 && alt <= 10)
			altChange = -1;

		if ((alt + altChange) > 1.0){
			this.altitude = new Altitude(alt + altChange);
		}

		sendGuidedPoint();
	}

	private void sendGuidedPoint() {
		myDrone.events.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
		MavLinkModes.setGuidedMode(myDrone, coord.latitude, coord.longitude,
				this.altitude.valueInMeters());
	}

	public LatLng getCoord() {
		return coord;
	}

	public Altitude getAltitude() {
		return this.altitude;
	}

	public void invalidateCoord() {
		if (isValid()) {
			coord = null;
			altitude = null;
			myDrone.events.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
		}
	}

	public void initCoord() {
		coord = myDrone.GPS.getPosition();
		double alt = Math.floor(myDrone.altitude.getAltitude());
		altitude = new Altitude(Math.max(alt, 2.0));

		myDrone.events.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);

		Toast.makeText(myDrone.context, "Init Guided Mode",
				Toast.LENGTH_SHORT).show();
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