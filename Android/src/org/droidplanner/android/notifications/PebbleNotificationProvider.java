package org.droidplanner.android.notifications;

import java.util.UUID;

import org.droidplanner.android.api.services.DroidPlannerApi;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.gcs.follow.FollowAlgorithm.FollowModes;
import org.droidplanner.core.model.Drone;

import android.content.Context;
import android.widget.Toast;

import com.MAVLink.Messages.ApmModes;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;

public class PebbleNotificationProvider implements NotificationHandler.NotificationProvider {

	private static final int KEY_MODE = 0;
	private static final int KEY_FOLLOW_TYPE = 1;
	private static final int KEY_TELEM = 2;
	private static final int KEY_APP_VERSION = 3;

	private static final UUID DP_UUID = UUID.fromString("79a2893d-fc7d-48c4-bc9a-34854d94ef6e");
	private static final String EXPECTED_APP_VERSION = "three";

	/**
	 * Application context.
	 */
	private final Context applicationContext;

    /**
     * Handle to the dp api
     */
    private final DroidPlannerApi dpApi;

	long timeWhenLastTelemSent = System.currentTimeMillis();
	private PebbleDataReceiver datahandler;

	public PebbleNotificationProvider(Context context, DroidPlannerApi dpApi) {
        this.dpApi = dpApi;
		applicationContext = context;
		PebbleKit.startAppOnPebble(applicationContext, DP_UUID);
		datahandler = new PebbleReceiverHandler(DP_UUID);
		PebbleKit.registerReceivedDataHandler(applicationContext, datahandler);
	}

    @Override
	public void onTerminate() {
		if (datahandler != null) {
			applicationContext.unregisterReceiver(datahandler);
			datahandler = null;
		}
	}

	@Override
	public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
		switch (event) {
		case CONNECTED:
			PebbleKit.startAppOnPebble(applicationContext, DP_UUID);
			break;
		case MODE:
			sendDataToWatchIfTimeHasElapsed(drone);
			break;
		case BATTERY:
			sendDataToWatchIfTimeHasElapsed(drone);
			break;
		case SPEED:
			sendDataToWatchIfTimeHasElapsed(drone);
			break;
		case FOLLOW_CHANGE_TYPE:
			sendDataToWatchIfTimeHasElapsed(drone);
			break;
		default:
			break;
		}
	}

	/**
	 * Calls sendDataToWatchNow if and only if the timeout of 500ms has elapsed
	 * since last call to prevent DOSing the pebble. If not, the packet will be
	 * dropped. If this packet is important (e.g. mode change), call
	 * sendDataToWatchNow directly.
	 * 
	 * @param drone
	 */
	public void sendDataToWatchIfTimeHasElapsed(Drone drone) {
		if (System.currentTimeMillis() - timeWhenLastTelemSent > 500) {
			sendDataToWatchNow(drone);
			timeWhenLastTelemSent = System.currentTimeMillis();
		}
	}

	/**
	 * Sends a full dictionary with updated information when called. If no
	 * pebble is present, the watchapp isn't installed, or the watchapp isn't
	 * running, nothing will happen.
	 * 
	 * @param drone
	 */
	public void sendDataToWatchNow(Drone drone) {
		Follow followMe = dpApi.getFollowMe();
		PebbleDictionary data = new PebbleDictionary();

		String mode = drone.getState().getMode().getName();
		if (!drone.getState().isArmed())
			mode = "Disarmed";
		else if (followMe.isEnabled() && "Guided".equals(mode))
			mode = "Follow";
		else if (drone.getGuidedPoint().isIdle() && !followMe.isEnabled() && "Guided".equals(mode))
			mode = "Paused";
		data.addString(KEY_MODE, mode);

		FollowModes type = followMe.getType();
		if (type != null) {
			data.addString(KEY_FOLLOW_TYPE, type.toString());
		} else
			data.addString(KEY_FOLLOW_TYPE, "none");

		String bat = "Bat:" + Double.toString(roundToOneDecimal(drone.getBattery().getBattVolt()))
				+ "V";
		String speed = "Speed: "
				+ Double.toString(roundToOneDecimal(drone.getSpeed().getAirSpeed()
						.valueInMetersPerSecond()));
		String altitude = "Alt: "
				+ Double.toString(roundToOneDecimal(drone.getAltitude().getAltitude()));
		String telem = bat + "\n" + altitude + "\n" + speed;
		data.addString(KEY_TELEM, telem);

		data.addString(KEY_APP_VERSION, EXPECTED_APP_VERSION);

		PebbleKit.sendDataToPebble(applicationContext, DP_UUID, data);
	}

	private double roundToOneDecimal(double value) {
		return (double) Math.round(value * 10) / 10;
	}

	public class PebbleReceiverHandler extends PebbleDataReceiver {

		private static final int KEY_PEBBLE_REQUEST = 100;
		private static final int KEY_REQUEST_MODE_FOLLOW = 101;
		private static final int KEY_REQUEST_CYCLE_FOLLOW_TYPE = 102;
		private static final int KEY_REQUEST_PAUSE = 103;
		private static final int KEY_REQUEST_MODE_RTL = 104;

		protected PebbleReceiverHandler(UUID id) {
			super(id);
		}

		@Override
		public void receiveData(Context context, int transactionId, PebbleDictionary data) {
			Follow followMe = dpApi.getFollowMe();
			PebbleKit.sendAckToPebble(applicationContext, transactionId);
			int request = (data.getInteger(KEY_PEBBLE_REQUEST).intValue());
			switch (request) {

			case KEY_REQUEST_MODE_FOLLOW:
				followMe.toggleFollowMeState();
				String eventLabel = null;
				switch (followMe.getState()) {
				case FOLLOW_START:
				case FOLLOW_RUNNING:
					eventLabel = "FollowMe enabled";
					break;

				case FOLLOW_END:
					eventLabel = "FollowMe disabled";
					break;

				case FOLLOW_INVALID_STATE:
					eventLabel = "FollowMe error: invalid state";
					break;

				case FOLLOW_DRONE_DISCONNECTED:
					eventLabel = "FollowMe error: drone not connected";
					break;

				case FOLLOW_DRONE_NOT_ARMED:
					eventLabel = "FollowMe error: drone not armed";
					break;
				}

				if (eventLabel != null) {
					Toast.makeText(applicationContext, eventLabel, Toast.LENGTH_SHORT).show();
				}
				break;

			case KEY_REQUEST_CYCLE_FOLLOW_TYPE:
				followMe.cycleType();
				break;

			case KEY_REQUEST_PAUSE:
				dpApi.getGuidedPoint().pauseAtCurrentLocation();
				break;

			case KEY_REQUEST_MODE_RTL:
				dpApi.getState().changeFlightMode(ApmModes.ROTOR_RTL);
				break;
			}
		}
	}
}
