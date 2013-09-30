package com.droidplanner.MAVLink;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.MAVLink.Messages.ardupilotmega.msg_request_data_stream;
import com.MAVLink.Messages.enums.MAV_DATA_STREAM;
import com.droidplanner.DroidPlannerApp;
import com.droidplanner.service.MAVLinkClient;

public class MavLinkStreamRates {
	public static void setupStreamRatesFromPref(DroidPlannerApp droidPlannerApp) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(droidPlannerApp);

		int extendedStatus = Integer.parseInt(prefs.getString(
				"pref_mavlink_stream_rate_ext_stat", "0"));
		int extra1 = Integer.parseInt(prefs.getString(
				"pref_mavlink_stream_rate_extra1", "0"));
		int extra2 = Integer.parseInt(prefs.getString(
				"pref_mavlink_stream_rate_extra2", "0"));
		int extra3 = Integer.parseInt(prefs.getString(
				"pref_mavlink_stream_rate_extra3", "0"));
		int position = Integer.parseInt(prefs.getString(
				"pref_mavlink_stream_rate_position", "0"));
		int rcChannels = Integer.parseInt(prefs.getString(
				"pref_mavlink_stream_rate_rc_channels", "0"));
		int rawSensors = Integer.parseInt(prefs.getString(
				"pref_mavlink_stream_rate_raw_sensors", "0"));

		setupStreamRates(droidPlannerApp.drone.MavClient, extendedStatus,
				extra1, extra2, extra3, position, rcChannels, rawSensors);
	}

	private static void setupStreamRates(MAVLinkClient MAVClient,
			int extendedStatus, int extra1, int extra2, int extra3,
			int position, int rcChannels, int rawSensors) {
		requestMavlinkDataStream(MAVClient,
				MAV_DATA_STREAM.MAV_DATA_STREAM_EXTENDED_STATUS, extendedStatus);
		requestMavlinkDataStream(MAVClient,
				MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA1, extra1);
		requestMavlinkDataStream(MAVClient,
				MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA2, extra2);
		requestMavlinkDataStream(MAVClient,
				MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA3, extra3);
		requestMavlinkDataStream(MAVClient,
				MAV_DATA_STREAM.MAV_DATA_STREAM_POSITION, position);
		requestMavlinkDataStream(MAVClient,
				MAV_DATA_STREAM.MAV_DATA_STREAM_RAW_SENSORS, rawSensors);
		requestMavlinkDataStream(MAVClient,
				MAV_DATA_STREAM.MAV_DATA_STREAM_RC_CHANNELS, rcChannels);
	}

	private static void requestMavlinkDataStream(MAVLinkClient mAVClient,
			int stream_id, int rate) {
		msg_request_data_stream msg = new msg_request_data_stream();
		msg.target_system = 1;
		msg.target_component = 1;

		msg.req_message_rate = (short) rate;
		msg.req_stream_id = (byte) stream_id;

		if (rate > 0) {
			msg.start_stop = 1;
		} else {
			msg.start_stop = 0;
		}
		mAVClient.sendMavPacket(msg.pack());
	}
}
