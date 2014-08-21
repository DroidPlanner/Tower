package org.droidplanner.core.gcs.follow;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.gcs.follow.FollowAlgorithm.FollowModes;
import org.droidplanner.core.gcs.location.Location;
import org.droidplanner.core.gcs.location.Location.LocationFinder;
import org.droidplanner.core.gcs.location.Location.LocationReceiver;
import org.droidplanner.core.gcs.roi.ROIEstimator;
import org.droidplanner.core.helpers.units.Length;

import com.MAVLink.Messages.ApmModes;

public class Follow implements OnDroneListener, LocationReceiver {

	private boolean followMeEnabled = false;
	private Drone drone;

	private ROIEstimator roiEstimator;
	private LocationFinder locationFinder;
	private FollowAlgorithm followAlgorithm;
	private TextNotificationReceiver notify;

	public Follow(Drone drone, Handler handler, LocationFinder locationFinder,
			TextNotificationReceiver notify) {
		this.drone = drone;
		this.notify = notify;
		followAlgorithm = new FollowAbove(drone, new Length(0.0));
		this.locationFinder = locationFinder;
		locationFinder.setLocationListner(this);
		roiEstimator = new ROIEstimator(handler, drone);
		drone.events.addDroneListener(this);
	}

	public void toggleFollowMeState() {
		if (isEnabled()) {
			disableFollowMe();
			drone.state.changeFlightMode(ApmModes.ROTOR_LOITER);
		} else {
			if (drone.MavClient.isConnected()) {
				if (drone.state.isArmed()) {
					drone.state.changeFlightMode(ApmModes.ROTOR_GUIDED);
					enableFollowMe();
				} else {
					notify.shortText("Drone Not Armed");
				}
			} else {
				notify.shortText("Drone Not Connected");
			}
		}
	}

	private void enableFollowMe() {
		drone.events.notifyDroneEvent(DroneEventsType.FOLLOW_START);
		notify.shortText("FollowMe Enabled");

		locationFinder.enableLocationUpdates();

		followMeEnabled = true;
	}

	private void disableFollowMe() {
		if (followMeEnabled) {
			notify.shortText("FollowMe Disabled");
			followMeEnabled = false;
		}
		locationFinder.disableLocationUpdates();
	}

	public boolean isEnabled() {
		return followMeEnabled;
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case MODE:
			if ((drone.state.getMode() != ApmModes.ROTOR_GUIDED)) {
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
		if(location.getAccuracy()<10.0){
			followAlgorithm.processNewLocation(location);
		}
		roiEstimator.onLocationChanged(location);
	}

	public void setType(FollowModes item) {
		followAlgorithm = item.getAlgorithmType(drone);
		drone.events.notifyDroneEvent(DroneEventsType.FOLLOW_CHANGE_TYPE);
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

	public interface TextNotificationReceiver {
		public void shortText(String notification);
	}
}
