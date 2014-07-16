package org.droidplanner.android.gcs.follow;

import org.droidplanner.android.gcs.follow.FollowAlgorithm.FollowModes;
import org.droidplanner.android.gcs.location.FusedLocation;
import org.droidplanner.android.gcs.location.LocationFinder;
import org.droidplanner.android.gcs.location.LocationReceiver;
import org.droidplanner.core.MAVLink.MavLinkROI;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.helpers.units.Length;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.MAVLink.Messages.ApmModes;

public class Follow implements OnDroneListener, LocationReceiver {

	private Context context;
	private boolean followMeEnabled = false;
	private Drone drone;

	private LocationFinder locationFinder;
	private FollowAlgorithm followAlgorithm;

	public Follow(Context context, Drone drone) {
		this.context = context;
		this.drone = drone;
		followAlgorithm = new FollowLeash(drone, new Length(5.0));
		locationFinder = new FusedLocation(context,this);
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
					Toast.makeText(context, "Drone Not Armed", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(context, "Drone Not Connected", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void enableFollowMe() {
		drone.events.notifyDroneEvent(DroneEventsType.FOLLOW_START);
		Log.d("follow", "enable");
		Toast.makeText(context, "FollowMe Enabled", Toast.LENGTH_SHORT).show();
		
		locationFinder.enableLocationUpdates();

		followMeEnabled = true;
	}

	private void disableFollowMe() {
		if (followMeEnabled) {
			Toast.makeText(context, "FollowMe Disabled", Toast.LENGTH_SHORT).show();
			followMeEnabled = false;
			Log.d("follow", "disable");
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
		MavLinkROI.setROI(drone, new Coord3D(location.getLatitude(), location.getLongitude(),
				new Altitude(0.0)));
		followAlgorithm.processNewLocation(location);
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
}
