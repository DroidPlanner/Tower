package org.droidplanner.core.gcs.follow;

import org.droidplanner.core.MAVLink.MavLinkROI;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.drone.variables.State;
import org.droidplanner.core.gcs.follow.FollowAlgorithm.FollowModes;
import org.droidplanner.core.gcs.location.Location;
import org.droidplanner.core.gcs.location.Location.LocationFinder;
import org.droidplanner.core.gcs.location.Location.LocationReceiver;
import org.droidplanner.core.gcs.roi.ROIEstimator;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.model.Drone;

import com.MAVLink.Messages.ApmModes;

public class Follow implements OnDroneListener, LocationReceiver {
	static final String TAG = Follow.class.getSimpleName();

	private static final double MAX_SPEED = 15.0;

	/** Set of return value for the 'toggleFollowMeState' method.*/
	public enum FollowStates {
		FOLLOW_INVALID_STATE, FOLLOW_DRONE_NOT_ARMED, FOLLOW_DRONE_DISCONNECTED, FOLLOW_START, FOLLOW_RUNNING, FOLLOW_END,
	};

	private FollowStates state = FollowStates.FOLLOW_INVALID_STATE;
	private Drone drone;

	private ROIEstimator roiEstimator;
	private LocationFinder locationFinder;
	private FollowAlgorithm followAlgorithm;

	public Follow(Drone drone, Handler handler, LocationFinder locationFinder) {
		this.drone = drone;
		followAlgorithm = FollowAlgorithm.FollowModes.LEASH.getAlgorithmType(drone);
		this.locationFinder = locationFinder;
		locationFinder.setLocationListner(this);
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
					drone.getState().changeFlightMode(ApmModes.ROTOR_GUIDED);
					enableFollowMe();
				} else {
					state = FollowStates.FOLLOW_DRONE_NOT_ARMED;
				}
			} else {
				state = FollowStates.FOLLOW_DRONE_DISCONNECTED;
				
			}
		}
		return;
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
			drone.getGuidedPoint().pauseAtCurrentLocation();
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
			if ((drone.getState().getMode() != ApmModes.ROTOR_GUIDED)) {
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
		if (location.getAccuracy() < 10.0) {
			state = FollowStates.FOLLOW_RUNNING;

			// Filter out spurious location changes that may cause a fly-away
			if(location.hasTimeSinceLast() && location.hasDistanceToPrevious()) {
				double mps = location.getDistanceToPrevious() / (location.getTimeSinceLast() / 1000);

				if(mps < MAX_SPEED) {
					followAlgorithm.processNewLocation(location);
				}
			}
			else {
				followAlgorithm.processNewLocation(location);
			}


		}else{
			state = FollowStates.FOLLOW_START;
		}
		drone.notifyDroneEvent(DroneEventsType.FOLLOW_UPDATE);
		roiEstimator.onLocationChanged(location);
	}

	public void setType(FollowModes item) {
		followAlgorithm = item.getAlgorithmType(drone);
		drone.notifyDroneEvent(DroneEventsType.FOLLOW_CHANGE_TYPE);
	}

	public void changeRadius(double increment) {
		followAlgorithm.changeRadius(increment);
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
