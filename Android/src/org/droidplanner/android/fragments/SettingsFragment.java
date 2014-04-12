package org.droidplanner.android.fragments;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.ConfigurationActivity;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.android.utils.Constants;
import org.droidplanner.android.utils.file.DirectoryPath;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

/**
 * Implements the application settings screen.
 */
public class SettingsFragment extends PreferenceFragment implements
        OnSharedPreferenceChangeListener {

    /**
     * Used as tag for logging.
     */
    private final static String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        final Context context = getActivity().getApplicationContext();
        final Resources res = getResources();
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        // Populate the drone settings category
        final PreferenceCategory dronePrefs = (PreferenceCategory) findPreference(Constants
                .PREF_DRONE_SETTINGS);
        if (dronePrefs != null) {
            dronePrefs.removeAll();

            final int configSectionsCount = ConfigurationActivity.sConfigurationFragments.length;
            for (int i = 0; i < configSectionsCount; i++) {
                final int index = i;
                Preference configPref = new Preference(context);
                configPref.setLayoutResource(R.layout.preference_config_screen);
                configPref.setTitle(ConfigurationActivity.sConfigurationFragmentTitlesRes[i]);
                configPref.setIcon(ConfigurationActivity.sConfigurationFragmentIconRes[i]);
                configPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener
                        () {
                    @Override
                    public boolean onPreferenceClick(
                            Preference preference) {
                        // Launch the configuration activity to show the
                        // current config screen
                        final Intent configIntent = new Intent(context, ConfigurationActivity.class)
                                .putExtra(ConfigurationActivity.EXTRA_CONFIG_SCREEN_INDEX,
                                        index);

                        startActivity(configIntent);
                        return true;
                    }
                });

                dronePrefs.addPreference(configPref);
            }
        }

        //Populate the app settings category
        findPreference("pref_connection_type").setSummary(sharedPref.getString
                ("pref_connection_type", ""));
        findPreference("pref_baud_type").setSummary(sharedPref.getString("pref_baud_type", ""));
        findPreference("pref_max_flight_path_size").setSummary(sharedPref
                .getString("pref_max_flight_path_size", "") + " "
                + getString(R.string.set_to_zero_to_disable));
        findPreference("pref_server_ip").setSummary(sharedPref.getString("pref_server_ip", ""));
        findPreference("pref_server_port").setSummary(sharedPref.getString("pref_server_port", ""));
        findPreference("pref_udp_server_port").setSummary(sharedPref.getString
                ("pref_udp_server_port", ""));
        findPreference("pref_map_type").setSummary(sharedPref.getString("pref_map_type", ""));
        findPreference("pref_vehicle_type").setSummary(sharedPref.getString("pref_vehicle_type",
                ""));

        if (sharedPref.getString("pref_rc_mode", "MODE2").equalsIgnoreCase("MODE1")) {
            findPreference("pref_rc_mode").setSummary(getString(R.string
                    .mode1_throttle_on_right_stick));
        } else {
            findPreference("pref_rc_mode").setSummary(getString(R.string
                    .mode2_throttle_on_left_stick));
        }

        findPreference("pref_rc_quickmode_left").setSummary(sharedPref.getString
                ("pref_rc_quickmode_left", ""));
        findPreference("pref_rc_quickmode_right").setSummary(sharedPref.getString
                ("pref_rc_quickmode_right", ""));

        findPreference("pref_storage").setSummary(DirectoryPath.getDroidPlannerPath());

        //Populate the map preference category
        final String mapsProvidersPrefKey = getString(R.string.pref_maps_providers_key);
        final ListPreference mapsProvidersPref = (ListPreference) findPreference
                (mapsProvidersPrefKey);
        if(mapsProvidersPref != null){
            //Grab the list of maps provider
            final DPMapProvider[] providers = DPMapProvider.values();
            final int providersCount = providers.length;
            final CharSequence[] providersNames = new CharSequence[providersCount];
            final CharSequence[] providersNamesValues = new CharSequence[providersCount];
            for(int i = 0; i < providersCount; i++){
                final String providerName = providers[i].name();
                providersNamesValues[i] = providerName;
                providersNames[i] = providerName.toLowerCase().replace('_', ' ');
            }

            final String defaultProviderName = sharedPref.getString(mapsProvidersPrefKey,
                    DPMapProvider.DEFAULT_MAP_PROVIDER.name());

            mapsProvidersPref.setEntries(providersNames);
            mapsProvidersPref.setEntryValues(providersNamesValues);
            mapsProvidersPref.setValue(defaultProviderName);
            mapsProvidersPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    //Update the map provider settings preference.
                    final String mapProviderName = newValue.toString();
                    return updateMapSettingsPreference(mapProviderName);
                }
            });

            updateMapSettingsPreference(defaultProviderName);
        }

        try {
            EditTextPreference versionPref = (EditTextPreference) findPreference("pref_version");
            String version = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
            versionPref.setSummary(version);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Unable to retrieve version name.", e);
        }
    }

    private boolean updateMapSettingsPreference(String mapProviderName){
        final DPMapProvider mapProvider = DPMapProvider.getMapProvider(mapProviderName);
        if(mapProvider == null)
            return false;

        final PreferenceScreen providerPrefs = (PreferenceScreen)findPreference
                (getText(R.string.pref_map_provider_settings_key));
        if(providerPrefs != null){
            providerPrefs.removeAll();

            final Preference[] providersPrefsSet = mapProvider.getMapPreferences(getActivity());
            for(Preference providerPref: providersPrefsSet) {
                providerPrefs.addPreference(providerPref);
            }
        }
        return true;
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_connection_type")) {
            findPreference(key).setSummary(sharedPreferences.getString(key, ""));
        }

        if (key.equals("pref_baud_type")) {
            findPreference(key).setSummary(sharedPreferences.getString(key, ""));
        }

        if (key.equals("pref_max_flight_path_size")) {
            findPreference(key).setSummary(sharedPreferences.getString("pref_max_flight_path_size" +
                    "", "")
                    + " " + getString(R.string.set_to_zero_to_disable));
        }

        if (key.equals("pref_server_ip")) {
            findPreference(key).setSummary(sharedPreferences.getString(key, ""));
        }

        if (key.equals("pref_server_port")) {
            findPreference(key).setSummary(sharedPreferences.getString(key, ""));
        }

        if (key.equals("pref_map_type")) {
            findPreference(key).setSummary(sharedPreferences.getString(key, ""));
            // ((DroidPlannerApp)
            // getActivity().getApplication()).drone.notifyMapTypeChanged();
        }

        if (key.equals("pref_vehicle_type")) {
            findPreference(key).setSummary(sharedPreferences.getString(key, ""));
            ((DroidPlannerApp) getActivity().getApplication()).drone.events
                    .notifyDroneEvent(DroneEventsType.TYPE);
        }

        if (key.equals("pref_rc_mode")) {
            if (sharedPreferences.getString(key, "MODE2").equalsIgnoreCase("MODE1")) {
                findPreference(key).setSummary(R.string.mode1_throttle_on_right_stick);
            } else {
                findPreference(key).setSummary(R.string.mode2_throttle_on_left_stick);
            }
        }

        if (key.equals("pref_rc_quickmode_left")) {
            findPreference(key).setSummary(sharedPreferences.getString(key, ""));
        }

        if (key.equals("pref_rc_quickmode_right")) {
            findPreference(key).setSummary(sharedPreferences.getString(key, ""));
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
