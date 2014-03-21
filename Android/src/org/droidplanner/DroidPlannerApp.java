package org.droidplanner;

import org.droidplanner.MAVLink.MAVLinkStreams;
import org.droidplanner.MAVLink.MavLinkMsgHandler;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.Clock;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.Handler;
import org.droidplanner.drone.Preferences;
import org.droidplanner.helpers.DpPreferences;
import org.droidplanner.helpers.FollowMe;
import org.droidplanner.helpers.RecordMe;
import org.droidplanner.helpers.TTS;
import org.droidplanner.service.MAVLinkClient;

import android.os.SystemClock;

import com.MAVLink.Messages.MAVLinkMessage;

public class DroidPlannerApp extends ErrorReportApp implements
		MAVLinkStreams.MavlinkInputStream {
	public Drone drone;
	private MavLinkMsgHandler mavLinkMsgHandler;
	public FollowMe followMe;
	public RecordMe recordMe;
	public TTS tts;

	@Override
	public void onCreate() {
		super.onCreate();

		tts = new TTS(this);
		MAVLinkClient MAVClient = new MAVLinkClient(this,this);		
		Clock clock = new Clock() {
			@Override
			public long elapsedRealtime() {
				return SystemClock.elapsedRealtime();
			}
		};
		Handler handler = new Handler() {
			android.os.Handler handler = new android.os.Handler();
			
			@Override
			public void removeCallbacks(Runnable thread) {
				handler.removeCallbacks(thread);				
			}
			
			@Override
			public void postDelayed(Runnable thread, long timeout) {
				handler.postDelayed(thread, timeout);				
			}
		};
		Preferences pref = new DpPreferences(getApplicationContext());
		drone = new Drone(MAVClient,clock,handler,pref);
		drone.events.addDroneListener(tts);
		mavLinkMsgHandler = new org.droidplanner.MAVLink.MavLinkMsgHandler(
				drone);
	}

	@Override
	public void notifyReceivedData(MAVLinkMessage msg) {
		mavLinkMsgHandler.receiveData(msg);
	}

	@Override
	public void notifyConnected() {
		drone.events.notifyDroneEvent(DroneEventsType.CONNECTED);
	}

	@Override
	public void notifyDisconnected() {
		drone.events.notifyDroneEvent(DroneEventsType.DISCONNECTED);
	}
}
