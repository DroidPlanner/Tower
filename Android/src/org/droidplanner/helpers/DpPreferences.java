package org.droidplanner.helpers;

import org.droidplanner.drone.profiles.VehicleProfile;
import org.droidplanner.drone.variables.Type.FirmwareType;
import org.droidplanner.file.IO.VehicleProfileReader;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class DpPreferences implements org.droidplanner.drone.Preferences  {

	private Context context;

	public DpPreferences(Context context) {
		this.context = context;
	}

	@Override
	public String getVehicleType() {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs.getString("pref_vehicle_type", null);
	}

	@Override
	public Rates getRates() {
		Rates rates = new Rates();

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

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

	@Override
	public VehicleProfile loadVehicleProfile(FirmwareType firmwareType) {
		return VehicleProfileReader.load(context, firmwareType);
	}
}
