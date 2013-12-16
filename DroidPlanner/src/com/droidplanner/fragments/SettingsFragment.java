package com.droidplanner.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.droidplanner.file.DirectoryPath;

import static com.droidplanner.utils.Constants.*;

public class SettingsFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

        final Context context = getActivity().getApplicationContext();
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences
                (context);

        //Mavlink preferences
        final CheckBoxPreference btRelayServerSwitch = (CheckBoxPreference) findPreference
                (PREF_BLUETOOTH_RELAY_SERVER_TOGGLE);
        if(btRelayServerSwitch != null){
            boolean defaultValue = sharedPref.getBoolean
                    (PREF_BLUETOOTH_RELAY_SERVER_TOGGLE,
                            DEFAULT_BLUETOOTH_RELAY_SERVER_TOGGLE);
            btRelayServerSwitch.setChecked(defaultValue);
            btRelayServerSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    //Broadcast the preference update
                    context.sendBroadcast(new Intent(ACTION_BLUETOOTH_RELAY_SERVER)
                    .putExtra(EXTRA_BLUETOOTH_RELAY_SERVER_ENABLED, (Boolean)newValue));
                    return true;
                }
            });
        }

		findPreference("pref_connection_type").setSummary(
				sharedPref.getString("pref_connection_type", ""));
		findPreference("pref_baud_type").setSummary(
				sharedPref.getString("pref_baud_type", ""));
		findPreference("pref_max_fligth_path_size").setSummary(
				sharedPref.getString("pref_max_fligth_path_size", "")
						+ " (set to zero to disable).");
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
					"Mode1: Throttle on RIGHT stick");
		} else {
			findPreference("pref_rc_mode").setSummary(
					"Mode2: Throttle on LEFT stick");
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
							+ " (set to zero to disable).");
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
						.setSummary("Mode1: Throttle on RIGHT stick");
			} else {
				findPreference(key).setSummary("Mode2: Throttle on LEFT stick");
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
