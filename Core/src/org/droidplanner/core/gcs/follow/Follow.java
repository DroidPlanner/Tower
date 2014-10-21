package org.droidplanner.core.gcs.follow;

import org.droidplanner.core.MAVLink.MavLinkROI;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.drone.variables.State;
import org.droidplanner.core.drone.variables.Type;
import org.droidplanner.core.gcs.follow.FollowAlgorithm.FollowModes;
import org.droidplanner.core.gcs.location.Location;
import org.droidplanner.core.gcs.location.Location.LocationFinder;
import org.droidplanner.core.gcs.location.Location.LocationReceiver;
import org.droidplanner.core.gcs.roi.ROIEstimator;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.model.Drone;

import com.MAVLink.Messages.ApmModes;

public class Follow implements OnDroneListener, LocationReceiver {

	/** Set of return value for the 'toggleFollowMeState' method.*/
	public enum FollowStates {
		FOLLOW_INVALID_STATE, FOLLOW_DRONE_NOT_ARMED, FOLLOW_DRONE_DISCONNECTED, FOLLOW_START, FOLLOW_RUNNING, FOLLOW_END
	}

	private FollowStates state = FollowStates.FOLLOW_INVALID_STATE;
	private Drone drone;

	private ROIEstimator roiEstimator;
	private LocationFinder locationFinder;
	private FollowAlgorithm followAlgorithm;

	public Follow(Drone drone, Handler handler, LocationFinder locationFinder) {
		this.drone = drone;
		followAlgorithm = FollowAlgorithm.FollowModes.LEASH.getAlgorithmType(drone);
		this.locationFinder = locationFinder;
		locationFinder.setLocationListener(this);
		roiEstimator = new ROIEstimator(handler, drone);
		drone.addDroneListener(this);
	}

	public void toggleFollowMeState() {
		final State droneState = drone.getState();
		if (droneState == null) {
			state = FollowStates.FOLLOW_INVALID_STATE;
			return;
		}

		if (isEnabled()) {
			disableFollowMe();
		} else {
			if (drone.getMavClient().isConnected()) {
				if (drone.getState().isArmed()) {
					changeToGuidedMode();
					enableFollowMe();
				} else {
					state = FollowStates.FOLLOW_DRONE_NOT_ARMED;
				}
			} else {
				state = FollowStates.FOLLOW_DRONE_DISCONNECTED;
				
			}
		}
	}

    private void changeToGuidedMode(){
        final State droneState = drone.getState();
        final int droneType = drone.getType();
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

    private boolean isGuidedMode(){
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

	private void enableFollowMe() {
		locationFinder.enableLocationUpdates();
		state = FollowStates.FOLLOW_START;
		drone.notifyDroneEvent(DroneEventsType.FOLLOW_START);
	}

	private void disableFollowMe() {
		locationFinder.disableLocationUpdates();
		if (isEnabled()) {
			state = FollowStates.FOLLOW_END;
			MavLinkROI.resetROI(drone);

            if(isGuidedMode()) {
                drone.getGuidedPoint().pauseAtCurrentLocation();
            }

			drone.notifyDroneEvent(DroneEventsType.FOLLOW_STOP);
		}
	}

	public boolean isEnabled() {
		return state == FollowStates.FOLLOW_RUNNING || state == FollowStates.FOLLOW_START;
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case MODE:
			if (!isGuidedMode()) {
				disableFollowMe();
			}
			break;

		case DISCONNECTED:
			disableFollowMe();
			break;
		default:
		}
	}

	public Length getRadius() {
		return followAlgorithm.radius;
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location.isAccurate()) {
			state = FollowStates.FOLLOW_RUNNING;
            followAlgorithm.processNewLocation(location);
            roiEstimator.onLocationChanged(location);
		}
		else {
			state = FollowStates.FOLLOW_START;
		}

			drone.notifyDroneEvent(DroneEventsType.FOLLOW_UPDATE);
	}

	public void setType(FollowModes item) {
		followAlgorithm = item.getAlgorithmType(drone);
		drone.notifyDroneEvent(DroneEventsType.FOLLOW_CHANGE_TYPE);
	}

	public void changeRadius(double radius) {
		followAlgorithm.changeRadius(radius);
	}

	public void cycleType() {
		setType(followAlgorithm.getType().next());
	}

	public FollowModes getType() {
		return followAlgorithm.getType();
	}

	public FollowStates getState() {
		return state;
	}
}
