package com.droidplanner;

import android.app.Application;

import com.MAVLink.Messages.MAVLinkMessage;
import com.droidplanner.MAVLink.MavLinkMsgHandler;
import com.droidplanner.MAVLink.MavLinkStreamRates;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces;
import com.droidplanner.helpers.FollowMe;
import com.droidplanner.helpers.RecordMe;
import com.droidplanner.helpers.TTS;
import com.droidplanner.service.MAVLinkClient;
import com.droidplanner.service.MAVLinkClient.OnMavlinkClientListner;

public class DroidPlannerApp extends Application implements OnMavlinkClientListner {
	public Drone drone;
	public MAVLinkClient MAVClient;
	private MavLinkMsgHandler mavLinkMsgHandler;
	public FollowMe followMe;
	public RecordMe recordMe;
	public ConnectionStateListner conectionListner;
	private TTS tts;
	
	public interface OnWaypointReceivedListner{
		public void onWaypointsReceived();
	}
	
	public interface ConnectionStateListner{
		public void notifyConnected();
		public void notifyDisconnected();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		tts = new TTS(this);
		MAVClient = new MAVLinkClient(this,this);
		drone = new Drone(tts,MAVClient,getApplicationContext());
		followMe = new FollowMe(MAVClient, this,drone);
		recordMe = new RecordMe(MAVClient, this,drone);
		mavLinkMsgHandler = new com.droidplanner.MAVLink.MavLinkMsgHandler(drone);
	}
	
	
	@Override
	public void notifyReceivedData(MAVLinkMessage msg) {
		mavLinkMsgHandler.receiveData(msg);
	}

	@Override
	public void notifyDisconnected() {
		conectionListner.notifyDisconnected();
		tts.speak("Disconnected");
	}

	@Override
	public void notifyConnected() {
		MavLinkStreamRates.setupStreamRatesFromPref(this);
		conectionListner.notifyConnected();
		tts.speak("Connected");
	}

	public void setConectionStateListner(ConnectionStateListner listner) {
		conectionListner = listner;		
	}
	
	public void setWaypointReceivedListner(OnWaypointReceivedListner listner){
		drone.mission.waypointsListner = listner;
	}
	
	public void setOnParametersChangedListner(DroneInterfaces.OnParameterManagerListner listner){
		drone.parameterMananger.parameterListner = listner;
	}


}
