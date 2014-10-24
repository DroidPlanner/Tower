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

    private Runnable mPostInitializationTask;

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
			if (isGuidedMode(myDrone)) {
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

    public static boolean isGuidedMode(Drone drone){
        final int droneType = drone.getType();
        final ApmModes droneMode = drone.getState().getMode();

        if(Type.isCopter(droneType)){
            return droneMode == ApmModes.ROTOR_GUIDED;
        }

        if(Type.isPlane(droneType)){
            return droneMode == ApmModes.FIXED_WING_GUIDED;
        }

        if(Type.isRover(droneType)){
            return droneMode == ApmModes.ROVER_GUIDED;
        }

        return false;
    }

	public void pauseAtCurrentLocation() {
		if (state !=GuidedStates.ACTIVE) {
			changeToGuidedMode(myDrone);
		}else{
			newGuidedCoord(myDrone.getGps().getPosition());
		}
	}

    public static void changeToGuidedMode(Drone drone){
        final State droneState = drone.getState();
        final int droneType = drone.getType();
        if(Type.isCopter(droneType)){
            droneState.changeFlightMode(ApmModes.ROTOR_GUIDED);
        }
        else if(Type.isPlane(droneType)){
            //You have to send a guided point to the plane in order to trigger guided mode.
            forceSendGuidedPoint(drone, drone.getGps().getPosition(),
                    getDroneAltConstrained(drone));
        }
        else if(Type.isRover(droneType)){
            droneState.changeFlightMode(ApmModes.ROVER_GUIDED);
        }
    }

	public void doGuidedTakeoff(Altitude alt) {
        if(Type.isCopter(myDrone.getType())) {
            coord = myDrone.getGps().getPosition();
            altitude.set(alt.valueInMeters());
            state = GuidedStates.IDLE;
            changeToGuidedMode(myDrone);
            MavLinkTakeoff.sendTakeoff(myDrone, alt);
            myDrone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
        }
	}

	public void newGuidedCoord(Coord2D coord) {
		changeCoord(coord);
	}

	public void changeGuidedAltitude(double alt) {
		changeAlt(alt);
	}

	public void forcedGuidedCoordinate(final Coord2D coord) throws Exception {
		if ((myDrone.getGps().getFixTypeNumeric() != GPS.LOCK_3D)) {
			throw new Exception("Bad GPS for guided");
		}

        if(isInitialized()) {
            changeCoord(coord);
        }
        else{
            mPostInitializationTask = new Runnable() {
                @Override
                public void run() {
                    changeCoord(coord);
                }
            };

            changeToGuidedMode(myDrone);
        }
	}

	private void initialize() {
		if (state == GuidedStates.UNINITIALIZED) {
			coord = myDrone.getGps().getPosition();
			altitude.set(getDroneAltConstrained(myDrone));
			state = GuidedStates.IDLE;
			myDrone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
		}

        if(mPostInitializationTask != null){
            mPostInitializationTask.run();
            mPostInitializationTask = null;
        }
	}

	private void disable() {
		state = GuidedStates.UNINITIALIZED;
		myDrone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
	}

    private void changeAlt(double alt) {
        switch (state) {
            case UNINITIALIZED:
                break;

            case IDLE:
                state = GuidedStates.ACTIVE;
                /** FALL THROUGH **/

            case ACTIVE:
                altitude.set(Math.max(alt, getMinAltitude(myDrone)));
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
            /** FALL THROUGH **/
		case ACTIVE:
			this.coord = coord;
			sendGuidedPoint();
			break;
		}
	}

	private void sendGuidedPoint() {
		if (state == GuidedStates.ACTIVE) {
            forceSendGuidedPoint(myDrone, coord, altitude.valueInMeters());
		}
	}

    public static void forceSendGuidedPoint(Drone drone, Coord2D coord, double altitudeInMeters){
        drone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
        MavLinkModes.setGuidedMode(drone, coord.getLat(), coord.getLng(), altitudeInMeters);
    }

	private static double getDroneAltConstrained(Drone drone) {
		double alt = Math.floor(drone.getAltitude().getAltitude());
		return Math.max(alt, getMinAltitude(drone));
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
	
	public boolean isIdle() {
		return (state == GuidedStates.IDLE);
	}

	public boolean isInitialized() {
		return !(state == GuidedStates.UNINITIALIZED);
	}

    public static float getMinAltitude(Drone drone){
        final int droneType = drone.getType();
        if(Type.isCopter(droneType)){
            return 2f;
        }
        else if(Type.isPlane(droneType)){
            return 15f;
        }
        else{
            return 0f;
        }
    }

    public void newGuidedVelocity( double xVel, double yVel, double zVel){
			MavLinkModes.sendGuidedVelocity(myDrone,xVel,yVel,zVel);
	}

}
