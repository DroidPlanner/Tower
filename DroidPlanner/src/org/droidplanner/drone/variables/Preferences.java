package org.droidplanner.drone.variables;

import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneVariable;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences extends DroneVariable {

	public Preferences(Drone myDrone) {
		super(myDrone);
	}

	public String getVehicleType() {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(myDrone.context);
		return prefs.getString("pref_vehicle_type", null);
	}

	public Rates getRates() {
		Rates rates = new Rates();

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(myDrone.context);

		rates.extendedStatus = Integer.parseInt(prefs.getString(
				"pref_mavlink_stream_rate_ext_stat", "0"));
		rates.extra1 = Integer.parseInt(prefs.getString(
				"pref_mavlink_stream_rate_extra1", "0"));
		rates.extra2 = Integer.parseInt(prefs.getString(
				"pref_mavlink_stream_rate_extra2", "0"));
		rates.extra3 = Integer.parseInt(prefs.getString(
				"pref_mavlink_stream_rate_extra3", "0"));
		rates.position = Integer.parseInt(prefs.getString(
				"pref_mavlink_stream_rate_position", "0"));
		rates.rcChannels = Integer.parseInt(prefs.getString(
				"pref_mavlink_stream_rate_rc_channels", "0"));
		rates.rawSensors = Integer.parseInt(prefs.getString(
				"pref_mavlink_stream_rate_raw_sensors", "0"));
		rates.rawController = Integer.parseInt(prefs.getString(
				"pref_mavlink_stream_rate_raw_controller", "0"));
		return rates;
	}

	class Rates {
		int extendedStatus;
		int extra1;
		int extra2;
		int extra3;
		int position;
		int rcChannels;
		int rawSensors;
		int rawController;
	}
}
