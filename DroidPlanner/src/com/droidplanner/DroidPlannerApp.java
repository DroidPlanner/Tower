package com.droidplanner;

import android.app.Application;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;
import com.MAVLink.Messages.enums.MAV_MODE_FLAG;
import com.droidplanner.MAVLink.MavLinkMsgHandler;
import com.droidplanner.MAVLink.MavLinkStreamRates;
import com.droidplanner.drone.Drone;
import com.droidplanner.helpers.FollowMe;
import com.droidplanner.helpers.RecordMe;
import com.droidplanner.helpers.TTS;
import com.droidplanner.service.MAVLinkClient;
import com.droidplanner.service.MAVLinkClient.OnMavlinkClientListner;

public class DroidPlannerApp extends Application implements
		OnMavlinkClientListner {
	public Drone drone;
	private MavLinkMsgHandler mavLinkMsgHandler;
	public FollowMe followMe;
	public RecordMe recordMe;
	public SuperActConnectionStateListner saConnectionListner;
	public SuperActOnSystemArmListener saOnSystemArmListener;
	private TTS tts;

	public interface OnWaypointUpdateListner {
		public void onWaypointsUpdate();
	}

	public interface SuperActConnectionStateListner {
		public void saNotifyConnected();		
		public void saNotifyDisconnected();
	}

	public interface SuperActOnSystemArmListener {
		public void saNotifyArmed();		
		public void saNotifyDisarmed();
	}

	@Override
	public void onCreate() {
		super.onCreate();

		tts = new TTS(this);
		MAVLinkClient MAVClient = new MAVLinkClient(this, this);
		drone = new Drone(tts, MAVClient, getApplicationContext());
		followMe = new FollowMe(this, drone);
		recordMe = new RecordMe(this, drone);
		mavLinkMsgHandler = new com.droidplanner.MAVLink.MavLinkMsgHandler(
				drone);
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
		}
		mavLinkMsgHandler.receiveData(msg);
	}

	@Override
	public void notifyDisconnected() {
		saConnectionListner.saNotifyDisconnected();
		tts.speak("Disconnected");
	}

	@Override
	public void notifyConnected() {
		MavLinkStreamRates.setupStreamRatesFromPref(this);
		saConnectionListner.saNotifyConnected();
		tts.speak("Connected");
	}

	@Override
	public void notifyArmed() {
		saOnSystemArmListener.saNotifyArmed();
	}

	@Override
	public void notifyDisarmed() {
		saOnSystemArmListener.saNotifyDisarmed();
	}

	@Override
	public void notifyTimeOut(int timeOutCount) {
		// TODO Auto-generated method stub
		
	}
}
