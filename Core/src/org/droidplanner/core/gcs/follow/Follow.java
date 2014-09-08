package org.droidplanner.core.gcs.follow;

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

	// Set of return value for the 'toggleFollowMeState' method.
	public static final int FOLLOW_INVALID_STATE = -1;
	public static final int FOLLOW_DRONE_NOT_ARMED = -2;
	public static final int FOLLOW_DRONE_DISCONNECTED = -3;
	public static final int FOLLOW_START = 0;
	public static final int FOLLOW_END = 1;

	private boolean followMeEnabled = false;
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

	public int toggleFollowMeState() {
		final State droneState = drone.getState();
		if (droneState == null) {
			return FOLLOW_INVALID_STATE;
		}

		if (isEnabled()) {
			disableFollowMe();
			drone.getState().changeFlightMode(ApmModes.ROTOR_LOITER);
			return FOLLOW_END;
		} else {
			if (drone.getMavClient().isConnected()) {
				if (drone.getState().isArmed()) {
					drone.getState().changeFlightMode(ApmModes.ROTOR_GUIDED);
					enableFollowMe();
					return FOLLOW_START;
				} else {
					return FOLLOW_DRONE_NOT_ARMED;
				}
			} else {
				return FOLLOW_DRONE_DISCONNECTED;
			}
		}
	}

	private void enableFollowMe() {
		locationFinder.enableLocationUpdates();
		followMeEnabled = true;
		drone.notifyDroneEvent(DroneEventsType.FOLLOW_START);
	}

	private void disableFollowMe() {
		locationFinder.disableLocationUpdates();
		if (followMeEnabled) {
			followMeEnabled = false;
			drone.notifyDroneEvent(DroneEventsType.FOLLOW_STOP);
		}
	}

	public boolean isEnabled() {
		return followMeEnabled;
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
			followAlgorithm.processNewLocation(location);
		}
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
}
