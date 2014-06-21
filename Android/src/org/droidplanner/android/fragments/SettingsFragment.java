package org.droidplanner.android.fragments;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.ConfigurationActivity;
import org.droidplanner.android.activities.helpers.MapPreferencesActivity;
import org.droidplanner.android.glass.utils.GlassUtils;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.core.bus.events.DroneDisconnectedEvent;
import org.droidplanner.core.bus.events.DroneHeartBeatEvent;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.android.utils.Constants;
import org.droidplanner.android.utils.file.DirectoryPath;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import java.util.HashSet;

import de.greenrobot.event.EventBus;

import static org.droidplanner.android.utils.Constants.*;

/**
 * Implements the application settings screen.
 */
public class SettingsFragment extends GlassPreferenceFragment implements
        OnSharedPreferenceChangeListener {

    /**
     * Used as tag for logging.
     */
    private final static String TAG = SettingsFragment.class.getSimpleName();

    /**
     * Keep track of which preferences' summary need to be updated.
     */
    private final HashSet<String> mDefaultSummaryPrefs = new HashSet<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        initSummaryPerPrefs();

        final Context context = getActivity().getApplicationContext();
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        final PreferenceScreen prefScreen = getPreferenceScreen();

        //Populate the drone settings category
        final PreferenceCategory dronePrefs = (PreferenceCategory) findPreference
                (Constants.PREF_DRONE_SETTINGS);
        if(dronePrefs != null){
            if (GlassUtils.isGlassDevice()) {
                //Remove the drone setup section from glass for now
                prefScreen.removePreference(dronePrefs);
            } else {
                dronePrefs.removeAll();

                final int configSectionsCount = ConfigurationActivity.sConfigurationFragments.length;
                for(int i = 0; i < configSectionsCount; i++){
                    final int index = i;
                    Preference configPref = new Preference(context);
                    configPref.setLayoutResource(R.layout.preference_config_screen);
                    configPref.setTitle(ConfigurationActivity.sConfigurationFragmentTitlesRes[i]);
                    configPref.setIcon(ConfigurationActivity.sConfigurationFragmentIconRes[i]);
                    configPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            //Launch the configuration activity to show the current config screen
                            final Intent configIntent = new Intent(context,
                                    ConfigurationActivity.class).putExtra(ConfigurationActivity
                                    .EXTRA_CONFIG_SCREEN_INDEX, index);

                            startActivity(configIntent);
                            return true;
                        }
                    });

                    dronePrefs.addPreference(configPref);
                }
            }
        }

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

        //update the summary for the preferences in the mDefaultSummaryPrefs hash table.
        for(String prefKey : mDefaultSummaryPrefs){
            final Preference pref = findPreference(prefKey);
            if(pref != null){
                pref.setSummary(sharedPref.getString(prefKey, ""));
            }
        }

        final String maxFlightPathSizeKey = getString(R.string.pref_max_flight_path_size_key);
        final Preference maxFlightPathSizePref = findPreference(maxFlightPathSizeKey);
        if(maxFlightPathSizePref != null){
            maxFlightPathSizePref.setSummary(sharedPref.getString(maxFlightPathSizeKey,
                    "") + " " + getString(R.string.set_to_zero_to_disable));
        }

        final String rcModeKey = getString(R.string.pref_rc_mode_key);
        final Preference rcModePref = findPreference(rcModeKey);
        if(rcModePref != null) {
            if (sharedPref.getString(rcModeKey, "MODE2").equalsIgnoreCase("MODE1")) {
                rcModePref.setSummary(getString(R.string.mode1_throttle_on_right_stick));
            }
            else {
                rcModePref.setSummary(getString(R.string.mode2_throttle_on_left_stick));
            }
        }

        final Preference storagePref = findPreference(getString(R.string.pref_storage_key));
        if(storagePref != null){
            storagePref.setSummary(DirectoryPath.getDroidPlannerPath());
        }

        try {
            Preference versionPref = findPreference("pref_version");
            if(versionPref != null) {
                String version = context.getPackageManager().getPackageInfo(
                        context.getPackageName(), 0).versionName;
                versionPref.setSummary(version);
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Unable to retrieve version name.", e);
        }

        updateMavlinkVersionPreference(null);
    }

    private void initSummaryPerPrefs(){
        mDefaultSummaryPrefs.clear();

        mDefaultSummaryPrefs.add(getString(R.string.pref_connection_type_key));
        mDefaultSummaryPrefs.add(getString(R.string.pref_baud_type_key));
        mDefaultSummaryPrefs.add(getString(R.string.pref_server_port_key));
        mDefaultSummaryPrefs.add(getString(R.string.pref_server_ip_key));
        mDefaultSummaryPrefs.add(getString(R.string.pref_udp_server_port_key));
        mDefaultSummaryPrefs.add(getString(R.string.pref_vehicle_type_key));
        mDefaultSummaryPrefs.add(getString(R.string.pref_rc_quickmode_left_key));
        mDefaultSummaryPrefs.add(getString(R.string.pref_rc_quickmode_right_key));
    }

    /**
     * This is used to update the mavlink version preference.
     * @param version mavlink version
     */
    private void updateMavlinkVersionPreference(String version){
        final Preference mavlinkVersionPref = findPreference(getString(R.string
                .pref_mavlink_version_key));
        if(mavlinkVersionPref != null){
            if(version == null){
                mavlinkVersionPref.setSummary(getString(R.string.empty_content));
            }
            else{
                mavlinkVersionPref.setSummary(version);
            }
        }
    }

    private boolean updateMapSettingsPreference(final String mapProviderName){
        final DPMapProvider mapProvider = DPMapProvider.getMapProvider(mapProviderName);
        if(mapProvider == null)
            return false;

        final Preference providerPrefs = findPreference(getText(R.string.pref_map_provider_settings_key));
        if(providerPrefs != null){
            providerPrefs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), MapPreferencesActivity.class)
                            .putExtra(MapPreferencesActivity.EXTRA_MAP_PROVIDER_NAME,
                                    mapProviderName));
                    return true;
                }
            });
        }
        return true;
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        final Preference preference = findPreference(key);
        if(preference == null){
            return;
        }

        if(mDefaultSummaryPrefs.contains(key)){
            preference.setSummary(sharedPreferences.getString(key, ""));
        }

        if (key.equals(getString(R.string.pref_max_flight_path_size_key))) {
            preference.setSummary(sharedPreferences.getString(key, "")
                    + " " + getString(R.string.set_to_zero_to_disable));
        }

        if (key.equals(getString(R.string.pref_vehicle_type_key))) {
            ((DroidPlannerApp) getActivity().getApplication()).drone.events
                    .notifyDroneEvent(DroneEventsType.TYPE);
        }

        if (key.equals(getString(R.string.pref_rc_mode_key))) {
            if (sharedPreferences.getString(key, "MODE2").equalsIgnoreCase("MODE1")) {
                preference.setSummary(R.string.mode1_throttle_on_right_stick);
            } else {
                preference.setSummary(R.string.mode2_throttle_on_left_stick);
            }
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onStop(){
        super.onStop();
        EventBus.getDefault().unregister(this);
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

    /*
    Event bus handler methods
     */

    /**
     * Handle update of the settings ui after a drone heartbeat event is received from the event
     * bus.
     * @param heartbeatEvent drone heartbeat event
     */
    public void onEventMainThread(DroneHeartBeatEvent heartbeatEvent){
        updateMavlinkVersionPreference(String.valueOf(heartbeatEvent.getHeartBeat()
                .mavlink_version));
    }

    /**
     * Handle update of the settings ui after a drone disconnected event is received from the
     * event bus.
     * @param event drone disconnected event
     */
    public void onEventMainThread(DroneDisconnectedEvent event){
        updateMavlinkVersionPreference(null);
    }
}
