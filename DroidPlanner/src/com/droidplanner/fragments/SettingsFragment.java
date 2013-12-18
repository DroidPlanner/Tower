package com.droidplanner.fragments;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.droidplanner.file.DirectoryPath;

public class SettingsFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());

		findPreference("pref_connection_type").setSummary(
				sharedPref.getString("pref_connection_type", ""));
		findPreference("pref_baud_type").setSummary(
				sharedPref.getString("pref_baud_type", ""));
		findPreference("pref_max_fligth_path_size").setSummary(
				sharedPref.getString("pref_max_fligth_path_size", "")
						+ " "+getResources().getString(R.string.set_to_zero_to_disable));
		findPreference("pref_server_ip").setSummary(
				sharedPref.getString("pref_server_ip", ""));
		findPreference("pref_server_port").setSummary(
				sharedPref.getString("pref_server_port", ""));
		findPreference("pref_udp_server_port").setSummary(
				sharedPref.getString("pref_udp_server_port", ""));
		findPreference("pref_map_type").setSummary(
				sharedPref.getString("pref_map_type", ""));
		findPreference("pref_vehicle_type").setSummary(
                sharedPref.getString("pref_vehicle_type", ""));
		if (sharedPref.getString("pref_rc_mode", "MODE2").equalsIgnoreCase(
				"MODE1")) {
			findPreference("pref_rc_mode").setSummary(
					getResources().getString(R.string.mode1_throttle_on_right_stick));
		} else {
			findPreference("pref_rc_mode").setSummary(
					getResources().getString(R.string.mode2_throttle_on_left_stick));
		}
		findPreference("pref_rc_quickmode_left").setSummary(
				sharedPref.getString("pref_rc_quickmode_left", ""));
		findPreference("pref_rc_quickmode_right").setSummary(
				sharedPref.getString("pref_rc_quickmode_right", ""));

		findPreference("pref_storage").setSummary(
				DirectoryPath.getDroidPlannerPath());

		try {
			EditTextPreference versionPref = (EditTextPreference) findPreference("pref_version");
			String version = getActivity().getPackageManager().getPackageInfo(
					getActivity().getPackageName(), 0).versionName;
			versionPref.setSummary(version);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals("pref_connection_type")) {
			findPreference(key)
					.setSummary(sharedPreferences.getString(key, ""));
		}
		if (key.equals("pref_baud_type")) {
			findPreference(key)
					.setSummary(sharedPreferences.getString(key, ""));
		}
		if (key.equals("pref_max_fligth_path_size")) {
			findPreference(key).setSummary(
					sharedPreferences
							.getString("pref_max_fligth_path_size", "")
							+ " "+getResources().getString(R.string.set_to_zero_to_disable));
		}
		if (key.equals("pref_server_ip")) {
			findPreference(key)
					.setSummary(sharedPreferences.getString(key, ""));
		}
		if (key.equals("pref_server_port")) {
			findPreference(key)
					.setSummary(sharedPreferences.getString(key, ""));
		}
		if (key.equals("pref_map_type")) {
			findPreference(key)
					.setSummary(sharedPreferences.getString(key, ""));
			//((DroidPlannerApp) getActivity().getApplication()).drone.notifyMapTypeChanged();
		}
        if (key.equals("pref_vehicle_type")) {
			findPreference(key)
					.setSummary(sharedPreferences.getString(key, ""));
			((DroidPlannerApp) getActivity().getApplication()).drone.events.notifyDroneEvent(DroneEventsType.TYPE);
        }
		if (key.equals("pref_rc_mode")) {
			if (sharedPreferences.getString(key, "MODE2").equalsIgnoreCase(
					"MODE1")) {
				findPreference(key)
						.setSummary(R.string.mode1_throttle_on_right_stick);
			} else {
				findPreference(key).setSummary(R.string.mode2_throttle_on_left_stick);
			}
		}
		if (key.equals("pref_rc_quickmode_left")) {
			findPreference(key)
					.setSummary(sharedPreferences.getString(key, ""));
		}
		if (key.equals("pref_rc_quickmode_right")) {
			findPreference(key)
					.setSummary(sharedPreferences.getString(key, ""));
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

}
