package com.droidplanner;

import java.util.List;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.MAVLink.waypoint;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.MAVLink.Messages.ardupilotmega.msg_request_data_stream;
import com.MAVLink.Messages.enums.MAV_DATA_STREAM;
import com.droidplanner.MAVLink.Drone;
import com.droidplanner.MAVLink.MavLinkMsgHandler;
import com.droidplanner.MAVLink.parameters.Parameter;
import com.droidplanner.MAVLink.parameters.ParametersManager;
import com.droidplanner.MAVLink.parameters.ParametersManager.OnParameterManagerListner;
import com.droidplanner.MAVLink.waypoints.WaypointMananger;
import com.droidplanner.MAVLink.waypoints.WaypointMananger.OnWaypointManagerListner;
import com.droidplanner.helpers.TTS;
import com.droidplanner.service.MAVLinkClient;
import com.droidplanner.service.MAVLinkClient.OnMavlinkClientListner;

public class DroidPlannerApp extends Application implements OnMavlinkClientListner, OnWaypointManagerListner, OnParameterManagerListner {
	public Drone drone;
	public MAVLinkClient MAVClient;
	public WaypointMananger waypointMananger;
	public ParametersManager parameterMananger;
	private MavLinkMsgHandler mavLinkMsgHandler;
	
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
		drone = new Drone(tts);
		MAVClient = new MAVLinkClient(this,this);
		waypointMananger = new WaypointMananger(MAVClient,this);
		parameterMananger = new ParametersManager(MAVClient, this);
		mavLinkMsgHandler = new com.droidplanner.MAVLink.MavLinkMsgHandler(drone); 
	}
	
	
	@Override
	public void notifyReceivedData(MAVLinkMessage msg) {
		mavLinkMsgHandler.receiveData(msg);
		waypointMananger.processMessage(msg);
		parameterMananger.processMessage(msg);
	}

	@Override
	public void notifyDisconnected() {
		conectionListner.notifyDisconnected();
		tts.speak("Disconnected");
	}

	@Override
	public void notifyConnected() {
		setupMavlinkStreamRate();
		conectionListner.notifyConnected();
		tts.speak("Connected");
	}
	
	private void setupMavlinkStreamRate() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		requestMavlinkDataStream(
				MAV_DATA_STREAM.MAV_DATA_STREAM_EXTENDED_STATUS,
				Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_ext_stat",
						"0")));
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA1,
				Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_extra1",
						"0")));
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA2,
				Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_extra2",
						"0")));
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA3,
				Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_extra3",
						"0")));
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_POSITION,
				Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_position",
						"0")));
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_RAW_SENSORS,
				0);
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_RC_CHANNELS,
				0);
	}

	private void requestMavlinkDataStream(int stream_id, int rate) {
		msg_request_data_stream msg = new msg_request_data_stream();
		msg.target_system = 1;
		msg.target_component = 1;

		msg.req_message_rate = (short) rate;
		msg.req_stream_id = (byte) stream_id;

		if (rate>0){
			msg.start_stop = 1;
		}else{
			msg.start_stop = 0;
		}
		MAVClient.sendMavPacket(msg.pack());
	}

	@Override
	public void onWaypointsReceived(List<waypoint> waypoints) {
		if (waypoints != null) {
			Toast.makeText(getApplicationContext(),
					"Waypoints received from Drone", Toast.LENGTH_SHORT).show();
			tts.speak("Waypoints received");
			drone.setHome(waypoints.get(0));
			waypoints.remove(0); // Remove Home waypoint
			drone.clearWaypoints();
			drone.addWaypoints(waypoints);
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
	public void onParametersReceived(List<Parameter> parameters) {
		if (parameterListner != null) {
			parameterListner.onParametersReceived(parameters);			
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
