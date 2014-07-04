package org.droidplanner.android.notifications;

import java.util.UUID;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;

import android.content.Context;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;



public class PebbleNotificationProvider implements
NotificationHandler.NotificationProvider {
	
	private static final int KEY_MODE = 0;
	private static final int KEW_FOLLOW_TYPE=1;
	private static final int KEY_TELEM_1 = 2;
	private static final int KEY_TELEM_2 = 3;
	private static final int KEY_TELEM_3 = 4;
	private static final int KEY_TELEM_4 = 5;
	
	private static final UUID DP_UUID = UUID.fromString("79a2893d-fc7d-48c4-bc9a-34854d94ef6e");
	/**
	 * Application context.
	 */
	private Context mContext;

	public PebbleNotificationProvider(Context context) {
		mContext = context;
	}

	@Override
	public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
		switch (event) {
		case MODE:
			sendDataToWatch(KEY_MODE, "seb");
			break;
		case BATTERY:
			//send stuff
			break;
		case SPEED:
			//send stuff
			break;
		default:
			break;

		}
	}
	
    public void sendDataToWatch(int id, String str) {
        // Build up a Pebble dictionary containing the weather icon and the current temperature in degrees celsius
        PebbleDictionary data = new PebbleDictionary();
        
        data.addString(id, str);

        // Send the assembled dictionary to the weather watch-app; this is a no-op if the app isn't running or is not
        // installed
        PebbleKit.sendDataToPebble(mContext.getApplicationContext(), DP_UUID, data);
    }
}
