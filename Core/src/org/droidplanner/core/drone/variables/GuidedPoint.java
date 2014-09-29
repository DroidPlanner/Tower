package org.droidplanner.core.drone.variables;

import org.droidplanner.core.MAVLink.MavLinkModes;
import org.droidplanner.core.MAVLink.MavLinkTakeoff;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.model.Drone;

import com.MAVLink.Messages.ApmModes;

public class GuidedPoint extends DroneVariable implements OnDroneListener {

	private GuidedStates state = GuidedStates.UNINITIALIZED;
	private Coord2D coord = new Coord2D(0, 0);
	private Altitude altitude = new Altitude(0.0);

	private enum GuidedStates {
		UNINITIALIZED, IDLE, ACTIVE
	}

	public GuidedPoint(Drone myDrone) {
		super(myDrone);
		myDrone.addDroneListener(this);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case MODE:
			if ((myDrone.getState().getMode() == ApmModes.ROTOR_GUIDED)) {
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

	public void doGuidedTakeoff(Altitude alt) {
		coord = myDrone.getGps().getPosition();
		altitude.set(alt.valueInMeters());
		state = GuidedStates.IDLE;		
		myDrone.getState().changeFlightMode(ApmModes.ROTOR_GUIDED);
		MavLinkTakeoff.sendTakeoff(myDrone, alt);
		myDrone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
		
	}

	public void newGuidedCoord(Coord2D coord) {
		changeCoord(coord);
	}

	public void changeGuidedAltitude(double altChange) {
		changeAlt(altChange);
	}

	public void forcedGuidedCoordinate(Coord2D coord) throws Exception {
		if ((myDrone.getGps().getFixTypeNumeric() != GPS.LOCK_3D)) {
			throw new Exception("Bad GPS for guided");
		}
		initialize();
		changeCoord(coord);
	}

	private void initialize() {
		if (state == GuidedStates.UNINITIALIZED) {
			coord = myDrone.getGps().getPosition();
			altitude.set(getDroneAltConstained());
			state = GuidedStates.IDLE;
			myDrone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
		}
	}

	private void disable() {
		state = GuidedStates.UNINITIALIZED;
		myDrone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
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

	private void changeCoord(Coord2D coord) {
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
			myDrone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
			MavLinkModes.setGuidedMode(myDrone, coord.getLat(), coord.getLng(),
					altitude.valueInMeters());
		}
	}

	private double getDroneAltConstained() {
		double alt = Math.floor(myDrone.getAltitude().getAltitude());
		return Math.max(alt, 2.0);
	}

	public Coord2D getCoord() {
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

}
