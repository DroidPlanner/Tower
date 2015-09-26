package org.droidplanner.android.utils.prefs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.o3dr.services.android.lib.drone.connection.ConnectionType;

import org.droidplanner.android.fragments.widget.TowerWidgets;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.unit.systems.UnitSystem;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides structured access to Droidplanner preferences
 * <p/>
 * Over time it might be good to move the various places that are doing
 * prefs.getFoo(blah, default) here - to collect prefs in one place and avoid
 * duplicating string constants (which tend to become stale as code evolves).
 * This is called the DRY (don't repeat yourself) principle of software
 * development.
 */
public class DroidPlannerPrefs {

    /*
     * Default preference value
     */
    public static final String PREF_USAGE_STATISTICS = "pref_usage_statistics";
    public static final boolean DEFAULT_USAGE_STATISTICS = true;

    public static final String PREF_CONNECTION_TYPE = "pref_connection_param_type";
    public static final String DEFAULT_CONNECTION_TYPE = String.valueOf(ConnectionType.TYPE_USB);

    private static final String PREF_KEEP_SCREEN_ON = "pref_keep_screen_bright";
    private static final boolean DEFAULT_KEEP_SCREEN_ON = false;

    public static final String PREF_MAPS_PROVIDERS = "pref_maps_providers_key";
    private static final String DEFAULT_MAPS_PROVIDER = DPMapProvider.DEFAULT_MAP_PROVIDER.name();

    public static final String PREF_MAPS_PROVIDER_SETTINGS = "pref_map_provider_settings";

    private static final AutoPanMode DEFAULT_AUTO_PAN_MODE = AutoPanMode.DISABLED;

    private static final String PREF_UI_LANGUAGE = "pref_ui_language_english";
    public static final boolean DEFAULT_PREF_UI_LANGUAGE = false;

    private static final String PREF_SPEECH_PERIOD = "tts_periodic_status_period";
    public static final String DEFAULT_SPEECH_PERIOD = "0";

    public static final String PREF_MAX_ALT_WARNING = "pref_max_alt_warning";
    public static final boolean DEFAULT_MAX_ALT_WARNING = false;

    public static final String PREF_TTS_LOST_SIGNAL = "tts_lost_signal";
    public static final boolean DEFAULT_TTS_WARNING_LOST_SIGNAL = true;

    public static final String PREF_TTS_LOW_SIGNAL = "tts_low_signal";
    public static final boolean DEFAULT_TTS_WARNING_LOW_SIGNAL = false;

    public static final String PREF_TTS_AUTOPILOT_WARNING = "tts_autopilot_warning";
    public static final boolean DEFAULT_TTS_WARNING_AUTOPILOT_WARNING = true;

    public static final String PREF_USB_BAUD_RATE = "pref_baud_type";
    private static final String DEFAULT_USB_BAUD_RATE = "57600";

    public static final String PREF_TCP_SERVER_IP = "pref_server_ip";
    private static final String DEFAULT_TCP_SERVER_IP = "192.168.40.100";

    public static final String PREF_TCP_SERVER_PORT = "pref_server_port";
    private static final String DEFAULT_TCP_SERVER_PORT = "5763";

    public static final String PREF_UDP_PING_RECEIVER_IP = "pref_udp_ping_receiver_ip";
    public static final String PREF_UDP_PING_RECEIVER_PORT = "pref_udp_ping_receiver_port";

    public static final String PREF_UDP_SERVER_PORT = "pref_udp_server_port";
    private static final String DEFAULT_UDP_SERVER_PORT = "14550";

    public static final String PREF_UNIT_SYSTEM = "pref_unit_system";
    private static final int DEFAULT_UNIT_SYSTEM = UnitSystem.AUTO;

    public static final String PREF_WARNING_GROUND_COLLISION = "pref_ground_collision_warning";
    private static final boolean DEFAULT_WARNING_GROUND_COLLISION = false;

    public static final String PREF_ENABLE_MAP_ROTATION = "pref_map_enable_rotation";
    private static final boolean DEFAULT_ENABLE_MAP_ROTATION = true;

    public static final String PREF_ENABLE_KILL_SWITCH = "pref_enable_kill_switch";
    private static final boolean DEFAULT_ENABLE_KILL_SWITCH = false;

    public static final String PREF_ENABLE_UDP_PING = "pref_enable_udp_server_ping";
    private static final boolean DEFAULT_ENABLE_UDP_PING = false;

    public static final String PREF_ALT_MAX_VALUE = "pref_alt_max_value";
    private static final double DEFAULT_MAX_ALT = 200; //meters

    public static final String PREF_ALT_MIN_VALUE = "pref_alt_min_value";
    private static final double DEFAULT_MIN_ALT = 0; // meter

    public static final String PREF_ALT_DEFAULT_VALUE = "pref_alt_default_value";
    private static final double DEFAULT_ALT = 20; // meters

    public static final String PREF_APP_VERSION = "pref_version";

    private static final String PREF_IS_TTS_ENABLED = "pref_enable_tts";
    private static final boolean DEFAULT_TTS_ENABLED = false;

    private static final String PREF_BT_DEVICE_NAME = "pref_bluetooth_device_name";
    public static final String PREF_BT_DEVICE_ADDRESS = "pref_bluetooth_device_address";

    public static final String PREF_FIRMWARE_VERSION = "pref_firmware_version";
    public static final String PREF_VEHICLE_TYPE = "pref_vehicle_type";

    public static final String PREF_SHOW_GPS_HDOP = "pref_ui_gps_hdop";
    public static final boolean DEFAULT_SHOW_GPS_HDOP = false;

    private static final String PREF_UI_REALTIME_FOOTPRINTS = "pref_ui_realtime_footprints_key";
    private static final boolean DEFAULT_UI_REALTIME_FOOTPRINTS = false;

    private static final String PREF_DSHARE_USERNAME = "dshare_username";
    private static final String DEFAULT_DSHARE_USERNAME = "";

    private static final String PREF_DSHARE_PASSWORD = "dshare_password";
    private static final String DEFAULT_DSHARE_PASSWORD = "";

    private static final String PREF_LIVE_UPLOAD_ENABLED = "pref_live_upload_enabled";
    private static final boolean DEFAULT_LIVE_UPLOAD_ENABLED = false;

    public static final String PREF_AUTO_INSERT_MISSION_TAKEOFF_RTL_LAND = "pref_auto_insert_mission_takeoff_rtl_land";
    public static final String PREF_WARN_ON_DRONIE_CREATION = "pref_warn_on_dronie_creation";

    public static final String PREF_TTS_PERIODIC = "tts_periodic";

    public static final String PREF_TTS_PERIODIC_BAT_VOLT = "tts_periodic_bat_volt";
    private static final boolean DEFAULT_TTS_PERIODIC_BAT_VOLT = true;

    public static final String PREF_TTS_PERIODIC_ALT = "tts_periodic_alt";
    private static final boolean DEFAULT_TTS_PERIODIC_ALT = true;

    public static final String PREF_TTS_PERIODIC_RSSI = "tts_periodic_rssi";
    private static final boolean DEFAULT_TTS_PERIODIC_RRSI = true;

    public static final String PREF_TTS_PERIODIC_AIRSPEED = "tts_periodic_airspeed";
    private static final boolean DEFAULT_TTS_PERIODIC_AIRSPEED = true;

    public static final String PREF_TOWER_WIDGETS = "pref_tower_widgets";

    public static final String ACTION_PREF_RETURN_TO_ME_UPDATED = Utils.PACKAGE_NAME + ".action.PREF_RETURN_TO_ME_UPDATED";
    public static final String PREF_RETURN_TO_ME = "pref_enable_return_to_me";
    public static final boolean DEFAULT_RETURN_TO_ME = false;

    public static final String PREF_VEHICLE_HOME_UPDATE_WARNING = "pref_vehicle_home_update_warning";
    public static final boolean DEFAULT_VEHICLE_HOME_UPDATE_WARNING = true;

    // Public for legacy usage
    public final SharedPreferences prefs;
    private final LocalBroadcastManager lbm;

    public DroidPlannerPrefs(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        lbm = LocalBroadcastManager.getInstance(context);
    }

    public boolean isLiveUploadEnabled() {
        // FIXME: Disabling live upload as it often causes the app to freeze on
        // disconnect.
        // return
        // prefs.getBoolean(PREF_LIVE_UPLOAD_ENABLED, DEFAULT_LIVE_UPLOAD_ENABLED);
        return false;
    }

    public String getDroneshareLogin() {
        return prefs.getString(PREF_DSHARE_USERNAME, DEFAULT_DSHARE_USERNAME).trim();
    }

    public void setDroneshareLogin(String b) {
        prefs.edit().putString(PREF_DSHARE_USERNAME, b.trim()).apply();
    }

    public String getDroneshareEmail() {
        return prefs.getString("dshare_email", "").trim();
    }

    public void setDroneshareEmail(String b) {
        prefs.edit().putString("dshare_email", b.trim()).apply();
    }

    public String getDronesharePassword() {
        return prefs.getString(PREF_DSHARE_PASSWORD, DEFAULT_DSHARE_PASSWORD).trim();
    }

    public void setDronesharePassword(String b) {
        prefs.edit().putString(PREF_DSHARE_PASSWORD, b.trim()).apply();
    }

    public boolean isDroneshareEnabled() {
        return !TextUtils.isEmpty(getDroneshareLogin()) && !TextUtils.isEmpty(getDronesharePassword());
    }

    public String getDroneshareApiKey() {
        return "2d38fb2e.72afe7b3761d5ee6346c178fdd6b680f";
    }

    /**
     * How many times has this application been started? (will increment for
     * each call)
     */
    public int getNumberOfRuns() {
        int r = prefs.getInt("num_runs", 0) + 1;

        prefs.edit().putInt("num_runs", r).apply();

        return r;
    }

    /**
     * @return true if google analytics reporting is enabled.
     */
    public boolean isUsageStatisticsEnabled() {
        return prefs.getBoolean(PREF_USAGE_STATISTICS, DEFAULT_USAGE_STATISTICS);
    }

    public void setConnectionParameterType(int connectionType) {
        prefs.edit().putString(PREF_CONNECTION_TYPE, String.valueOf(connectionType)).apply();
    }

    /**
     * @return the selected mavlink connection type.
     */
    public int getConnectionParameterType() {
        return Integer.parseInt(prefs.getString(PREF_CONNECTION_TYPE, DEFAULT_CONNECTION_TYPE));
    }

    public int getUnitSystemType() {
        String unitSystem = prefs.getString(PREF_UNIT_SYSTEM, null);
        if (unitSystem == null)
            return DEFAULT_UNIT_SYSTEM;

        return Integer.parseInt(unitSystem);
    }

    public void setUsbBaudRate(int baudRate) {
        prefs.edit().putString(PREF_USB_BAUD_RATE, String.valueOf(baudRate)).apply();
    }

    public int getUsbBaudRate() {
        return Integer.parseInt(prefs.getString(PREF_USB_BAUD_RATE, DEFAULT_USB_BAUD_RATE));
    }

    public void setTcpServerIp(String serverIp) {
        prefs.edit().putString(PREF_TCP_SERVER_IP, serverIp).apply();
    }

    public String getTcpServerIp() {
        return prefs.getString(PREF_TCP_SERVER_IP, DEFAULT_TCP_SERVER_IP);
    }

    public void setTcpServerPort(int serverPort) {
        prefs.edit().putString(PREF_TCP_SERVER_PORT, String.valueOf(serverPort)).apply();
    }

    public int getTcpServerPort() {
        return Integer.parseInt(prefs.getString(PREF_TCP_SERVER_PORT, DEFAULT_TCP_SERVER_PORT));
    }

    public void setUdpServerPort(int serverPort) {
        prefs.edit().putString(PREF_UDP_SERVER_PORT, String.valueOf(serverPort)).apply();
    }

    public int getUdpServerPort() {
        return Integer.parseInt(prefs.getString(PREF_UDP_SERVER_PORT, DEFAULT_UDP_SERVER_PORT));
    }

    public boolean isUdpPingEnabled() {
        return prefs.getBoolean(PREF_ENABLE_UDP_PING, DEFAULT_ENABLE_UDP_PING);
    }

    public String getUdpPingReceiverIp() {
        return prefs.getString(PREF_UDP_PING_RECEIVER_IP, null);
    }

    public int getUdpPingReceiverPort() {
        return Integer.parseInt(prefs.getString(PREF_UDP_PING_RECEIVER_PORT, DEFAULT_UDP_SERVER_PORT));
    }

    public String getBluetoothDeviceName() {
        return prefs.getString(PREF_BT_DEVICE_NAME, null);
    }

    public void setBluetoothDeviceName(String deviceName) {
        prefs.edit().putString(PREF_BT_DEVICE_NAME, deviceName).apply();
    }

    public String getBluetoothDeviceAddress() {
        return prefs.getString(PREF_BT_DEVICE_ADDRESS, null);
    }

    public void setBluetoothDeviceAddress(String newAddress) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_BT_DEVICE_ADDRESS, newAddress)
                .apply();
    }

    /**
     * @return true if the device screen should stay on.
     */
    public boolean keepScreenOn() {
        return prefs.getBoolean(PREF_KEEP_SCREEN_ON, DEFAULT_KEEP_SCREEN_ON);
    }

    /**
     * @return the target for the map auto panning.
     */
    public AutoPanMode getAutoPanMode() {
        final String defaultAutoPanModeName = DEFAULT_AUTO_PAN_MODE.name();
        final String autoPanTypeString = prefs.getString(AutoPanMode.PREF_KEY,
                defaultAutoPanModeName);
        try {
            return AutoPanMode.valueOf(autoPanTypeString);
        } catch (IllegalArgumentException e) {
            return DEFAULT_AUTO_PAN_MODE;
        }
    }

    /**
     * Updates the map auto panning target.
     *
     * @param target
     */
    public void setAutoPanMode(AutoPanMode target) {
        prefs.edit().putString(AutoPanMode.PREF_KEY, target.name()).apply();
    }

    /**
     * Use HDOP instead of satellite count on infobar
     */
    public boolean shouldGpsHdopBeDisplayed() {
        return prefs.getBoolean(PREF_SHOW_GPS_HDOP, DEFAULT_SHOW_GPS_HDOP);
    }

    public boolean isEnglishDefaultLanguage() {
        return prefs.getBoolean(PREF_UI_LANGUAGE, DEFAULT_PREF_UI_LANGUAGE);
    }

    public boolean isRealtimeFootprintsEnabled() {
        return prefs.getBoolean(PREF_UI_REALTIME_FOOTPRINTS, DEFAULT_UI_REALTIME_FOOTPRINTS);
    }

    public String getMapProviderName() {
        return prefs.getString(PREF_MAPS_PROVIDERS, DEFAULT_MAPS_PROVIDER);
    }

    /**
     * Returns the map provider selected by the user.
     *
     * @return selected map provider
     */
    public DPMapProvider getMapProvider() {
        final String mapProviderName = getMapProviderName();

        return mapProviderName == null ? DPMapProvider.DEFAULT_MAP_PROVIDER : DPMapProvider.getMapProvider
                (mapProviderName);
    }

    public Map<String, Boolean> getPeriodicSpeechPrefs() {
        final Map<String, Boolean> speechPrefs = new HashMap<>();
        speechPrefs.put(PREF_TTS_PERIODIC_BAT_VOLT,
                prefs.getBoolean(PREF_TTS_PERIODIC_BAT_VOLT, DEFAULT_TTS_PERIODIC_BAT_VOLT));
        speechPrefs.put(PREF_TTS_PERIODIC_ALT,
                prefs.getBoolean(PREF_TTS_PERIODIC_ALT, DEFAULT_TTS_PERIODIC_ALT));
        speechPrefs.put(PREF_TTS_PERIODIC_AIRSPEED,
                prefs.getBoolean(PREF_TTS_PERIODIC_AIRSPEED, DEFAULT_TTS_PERIODIC_AIRSPEED));
        speechPrefs.put(PREF_TTS_PERIODIC_RSSI,
                prefs.getBoolean(PREF_TTS_PERIODIC_RSSI, DEFAULT_TTS_PERIODIC_RRSI));

        return speechPrefs;
    }

    public int getSpokenStatusInterval() {
        return Integer.parseInt(prefs.getString(PREF_SPEECH_PERIOD, DEFAULT_SPEECH_PERIOD));
    }

    public boolean hasExceededMaxAltitude(double currentAltInMeters) {
        final boolean isWarningEnabled = prefs.getBoolean(PREF_MAX_ALT_WARNING, DEFAULT_MAX_ALT_WARNING);
        if (!isWarningEnabled)
            return false;

        final double maxAltitude = getMaxAltitude();
        return currentAltInMeters > maxAltitude;
    }

    public boolean getWarningOnLostOrRestoredSignal() {
        return prefs.getBoolean(PREF_TTS_LOST_SIGNAL, DEFAULT_TTS_WARNING_LOST_SIGNAL);
    }

    public boolean getWarningOnLowSignalStrength() {
        return prefs.getBoolean(PREF_TTS_LOW_SIGNAL, DEFAULT_TTS_WARNING_LOW_SIGNAL);
    }

    public boolean getWarningOnAutopilotWarning() {
        return prefs.getBoolean(PREF_TTS_AUTOPILOT_WARNING, DEFAULT_TTS_WARNING_AUTOPILOT_WARNING);
    }

    public boolean getImminentGroundCollisionWarning() {
        return prefs.getBoolean(PREF_WARNING_GROUND_COLLISION, DEFAULT_WARNING_GROUND_COLLISION);
    }

    public boolean isMapRotationEnabled() {
        return prefs.getBoolean(PREF_ENABLE_MAP_ROTATION, DEFAULT_ENABLE_MAP_ROTATION);
    }

    public boolean isKillSwitchEnabled() {
        return prefs.getBoolean(PREF_ENABLE_KILL_SWITCH, DEFAULT_ENABLE_KILL_SWITCH);
    }

    /**
     * @return the max altitude in meters
     */
    public double getMaxAltitude() {
        return getAltitudePreference(PREF_ALT_MAX_VALUE, DEFAULT_MAX_ALT);
    }


    /**
     * @return the min altitude in meters
     */
    public double getMinAltitude() {
        return getAltitudePreference(PREF_ALT_MIN_VALUE, DEFAULT_MIN_ALT);
    }

    /**
     * @return the default starting altitude in meters
     */
    public double getDefaultAltitude() {
        return getAltitudePreference(PREF_ALT_DEFAULT_VALUE, DEFAULT_ALT);
    }

    public void setAltitudePreference(String prefKey, double altitude) {
        prefs.edit().putString(prefKey, String.valueOf(altitude)).apply();
    }

    private double getAltitudePreference(String prefKey, double defaultValue) {
        final String maxAltValue = prefs.getString(prefKey, null);
        if (TextUtils.isEmpty(maxAltValue))
            return defaultValue;

        try {
            final double maxAlt = Double.parseDouble(maxAltValue);
            return maxAlt;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public boolean isTtsEnabled() {
        return prefs.getBoolean(PREF_IS_TTS_ENABLED, DEFAULT_TTS_ENABLED);
    }

    public boolean isWidgetEnabled(TowerWidgets widget) {
        return prefs.getBoolean(widget.getPrefKey(), widget.isEnabledByDefault());
    }

    public boolean isReturnToMeEnabled() {
        return prefs.getBoolean(PREF_RETURN_TO_ME, DEFAULT_RETURN_TO_ME);
    }

    public void enableReturnToMe(boolean isEnabled) {
        prefs.edit().putBoolean(PREF_RETURN_TO_ME, isEnabled).apply();
        lbm.sendBroadcast(new Intent(ACTION_PREF_RETURN_TO_ME_UPDATED).putExtra(PREF_RETURN_TO_ME, isEnabled));
    }

    public boolean getWarningOnVehicleHomeUpdate(){
        return prefs.getBoolean(PREF_VEHICLE_HOME_UPDATE_WARNING, DEFAULT_VEHICLE_HOME_UPDATE_WARNING);
    }
}
