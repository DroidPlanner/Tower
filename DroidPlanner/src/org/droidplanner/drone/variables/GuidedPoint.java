package org.droidplanner.drone.variables;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.MAVLink.MavLinkModes;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.OnDroneListner;
import org.droidplanner.drone.DroneVariable;
import org.droidplanner.fragments.helpers.MapPath.PathSource;
import org.droidplanner.fragments.markers.GuidedMarker;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import org.droidplanner.helpers.units.Altitude;

import android.content.Context;

import com.MAVLink.Messages.ApmModes;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GuidedPoint extends DroneVariable implements MarkerSource,
		PathSource, OnDroneListner {

	private GuidedStates state = GuidedStates.UNINITIALIZED;
	private LatLng coord;
	private Altitude altitude = new Altitude(0.0);

	private enum GuidedStates {
		UNINITIALIZED, IDLE, ACTIVE
	};

	public GuidedPoint(Drone myDrone) {
		super(myDrone);
		myDrone.events.addDroneListener(this);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case MODE:
			if (myDrone.state.getMode() == ApmModes.ROTOR_GUIDED) {
				initialize();
			} else {
				disable();
			}
			break;
		case DISCONNECTED:
		case HEARTBEAT_TIMEOUT:
			disable();
		default:
			break;
		}
	}

	public void newGuidedCoord(LatLng coord) {
		changeCoord(coord);
	}

	public void changeGuidedAltitude(double altChange) {
		changeAlt(altChange);
	}

	public void forcedGuidedCoordinate(LatLng coord) {
		initialize();
		changeCoord(coord);
	}

	private void initialize() {
		if (state == GuidedStates.UNINITIALIZED) {
			coord = myDrone.GPS.getPosition();
			altitude.set(getDroneAltConstained());
			state = GuidedStates.IDLE;
			myDrone.events.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
		}
	}

	private void disable() {
		state = GuidedStates.UNINITIALIZED;
		myDrone.events.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
	}

	private void changeAlt(double altChange) {
		switch (state) {
		case UNINITIALIZED:
			break;
		case IDLE:
			state = GuidedStates.ACTIVE;
			changeAlt(altChange);
			break;
		case ACTIVE:
			double alt = Math.floor(altitude.valueInMeters());
			alt = Math.max(alt, 2.0);
	
			if (altChange < -1 && alt <= 10)
				altChange = -1;
	
			if ((alt + altChange) > 1.0) {
				altitude.set(alt + altChange);
			}
			sendGuidedPoint();
			break;
		}
	}

	private void changeCoord(LatLng coord) {
		switch (state) {
		case UNINITIALIZED:
			break;
		case IDLE:
			state = GuidedStates.ACTIVE;
			changeCoord(coord);
			break;
		case ACTIVE:
			this.coord = coord;
			sendGuidedPoint();
			break;
		}
	}

	private void sendGuidedPoint() {
		if (state == GuidedStates.ACTIVE) {
			myDrone.events.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
			MavLinkModes.setGuidedMode(myDrone, coord.latitude,
					coord.longitude, altitude.valueInMeters());
		}
	}

	private double getDroneAltConstained() {
		double alt = Math.floor(myDrone.altitude.getAltitude());
		return Math.max(alt, 2.0);
	}

	public LatLng getCoord() {
		return coord;
	}

	public Altitude getAltitude() {
		return this.altitude;
	}

	public boolean isActive() {
		return (state == GuidedStates.ACTIVE);
	}

	public boolean isInitialized() {
		return !(state == GuidedStates.UNINITIALIZED);
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
		if (isActive()) {
			path.add(myDrone.GPS.getPosition());
			path.add(coord);
		}
		return path;
	}
}