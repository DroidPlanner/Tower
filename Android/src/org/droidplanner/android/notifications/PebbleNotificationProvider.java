package org.droidplanner.android.notifications;

import java.util.UUID;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;

import android.content.Context;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;



public class PebbleNotificationProvider implements
NotificationHandler.NotificationProvider {
	
	private static final int KEY_MODE = 0;
	private static final int KEW_FOLLOW_TYPE=1;
	private static final int KEY_TELEM = 2;
	
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
			sendDataToWatch(KEY_MODE, drone.state.getMode().getName());
			break;
		case BATTERY:
			sendDataToWatch(KEY_TELEM, getTelemetryToSend(drone) );
			break;
		case SPEED:
			sendDataToWatch(KEY_TELEM, getTelemetryToSend(drone));
			break;
		default:
			break;

		}
	}
	
	private String getTelemetryToSend(Drone drone){
		String bat = "Bat:" + Double.toString(roundToOneDecimal(drone.battery.getBattVolt())) + "V";
		String speed = "Speed: " + Double.toString(roundToOneDecimal(drone.speed.getAirSpeed()));
		return bat + "\n" + speed;
		
	}
	private double roundToOneDecimal(double value){
		return (double)Math.round(value * 10) / 10;
	}
	
    public void sendDataToWatch(int id, String str) {
        // Build up a Pebble dictionary containing the weather icon and the current temperature in degrees celsius
        PebbleDictionary data = new PebbleDictionary();
        
        data.addString(id, str);

        // Send the assembled dictionary to the weather watch-app; this is a no-op if the app isn't running or is not
        // installed
        PebbleKit.sendDataToPebble(mContext.getApplicationContext(), DP_UUID, data);
        Log.d("seB","sending data: "+str);
    }
}
