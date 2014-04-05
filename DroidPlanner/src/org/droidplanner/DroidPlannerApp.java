package org.droidplanner;

import org.droidplanner.MAVLink.MavLinkMsgHandler;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.helpers.FollowMe;
import org.droidplanner.helpers.RecordMe;
import org.droidplanner.helpers.TTS;
import org.droidplanner.service.MAVLinkClient;
import org.droidplanner.service.MAVLinkClient.OnMavlinkClientListener;

import com.MAVLink.Messages.MAVLinkMessage;

public class DroidPlannerApp extends ErrorReportApp implements
		OnMavlinkClientListener {
	public Drone drone;
	private MavLinkMsgHandler mavLinkMsgHandler;
	public FollowMe followMe;
	public RecordMe recordMe;
	private TTS tts;
	private boolean mConnectedToDrone = false;

	@Override
	public void onCreate() {
		super.onCreate();

		tts = new TTS(this);
		MAVLinkClient MAVClient = new MAVLinkClient(this, this);
		drone = new Drone(tts, MAVClient, getApplicationContext());
		followMe = new FollowMe(this, drone);
		recordMe = new RecordMe(this, drone);
		mavLinkMsgHandler = new org.droidplanner.MAVLink.MavLinkMsgHandler(
				drone);
	}

	@Override
	public void notifyReceivedData(MAVLinkMessage msg) {
		mavLinkMsgHandler.receiveData(msg);
	}

	@Override
	public void notifyTimeOut(int timeOutCount) {
		if (drone.waypointMananger.processTimeOut(timeOutCount)) {
			tts.speak("Retrying - " + String.valueOf(timeOutCount));
		} else {
			tts.speak("MAVLink has timed out");
		}
	}

	@Override
	public void notifyConnected() {
		mConnectedToDrone = true;
		drone.events.notifyDroneEvent(DroneEventsType.CONNECTED);
	}

	@Override
	public void notifyDisconnected() {
		mConnectedToDrone = false;
		drone.events.notifyDroneEvent(DroneEventsType.DISCONNECTED);
	}
	
	public Drone getDrone() { 
		return drone; 
	}
	
	public boolean isDroneConnected() {
		return mConnectedToDrone;
	}
}
