package org.droidplanner.android.notifications;

import java.util.UUID;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;

import android.content.Context;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;


public class PebbleNotificationProvider implements
NotificationHandler.NotificationProvider {
	
	private static final int KEY_MODE = 0;
	private static final int KEY_FOLLOW_TYPE=1;
	private static final int KEY_TELEM = 2;
	
	private static final int KEY_PEBBLE_REQUEST = 100;
	private static final int KEY_REQUEST_MODE_FOLLOW = 101;
	private static final int KEY_REQUEST_CYCLE_FOLLOW_TYPE=102;
	private static final int KEY_REQUEST_MODE_LOITER=103;
	private static final int KEY_REQUEST_MODE_RTL=104;
	
	private static final UUID DP_UUID = UUID.fromString("79a2893d-fc7d-48c4-bc9a-34854d94ef6e");
	
	/**
	 * Application context.
	 */
	private Context applicationContext;
	
	long timeWhenLastTelemSent = System.currentTimeMillis();
	private PebbleDataReceiver datahandler;

	public PebbleNotificationProvider(Context context) {
		applicationContext = context.getApplicationContext();
		PebbleKit.startAppOnPebble(applicationContext, DP_UUID);
		datahandler = new PebbleKit.PebbleDataReceiver(DP_UUID) {
			
			@Override
			public void receiveData(Context context, int transactionId,
					PebbleDictionary data) {
				PebbleKit.sendAckToPebble(applicationContext, transactionId);
				Log.d("seB",data.toString());
				//decode
			}
		};
		PebbleKit.registerReceivedDataHandler(applicationContext,datahandler);
	}

	@Override
	public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
		switch (event) {
		case MODE:
			sendDataToWatchNow(drone);
			break;
		case BATTERY:
			sendDataToWatchIfTimeHasElapsed(drone);
			break;
		case SPEED:
			sendDataToWatchIfTimeHasElapsed(drone);
			break;
		default:
			break;

		}
	}
	
	/**
	 * Calls sendDataToWatchNow if and only if the timeout of 500ms has elapsed since last call to prevent DOSing the pebble.
	 * If not, the packet will be dropped.  If this packet is important (e.g. mode change), call sendDataToWatchNow directly.
	 * @param drone
	 */
	public void sendDataToWatchIfTimeHasElapsed(Drone drone) {
		if(System.currentTimeMillis() - timeWhenLastTelemSent > 500){
			sendDataToWatchNow(drone);
			timeWhenLastTelemSent = System.currentTimeMillis();
		}
	}
	
	/**
	 * Sends a full dictionary with updated information when called.
	 * If no pebble is present, the watchapp isn't installed, or the watchapp isn't running, nothing will happen.
	 * @param drone
	 */
    public void sendDataToWatchNow(Drone drone) {
        PebbleDictionary data = new PebbleDictionary();
        
        data.addString(KEY_MODE, drone.state.getMode().getName());
        data.addString(KEY_FOLLOW_TYPE, "Leash");
        
		String bat = "Bat:" + Double.toString(roundToOneDecimal(drone.battery.getBattVolt())) + "V";
		String speed = "Speed: " + Double.toString(roundToOneDecimal(drone.speed.getAirSpeed()));

		String telem = bat + "\n" + speed;
		data.addString(KEY_TELEM, telem);
		
        PebbleKit.sendDataToPebble(applicationContext, DP_UUID, data);
    }
    
	private double roundToOneDecimal(double value){
		return (double)Math.round(value * 10) / 10;
	}
}
