package com.droidplanner;

import java.util.List;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.MAVLink.Drone;
import com.MAVLink.WaypointMananger;
import com.MAVLink.WaypointMananger.OnWaypointManagerListner;
import com.MAVLink.waypoint;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.MAVLink.Messages.ardupilotmega.msg_request_data_stream;
import com.MAVLink.Messages.enums.MAV_DATA_STREAM;
import com.droidplanner.service.MAVLinkClient;
import com.droidplanner.service.MAVLinkClient.OnMavlinkClientListner;

public class DroidPlannerApp extends Application implements OnMavlinkClientListner, OnWaypointManagerListner {
	public Drone drone;
	public MAVLinkClient MAVClient;
	public WaypointMananger waypointMananger;
	
	public ConnectionStateListner conectionListner;
	private OnWaypointReceivedListner waypointsListner;
	
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
		Log.d("APP", "Created");
		drone = new Drone();
		MAVClient = new MAVLinkClient(this,this);
		waypointMananger = new WaypointMananger(MAVClient,this);
		
		MAVClient.init();
	}
	
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		try {
			MAVClient.onDestroy();	
		} catch (Exception e) {
		};
	}


	
	@Override
	public void notifyReceivedData(MAVLinkMessage msg) {
		drone.receiveData(msg);
		waypointMananger.processMessage(msg);
	}

	@Override
	public void notifyDisconnected() {
		conectionListner.notifyDisconnected();
	}

	@Override
	public void notifyConnected() {
		setupMavlinkStreamRate();
		conectionListner.notifyConnected();
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
		Log.d("PREF", stream_id +"  -  "+rate);
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
			drone.setHome(waypoints.get(0));
			waypoints.remove(0); // Remove Home waypoint
			drone.clearWaypoints();
			drone.addWaypoints(waypoints);
			waypointsListner.onWaypointsReceived();
		}
	}

	@Override
	public void onWriteWaypoints(msg_mission_ack msg) {
	}


	public void setConectionStateListner(ConnectionStateListner listner) {
		conectionListner = listner;		
	}
	
	public void setWaypointReceivedListner(OnWaypointReceivedListner listner){
		waypointsListner = listner;
	}

}
