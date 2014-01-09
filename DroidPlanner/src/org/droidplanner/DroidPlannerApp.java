package org.droidplanner;

import org.droidplanner.MAVLink.MavLinkMsgHandler;
import org.droidplanner.MAVLink.MavLinkStreamRates;
import org.droidplanner.drone.Drone;
import org.droidplanner.helpers.FollowMe;
import org.droidplanner.helpers.RecordMe;
import org.droidplanner.helpers.TTS;
import org.droidplanner.service.MAVLinkClient;
import org.droidplanner.service.MAVLinkClient.OnMavlinkClientListner;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;
import com.MAVLink.Messages.enums.MAV_MODE_FLAG;

public class DroidPlannerApp extends ErrorReportApp implements
		OnMavlinkClientListner {


	public Drone drone;
	private MavLinkMsgHandler mavLinkMsgHandler;
	public FollowMe followMe;
	public RecordMe recordMe;
	public ConnectionStateListner conectionListner;
	public OnSystemArmListener onSystemArmListener;
	private TTS tts;

    private HeartBeat Heartbeat;

	public interface ConnectionStateListner {
		public void notifyConnected();
		
		public void notifyDisconnected();
	}

	public interface OnSystemArmListener {
		public void notifyArmed();
		
		public void notifyDisarmed();
	}

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
		Heartbeat = new HeartBeat(tts);
	}

	@Override
	public void notifyReceivedData(MAVLinkMessage msg) {
		if(msg.msgid == msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT){
			msg_heartbeat msg_heart = (msg_heartbeat) msg;
			if((msg_heart.base_mode & (byte) MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED) == (byte) MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED){
				notifyArmed();
			}
			else {
				notifyDisarmed();
			}
			Heartbeat.onHeartbeat();
		}
		mavLinkMsgHandler.receiveData(msg);
	}

	@Override
	public void notifyDisconnected() {
		conectionListner.notifyDisconnected();

		// stop watchdog
		Heartbeat.watchdog.removeCallbacks(Heartbeat.watchdogCallback);
	}

	@Override
	public void notifyConnected() {
		MavLinkStreamRates.setupStreamRatesFromPref(this);
		conectionListner.notifyConnected();
		// don't announce 'connected' until first heartbeat received

		// start watchdog
		Heartbeat.notifyConnected();
	}

	@Override
	public void notifyArmed() {
		onSystemArmListener.notifyArmed();
	}

	@Override
	public void notifyDisarmed() {
		onSystemArmListener.notifyDisarmed();
	}

	@Override
	public void notifyTimeOut(int timeOutCount) {
		if (drone.waypointMananger.processTimeOut(timeOutCount)) {
			tts.speak("Retrying - " + String.valueOf(timeOutCount));
		} else {
			tts.speak("MAVLink has timed out");
		}
	}	
}
