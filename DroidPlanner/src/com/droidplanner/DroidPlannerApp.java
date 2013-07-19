package com.droidplanner;

import java.util.List;

import android.app.Application;
import android.widget.Toast;

import com.MAVLink.waypoint;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.droidplanner.MAVLink.MavLinkMsgHandler;
import com.droidplanner.MAVLink.MavLinkStreamRates;
import com.droidplanner.drone.Drone;
import com.droidplanner.helpers.FollowMe;
import com.droidplanner.helpers.RecordMe;
import com.droidplanner.helpers.TTS;
import com.droidplanner.parameters.Parameter;
import com.droidplanner.parameters.ParameterManager.OnParameterManagerListner;
import com.droidplanner.service.MAVLinkClient;
import com.droidplanner.service.MAVLinkClient.OnMavlinkClientListner;
import com.droidplanner.waypoints.WaypointMananger.OnWaypointManagerListner;

public class DroidPlannerApp extends Application implements OnMavlinkClientListner, OnWaypointManagerListner, OnParameterManagerListner {
	public Drone drone;
	public MAVLinkClient MAVClient;
	private MavLinkMsgHandler mavLinkMsgHandler;
	public FollowMe followMe;
	public RecordMe recordMe;
	public ConnectionStateListner conectionListner;
	public OnParameterManagerListner parameterListner; 
	private OnWaypointReceivedListner waypointsListner;
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
		drone = new Drone(tts,MAVClient,this);
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
		MavLinkStreamRates.setupMavlinkStreamRate(this);
		conectionListner.notifyConnected();
		tts.speak("Connected");
	}
	
	@Override
	public void onWaypointsReceived(List<waypoint> waypoints) {
		if (waypoints != null) {
			Toast.makeText(getApplicationContext(),
					"Waypoints received from Drone", Toast.LENGTH_SHORT).show();
			tts.speak("Waypoints received");
			drone.mission.setHome(waypoints.get(0));
			waypoints.remove(0); // Remove Home waypoint
			drone.mission.clearWaypoints();
			drone.mission.addWaypoints(waypoints);
			waypointsListner.onWaypointsReceived();
		}
	}

	@Override
	public void onWriteWaypoints(msg_mission_ack msg) {
		Toast.makeText(getApplicationContext(), "Waypoints sent",
				Toast.LENGTH_SHORT).show();
		tts.speak("Waypoints saved to Drone");
	}


	@Override
	public void onParametersReceived() {
		if (parameterListner != null) {
			parameterListner.onParametersReceived();			
		}
	}
	
	@Override
	public void onParameterReceived(Parameter parameter) {
		if (parameterListner != null) {
			parameterListner.onParameterReceived(parameter);			
		}
	}
	
	public void setConectionStateListner(ConnectionStateListner listner) {
		conectionListner = listner;		
	}
	
	public void setWaypointReceivedListner(OnWaypointReceivedListner listner){
		waypointsListner = listner;
	}
	
	public void setOnParametersChangedListner(OnParameterManagerListner listner){
		parameterListner = listner;
	}


}
