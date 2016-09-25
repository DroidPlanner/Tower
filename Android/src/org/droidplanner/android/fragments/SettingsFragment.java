package org.droidplanner.android.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Type;

import org.beyene.sius.unit.composition.speed.SpeedUnit;
import org.beyene.sius.unit.length.LengthUnit;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.R;
import org.droidplanner.android.activities.helpers.MapPreferencesActivity;
import org.droidplanner.android.fragments.widget.WidgetsListPrefFragment;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.utils.unit.UnitManager;
import org.droidplanner.android.utils.unit.providers.length.LengthUnitProvider;
import org.droidplanner.android.utils.unit.providers.speed.SpeedUnitProvider;
import org.droidplanner.android.utils.unit.systems.UnitSystem;

import java.util.HashSet;
import java.util.Locale;

/**
 * Implements the application settings screen.
 */
public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener,
        DroidPlannerApp.ApiListener {

    /**
     * Used as tag for logging.
     */
    private final static String TAG = SettingsFragment.class.getSimpleName();

    private static final String PACKAGE_NAME = Utils.PACKAGE_NAME;

    /**
     * Action used to broadcast updates to the period for the spoken status
     * summary.
     */
    public static final String ACTION_UPDATED_STATUS_PERIOD = PACKAGE_NAME + ".ACTION_UPDATED_STATUS_PERIOD";

    /**
     * Action used to broadcast updates to the gps hdop display preference.
     */
    public static final String ACTION_PREF_HDOP_UPDATE = PACKAGE_NAME + ".ACTION_PREF_HDOP_UPDATE";

    /**
     * Action used to broadcast updates to the unit system.
     */
    public static final String ACTION_PREF_UNIT_SYSTEM_UPDATE = PACKAGE_NAME + ".ACTION_PREF_UNIT_SYSTEM_UPDATE";

    /**
     * Used to retrieve the new period for the spoken status summary.
     */
    public static final String EXTRA_UPDATED_STATUS_PERIOD = "extra_updated_status_period";

    public static final String ACTION_LOCATION_SETTINGS_UPDATED = PACKAGE_NAME + ".action.LOCATION_SETTINGS_UPDATED";
    public static final String EXTRA_RESULT_CODE = "extra_result_code";

    public static final String ACTION_ADVANCED_MENU_UPDATED = PACKAGE_NAME + ".action.ADVANCED_MENU_UPDATED";

    /**
     * Used to notify of an update to the map rotation preference.
     */
    public static final String ACTION_MAP_ROTATION_PREFERENCE_UPDATED = PACKAGE_NAME +
            ".ACTION_MAP_ROTATION_PREFERENCE_UPDATED";

    public static final String ACTION_WIDGET_PREFERENCE_UPDATED = PACKAGE_NAME + ".ACTION_WIDGET_PREFERENCE_UPDATED";
    public static final String EXTRA_ADD_WIDGET = "extra_add_widget";
    public static final String EXTRA_WIDGET_PREF_KEY = "extra_widget_pref_key";

    private static final IntentFilter intentFilter = new IntentFilter();

    static {
        intentFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
        intentFilter.addAction(AttributeEvent.STATE_CONNECTED);
        intentFilter.addAction(AttributeEvent.STATE_UPDATED);
        intentFilter.addAction(AttributeEvent.HEARTBEAT_RESTORED);
        intentFilter.addAction(AttributeEvent.TYPE_UPDATED);
        intentFilter.addAction(ACTION_PREF_UNIT_SYSTEM_UPDATE);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Activity activity = getActivity();
            if(activity == null)
                return;

            final String action = intent.getAction();
            switch (action) {
                case AttributeEvent.STATE_DISCONNECTED:
                    updateFirmwareVersionPreference(null);
                    break;

                case AttributeEvent.STATE_CONNECTED:
                case AttributeEvent.TYPE_UPDATED:
                    Drone drone = dpApp.getDrone();
                    if (drone.isConnected()) {
                        Type droneType = drone.getAttribute(AttributeType.TYPE);
                        updateFirmwareVersionPreference(droneType);
                    } else
                        updateFirmwareVersionPreference(null);
                    break;

                case ACTION_PREF_UNIT_SYSTEM_UPDATE:
                    setupAltitudePreferences();
                    setupSpeedPreferences();
                    break;
            }
        }
    };

    /**
     * Keep track of which preferences' summary need to be updated.
     */
    private final HashSet<String> mDefaultSummaryPrefs = new HashSet<String>();

    private final Handler mHandler = new Handler();

    private DroidPlannerApp dpApp;
    private DroidPlannerPrefs dpPrefs;
    private LocalBroadcastManager lbm;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        dpApp = (DroidPlannerApp) activity.getApplication();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        initSummaryPerPrefs();

        final Context context = getActivity().getApplicationContext();
        dpPrefs = DroidPlannerPrefs.getInstance(context);
        lbm = LocalBroadcastManager.getInstance(context);
        final SharedPreferences sharedPref = dpPrefs.prefs;

        // update the summary for the preferences in the mDefaultSummaryPrefs hash table.
        for (String prefKey : mDefaultSummaryPrefs) {
            final Preference pref = findPreference(prefKey);
            if (pref != null) {
                pref.setSummary(sharedPref.getString(prefKey, ""));
            }
        }

        // Set the usage statistics preference
        final String usageStatKey = DroidPlannerPrefs.PREF_USAGE_STATISTICS;
        final CheckBoxPreference usageStatPref = (CheckBoxPreference) findPreference(usageStatKey);
        if (usageStatPref != null) {
            usageStatPref
                    .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            // Update the google analytics singleton.
                            final boolean optIn = (Boolean) newValue;
                            final GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
                            analytics.setAppOptOut(!optIn);
                            return true;
                        }
                    });
        }

        try {
            Preference versionPref = findPreference(DroidPlannerPrefs.PREF_APP_VERSION);
            if (versionPref != null) {
                String version = context.getPackageManager().getPackageInfo(
                        context.getPackageName(), 0).versionName;
                versionPref.setSummary(version);
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Unable to retrieve version name.", e);
        }

        setupWidgetsPreferences();
        setupMapProviders();
        setupPeriodicControls();
        setupAdvancedMenu();
        setupUnitSystemPreferences();
        setupImminentGroundCollisionWarningPreference();
        setupMapPreferences();
        setupAltitudePreferences();
        setupCreditsPage();
        setupSpeedPreferences();
    }

    private void setupWidgetsPreferences(){
        final Preference widgetsPref = findPreference(DroidPlannerPrefs.PREF_TOWER_WIDGETS);
        if(widgetsPref != null){
            widgetsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new WidgetsListPrefFragment().show(getFragmentManager(), "Widgets List Preferences");
                    return true;
                }
            });
        }
    }

    private void setupMapProviders(){
        // Populate the map preference category
        final String mapsProvidersPrefKey = DroidPlannerPrefs.PREF_MAPS_PROVIDERS;

        final ListPreference mapsProvidersPref = (ListPreference) findPreference(mapsProvidersPrefKey);
        if (mapsProvidersPref != null) {
            final DPMapProvider[] providers = DPMapProvider.getEnabledProviders();
            final int providersCount = providers.length;

            final CharSequence[] providersNames = new CharSequence[providersCount];
            final CharSequence[] providersNamesValues = new CharSequence[providersCount];
            for (int i = 0; i < providersCount; i++) {
                final String providerName = providers[i].name();
                providersNamesValues[i] = providerName;
                providersNames[i] = providerName.toLowerCase(Locale.ENGLISH).replace('_', ' ');
            }

            final String defaultProviderName = dpPrefs.getMapProviderName();

            mapsProvidersPref.setEntries(providersNames);
            mapsProvidersPref.setEntryValues(providersNamesValues);
            mapsProvidersPref.setValue(defaultProviderName);
            mapsProvidersPref.setSummary(defaultProviderName.toLowerCase(Locale.ENGLISH).replace('_', ' '));
            mapsProvidersPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    // Update the map provider settings preference.
                    final String mapProviderName = newValue.toString();
                    mapsProvidersPref.setSummary(mapProviderName.toLowerCase(Locale.ENGLISH).replace('_', ' '));
                    return updateMapSettingsPreference(mapProviderName);
                }
            });

            updateMapSettingsPreference(defaultProviderName);
        }
    }

    private void setupAdvancedMenu(){
        final CheckBoxPreference hdopToggle = (CheckBoxPreference) findPreference(DroidPlannerPrefs.PREF_SHOW_GPS_HDOP);
        if(hdopToggle !=  null) {
            hdopToggle.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    lbm.sendBroadcast(new Intent(ACTION_PREF_HDOP_UPDATE));
                    return true;
                }
            });
        }

        final CheckBoxPreference killSwitch = (CheckBoxPreference) findPreference(DroidPlannerPrefs.PREF_ENABLE_KILL_SWITCH);
        if(killSwitch != null) {
            killSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    lbm.sendBroadcast(new Intent(ACTION_ADVANCED_MENU_UPDATED));
                    return true;
                }
            });
        }
    }

    private void setupUnitSystemPreferences(){
        ListPreference unitSystemPref = (ListPreference) findPreference(DroidPlannerPrefs.PREF_UNIT_SYSTEM);
        if(unitSystemPref != null){
            int defaultUnitSystem = dpPrefs.getUnitSystemType();
            updateUnitSystemSummary(unitSystemPref, defaultUnitSystem);
            unitSystemPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int unitSystem = Integer.parseInt((String) newValue);
                    updateUnitSystemSummary(preference, unitSystem);
                    lbm.sendBroadcast(new Intent(ACTION_PREF_UNIT_SYSTEM_UPDATE));
                    return true;
                }
            });
        }
    }

    private void setupMapPreferences(){
        final CheckBoxPreference mapRotation = (CheckBoxPreference) findPreference(DroidPlannerPrefs.PREF_ENABLE_MAP_ROTATION);
        mapRotation.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                lbm.sendBroadcast(new Intent(ACTION_MAP_ROTATION_PREFERENCE_UPDATED));
                return true;
            }
        });
    }

    private void setupImminentGroundCollisionWarningPreference(){
        final CheckBoxPreference collisionWarn = (CheckBoxPreference) findPreference(DroidPlannerPrefs.PREF_WARNING_GROUND_COLLISION);
        if(collisionWarn != null){
            collisionWarn.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final boolean isEnabled = (Boolean) newValue;
                    if(!isEnabled){
                        lbm.sendBroadcast(new Intent(Drone.ACTION_GROUND_COLLISION_IMMINENT)
                                .putExtra(Drone.EXTRA_IS_GROUND_COLLISION_IMMINENT, false));
                    }
                    return true;
                }
            });
        }
    }

    private void updateUnitSystemSummary(Preference preference, int unitSystemType){
        final int summaryResId;
        switch(unitSystemType){
            case 0:
            default:
                summaryResId = R.string.unit_system_entry_auto;
                break;

            case 1:
                summaryResId = R.string.unit_system_entry_metric;
                break;

            case 2:
                summaryResId = R.string.unit_system_entry_imperial;
                break;
        }

        preference.setSummary(summaryResId);
    }

    private void setupAltitudePreferences(){
        setupAltitudePreferenceHelper(DroidPlannerPrefs.PREF_ALT_MAX_VALUE, dpPrefs.getMaxAltitude());
        setupAltitudePreferenceHelper(DroidPlannerPrefs.PREF_ALT_MIN_VALUE, dpPrefs.getMinAltitude());
        setupAltitudePreferenceHelper(DroidPlannerPrefs.PREF_ALT_DEFAULT_VALUE, dpPrefs.getDefaultAltitude());
    }

    private void setupSpeedPreferences() {
        final SpeedUnitProvider sup = getSpeedUnitProvider();

        final EditTextPreference defaultSpeedPref = (EditTextPreference) findPreference(DroidPlannerPrefs.PREF_VEHICLE_DEFAULT_SPEED);
        if (defaultSpeedPref != null) {
            final SpeedUnit defaultValue = sup.boxBaseValueToTarget(dpPrefs.getVehicleDefaultSpeed());

            defaultSpeedPref.setText(String.format(Locale.US, "%2.1f", defaultValue.getValue()));
            defaultSpeedPref.setSummary(defaultValue.toString());

            defaultSpeedPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final Context context = getContext();
                    try {
                        final double speedValue = Double.parseDouble(newValue.toString());

                        final SpeedUnitProvider sup = getSpeedUnitProvider();
                        final SpeedUnit newSpeedValue = sup.boxTargetValue(speedValue);
                        final double speedPrefValue = sup.fromTargetToBase(newSpeedValue).getValue();

                        defaultSpeedPref.setText(String.format(Locale.US, "%2.1f", newSpeedValue.getValue()));
                        defaultSpeedPref.setSummary(newSpeedValue.toString());

                        dpPrefs.setVehicleDefaultSpeed((float) speedPrefValue);

                    }catch(NumberFormatException e) {
                        if (context != null) {
                            Toast.makeText(context, R.string.warning_invalid_speed, Toast.LENGTH_LONG).show();
                        }
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public Context getContext() {
        final Activity activity = getActivity();
        if (activity == null)
            return null;

        return activity.getApplicationContext();
    }

    private void setupAltitudePreferenceHelper(final String prefKey, double defaultAlt){
        final LengthUnitProvider lup = getLengthUnitProvider();

        final EditTextPreference altPref = (EditTextPreference) findPreference(prefKey);
        if(altPref != null){
            final LengthUnit defaultAltValue = lup.boxBaseValueToTarget(defaultAlt);

            altPref.setText(String.format(Locale.US, "%2.1f", defaultAltValue.getValue()));
            altPref.setSummary(defaultAltValue.toString());

            altPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final Context context = getContext();

                    try {
                        final double altValue = Double.parseDouble(newValue.toString());

                        final LengthUnitProvider lup = getLengthUnitProvider();
                        final LengthUnit newAltValue = lup.boxTargetValue(altValue);

                        final double altPrefValue = lup.fromTargetToBase(newAltValue).getValue();

                        final double maxAltValue = dpPrefs.getMaxAltitude();
                        final double minAltValue = dpPrefs.getMinAltitude();
                        final double defaultAltValue = dpPrefs.getDefaultAltitude();

                        final String key = preference.getKey();
                        boolean isValueInvalid = false;
                        String valueUpdateMsg = "";
                        switch(key){
                            case DroidPlannerPrefs.PREF_ALT_MIN_VALUE:
                                //Compare the new altitude value with the max altitude value

                                valueUpdateMsg = "Min altitude updated!";
                                if(altPrefValue > defaultAltValue){
                                    isValueInvalid = true;
                                    valueUpdateMsg = "Min altitude cannot be greater than the default altitude";
                                }
                                else if(altPrefValue > maxAltValue){
                                    isValueInvalid = true;
                                    valueUpdateMsg = "Min altitude cannot be greater than the max altitude";
                                }
                                break;

                            case DroidPlannerPrefs.PREF_ALT_MAX_VALUE:
                                valueUpdateMsg = "Max altitude updated!";
                                if(altPrefValue < defaultAltValue){
                                    isValueInvalid = true;
                                    valueUpdateMsg = "Max altitude cannot be less than the default altitude";
                                }
                                else if(altPrefValue < minAltValue){
                                    isValueInvalid = true;
                                    valueUpdateMsg = "Max altitude cannot be less than the min altitude";
                                }
                                break;

                            case DroidPlannerPrefs.PREF_ALT_DEFAULT_VALUE:
                                valueUpdateMsg = "Default altitude updated!";
                                if(altPrefValue > maxAltValue){
                                    isValueInvalid = true;
                                    valueUpdateMsg = "Default altitude cannot be greater than the max altitude";
                                }
                                else if(altPrefValue < minAltValue){
                                    isValueInvalid = true;
                                    valueUpdateMsg = "Default altitude cannot be less than the min altitude";
                                }
                                break;
                        }

                        if(!isValueInvalid){
                            altPref.setText(String.format(Locale.US, "%2.1f", newAltValue.getValue()));
                            altPref.setSummary(newAltValue.toString());

                            dpPrefs.setAltitudePreference(prefKey, altPrefValue);
                        }

                        if(context != null){
                            Toast.makeText(context, valueUpdateMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (NumberFormatException e) {
                        if(context != null){
                            Toast.makeText(context, R.string.warning_invalid_altitude, Toast.LENGTH_LONG).show();
                        }
                    }
                    return false;
                }
            });
        }
    }

    private LengthUnitProvider getLengthUnitProvider(){
        final UnitSystem unitSystem = UnitManager.getUnitSystem(getActivity().getApplicationContext());
        return unitSystem.getLengthUnitProvider();
    }

    private SpeedUnitProvider getSpeedUnitProvider() {
        final UnitSystem unitSystem = UnitManager.getUnitSystem(getActivity().getApplicationContext());
        return unitSystem.getSpeedUnitProvider();
    }

    private void initSummaryPerPrefs() {
        mDefaultSummaryPrefs.clear();

        mDefaultSummaryPrefs.add(DroidPlannerPrefs.PREF_USB_BAUD_RATE);
        mDefaultSummaryPrefs.add(DroidPlannerPrefs.PREF_TCP_SERVER_PORT);
        mDefaultSummaryPrefs.add(DroidPlannerPrefs.PREF_TCP_SERVER_IP);
        mDefaultSummaryPrefs.add(DroidPlannerPrefs.PREF_UDP_SERVER_PORT);
        mDefaultSummaryPrefs.add(DroidPlannerPrefs.PREF_UDP_PING_RECEIVER_IP);
        mDefaultSummaryPrefs.add(DroidPlannerPrefs.PREF_UDP_PING_RECEIVER_PORT);
    }

    private void updateFirmwareVersionPreference(Type droneType) {
        String firmwareVersion = droneType == null ? null : droneType.getFirmwareVersion();

        final Preference vehicleTypePref = findPreference(DroidPlannerPrefs.PREF_VEHICLE_TYPE);
        if(vehicleTypePref != null){
            if(droneType == null){
                vehicleTypePref.setSummary(R.string.empty_content);
            }
            else{
                final int typeLabelResId;
                switch(droneType.getDroneType()){
                    case Type.TYPE_COPTER:
                        typeLabelResId = R.string.label_type_copter;
                        break;

                    case Type.TYPE_PLANE:
                        typeLabelResId = R.string.label_type_plane;
                        break;

                    case Type.TYPE_ROVER:
                        typeLabelResId = R.string.label_type_rover;
                        break;

                    case Type.TYPE_UNKNOWN:
                    default:
                        typeLabelResId = R.string.label_type_unknown;
                        break;
                }

                vehicleTypePref.setSummary(typeLabelResId);
            }
        }

        final Preference firmwareVersionPref = findPreference(DroidPlannerPrefs.PREF_FIRMWARE_VERSION);
        if (firmwareVersionPref != null) {
            final HitBuilders.EventBuilder firmwareEvent = new HitBuilders.EventBuilder()
                    .setCategory(GAUtils.Category.MAVLINK_CONNECTION);

            if (firmwareVersion == null) {
                firmwareVersionPref.setSummary(R.string.empty_content);
                firmwareEvent.setAction("Firmware version unset");
            } else {
                firmwareVersionPref.setSummary(firmwareVersion);
                firmwareEvent.setAction("Firmware version set").setLabel(firmwareVersion);
            }

            // Record the firmware version.
            GAUtils.sendEvent(firmwareEvent);
        }
    }

    private boolean updateMapSettingsPreference(final String mapProviderName) {
        final DPMapProvider mapProvider = DPMapProvider.getMapProvider(mapProviderName);
        if (mapProvider == null)
            return false;

        final Preference providerPrefs = findPreference(DroidPlannerPrefs.PREF_MAPS_PROVIDER_SETTINGS);
        if (providerPrefs != null) {
            providerPrefs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), MapPreferencesActivity.class).putExtra(
                            MapPreferencesActivity.EXTRA_MAP_PROVIDER_NAME, mapProviderName));
                    return true;
                }
            });
        }
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        final Preference preference = findPreference(key);
        if (preference == null) {
            return;
        }

        if (mDefaultSummaryPrefs.contains(key)) {
            preference.setSummary(sharedPreferences.getString(key, ""));
        }
    }

    private void setupPeriodicControls() {
        final PreferenceCategory periodicSpeechPrefs = (PreferenceCategory) findPreference(DroidPlannerPrefs.PREF_TTS_PERIODIC);
        ListPreference periodic = ((ListPreference) periodicSpeechPrefs.getPreference(0));
        periodic.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, final Object newValue) {
                // Broadcast the event locally on update.
                // A handler is used to that the current action has the time to
                // return, and store the value in the preferences.
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        lbm.sendBroadcast(new Intent(ACTION_UPDATED_STATUS_PERIOD)
                                .putExtra(EXTRA_UPDATED_STATUS_PERIOD, (String) newValue));

                        setupPeriodicControls();
                    }
                });
                return true;
            }
        });

        int val = Integer.parseInt(periodic.getValue());

        final boolean isEnabled = val != 0;
        if (isEnabled) {
            periodic.setSummary(getString(R.string.pref_tts_status_every) + " " + val + " "
                    + getString(R.string.pref_tts_seconds));
        } else {
            periodic.setSummary(R.string.pref_tts_periodic_status_disabled);
        }

        for (int i = 1; i < periodicSpeechPrefs.getPreferenceCount(); i++) {
            periodicSpeechPrefs.getPreference(i).setEnabled(isEnabled);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        dpApp.addApiListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        dpApp.removeApiListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                this);
    }

    @Override
    public void onApiConnected() {
        Drone drone = dpApp.getDrone();
        Type droneType = drone.getAttribute(AttributeType.TYPE);

        updateFirmwareVersionPreference(droneType);

        lbm.registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onApiDisconnected() {
        lbm.unregisterReceiver(broadcastReceiver);
    }

    private void setupCreditsPage() {
        Preference creatorPref = findPreference(DroidPlannerPrefs.PREF_PROJECT_CREATOR);
        if(creatorPref != null) {
            creatorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    openWebUrl("https://github.com/arthurbenemann");
                    return true;
                }
            });
        }

        Preference leadMaintainerPref = findPreference(DroidPlannerPrefs.PREF_PROJECT_LEAD_MAINTAINER);
        if (leadMaintainerPref != null) {
            leadMaintainerPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    openWebUrl("https://github.com/ne0fhyk");
                    return true;
                }
            });
        }

        Preference contributorsPref = findPreference(DroidPlannerPrefs.PREF_PROJECT_CONTRIBUTORS);
        if (contributorsPref != null) {
            contributorsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    openWebUrl("https://github.com/DroidPlanner/Tower/graphs/contributors");
                    return true;
                }
            });
        }
    }

    private void openWebUrl(String url) {
        try {
            Intent browseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browseIntent);
        }catch(ActivityNotFoundException e) {
            Toast.makeText(getContext(), R.string.warning_unable_to_open_web_url, Toast.LENGTH_LONG).show();
        }
    }
}
