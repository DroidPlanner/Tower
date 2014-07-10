package org.droidplanner.android.fragments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.ConfigurationActivity;
import org.droidplanner.android.activities.helpers.MapPreferencesActivity;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.notifications.NotificationHandler;
import org.droidplanner.android.utils.Constants;
import org.droidplanner.android.utils.file.DirectoryPath;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.variables.HeartBeat;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.common.io.Files;

/**
 * Implements the application settings screen.
 */
public class SettingsFragment extends DpPreferenceFragment implements
        OnSharedPreferenceChangeListener, DroneInterfaces.OnDroneListener {

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

        //Populate the map preference category
        final String mapsProvidersPrefKey = getString(R.string.pref_maps_providers_key);
        final ListPreference mapsProvidersPref = (ListPreference) findPreference
                (mapsProvidersPrefKey);
        if(mapsProvidersPref != null){
            //Grab the list of maps provider
            //TODO: enable full list of map providers when osm implementation is feature complete.
//            final DPMapProvider[] providers = DPMapProvider.values();
            final DPMapProvider[] providers = new DPMapProvider[]{DPMapProvider.GOOGLE_MAP};
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

        //Set the usage statistics preference
        final String usageStatKey = getString(R.string.pref_usage_statistics_key);
        final CheckBoxPreference usageStatPref = (CheckBoxPreference) findPreference(usageStatKey);
        if(usageStatPref != null){
            usageStatPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener
                    () {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    //Update the google analytics singleton.
                    final boolean optIn = (Boolean) newValue;
                    final GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
                    analytics.setAppOptOut(!optIn);
                    return true;
                }
            });
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
        
        /* Pebble Install Button.  When clicked, will check for pebble
         * if pebble is not present, error displayed.
         * If it is, the pbw (pebble bundle) will be copied from assets to external memory (makes sure to overwrite), and sends pbw intent for pebble app to install bundle.       
         */         
        Preference pebblePreference = findPreference(getString(R.string.pref_pebble_install_key));
        pebblePreference.setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
            public boolean onPreferenceClick( Preference pref )
            {
            	if(PebbleKit.isWatchConnected(context.getApplicationContext())){
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                      in = context.getAssets().open("Pebble/DroidPlanner.pbw");;
                      File outFile = new File(DirectoryPath.getDroidPlannerPath(), "DroidPlanner.pbw");
                      out = new FileOutputStream(outFile);
                      byte[] buffer = new byte[1024];
                      int read;
                      while((read = in.read(buffer)) != -1){
                        out.write(buffer, 0, read);
                      }
                      in.close();
                      in = null;
                      out.flush();
                      out.close();
                      out = null;
                      
                      Intent intent = new Intent(Intent.ACTION_VIEW);
                      intent.setData(Uri.fromFile(outFile));
                      intent.setClassName("com.getpebble.android", "com.getpebble.android.ui.UpdateActivity");
                      startActivity(intent);
                    } catch(IOException e) {
                        Log.e("pebble", "Failed to copy pbw asset", e);
                        Toast.makeText(context, "Failed to copy pbw asset", Toast.LENGTH_SHORT).show();
                    } catch(ActivityNotFoundException e){
                    	Log.e("pebble", "Pebble App Not installed", e);
                    	Toast.makeText(context, "Pebble App Not installed", Toast.LENGTH_SHORT).show();
                    }
            	}else{
            		Toast.makeText(context, "No Pebble Connected", Toast.LENGTH_SHORT).show();
            	}
            	return true;
            }
        });
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
                mavlinkVersionPref.setSummary('v' + version);
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
            ((DroidPlannerApp) getActivity().getApplication()).getDrone().events
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

        final Drone drone = ((DroidPlannerApp)getActivity().getApplication()).getDrone();
        final byte mavlinkVersion = drone.heartbeat.getMavlinkVersion();
        if(mavlinkVersion != HeartBeat.INVALID_MAVLINK_VERSION){
            updateMavlinkVersionPreference(String.valueOf(mavlinkVersion));
        }
        else{
            updateMavlinkVersionPreference(null);
        }

        drone.events.addDroneListener(this);
    }

    @Override
    public void onStop(){
        super.onStop();

        final Drone drone = ((DroidPlannerApp)getActivity().getApplication()).getDrone();
        drone.events.removeDroneListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDroneEvent(DroneEventsType event, Drone drone) {
        switch (event) {
            case DISCONNECTED:
                updateMavlinkVersionPreference(null);
                break;

            case HEARTBEAT_FIRST:
            case HEARTBEAT_RESTORED:
                updateMavlinkVersionPreference(String.valueOf(drone.heartbeat.getMavlinkVersion()));
                break;
        }
    }
}
