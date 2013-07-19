package com.droidplanner.MAVLink;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.MAVLink.Messages.ardupilotmega.msg_request_data_stream;
import com.MAVLink.Messages.enums.MAV_DATA_STREAM;
import com.droidplanner.DroidPlannerApp;
import com.droidplanner.service.MAVLinkClient;

public class MavLinkStreamRates {
	public static void setupMavlinkStreamRate(DroidPlannerApp droidPlannerApp) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(droidPlannerApp);
	
		requestMavlinkDataStream(
				droidPlannerApp.MAVClient, MAV_DATA_STREAM.MAV_DATA_STREAM_EXTENDED_STATUS,
				Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_ext_stat",
						"0")));
		requestMavlinkDataStream(droidPlannerApp.MAVClient, MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA1,
				Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_extra1",
						"0")));
		requestMavlinkDataStream(droidPlannerApp.MAVClient, MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA2,
				Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_extra2",
						"0")));
		requestMavlinkDataStream(droidPlannerApp.MAVClient, MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA3,
				Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_extra3",
						"0")));
		requestMavlinkDataStream(droidPlannerApp.MAVClient, MAV_DATA_STREAM.MAV_DATA_STREAM_POSITION,
				Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_position",
						"0")));
		requestMavlinkDataStream(droidPlannerApp.MAVClient, MAV_DATA_STREAM.MAV_DATA_STREAM_RAW_SENSORS,
				0);
		requestMavlinkDataStream(droidPlannerApp.MAVClient, MAV_DATA_STREAM.MAV_DATA_STREAM_RC_CHANNELS,
				0);
	}
	
	public static void requestMavlinkDataStream(MAVLinkClient mAVClient, int stream_id, int rate) {
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
		mAVClient.sendMavPacket(msg.pack());
	}
}
