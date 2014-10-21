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
			if (isGuidedMode()) {
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

    private boolean isGuidedMode(){
        final int droneType = myDrone.getType();
        final ApmModes droneMode = myDrone.getState().getMode();

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
			changeToGuidedMode();
		}else{
			newGuidedCoord(myDrone.getGps().getPosition());
		}
	}

    private void changeToGuidedMode(){
        final State droneState = myDrone.getState();
        final int droneType = myDrone.getType();
        if(Type.isCopter(droneType)){
            droneState.changeFlightMode(ApmModes.ROTOR_GUIDED);
        }
        else if(Type.isPlane(droneType)){
            droneState.changeFlightMode(ApmModes.FIXED_WING_GUIDED);
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
            changeToGuidedMode();
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

            changeToGuidedMode();
        }
	}

	private void initialize() {
		if (state == GuidedStates.UNINITIALIZED) {
			coord = myDrone.getGps().getPosition();
			altitude.set(getDroneAltConstrained());
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
			myDrone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
			MavLinkModes.setGuidedMode(myDrone, coord.getLat(), coord.getLng(),
					altitude.valueInMeters());
		}
	}

	private double getDroneAltConstrained() {
		double alt = Math.floor(myDrone.getAltitude().getAltitude());
		return Math.max(alt, getMinAltitude(myDrone));
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

}
