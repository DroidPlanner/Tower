package org.droidplanner.android.utils.prefs;

import java.util.UUID;

import org.droidplanner.android.R;
import org.droidplanner.android.utils.unit.systems.UnitSystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.SparseBooleanArray;

import com.o3dr.services.android.lib.drone.connection.ConnectionType;

/**
 * Provides structured access to Droidplanner preferences
 *
 * Over time it might be good to move the various places that are doing
 * prefs.getFoo(blah, default) here - to collect prefs in one place and avoid
 * duplicating string constants (which tend to become stale as code evolves).
 * This is called the DRY (don't repeat yourself) principle of software
 * development.
 *
 *
 */
public class DroidPlannerPrefs {

	/*
	 * Default preference value
	 */
	public static final boolean DEFAULT_USAGE_STATISTICS = true;
	public static final String DEFAULT_CONNECTION_TYPE = String.valueOf(ConnectionType.TYPE_USB);
	private static final boolean DEFAULT_KEEP_SCREEN_ON = false;
	private static final boolean DEFAULT_MAX_VOLUME_ON_START = false;
	private static final boolean DEFAULT_PERMANENT_NOTIFICATION = true;
	private static final boolean DEFAULT_OFFLINE_MAP_ENABLED = false;
	private static final String DEFAULT_MAP_TYPE = "";
	private static final AutoPanMode DEFAULT_AUTO_PAN_MODE = AutoPanMode.DISABLED;
	public static final boolean DEFAULT_PREF_UI_LANGUAGE = false;
	public static final String DEFAULT_SPEECH_PERIOD = "0";
	public static final boolean DEFAULT_TTS_CEILING_EXCEEDED = true;
	public static final boolean DEFAULT_TTS_WARNING_LOST_SIGNAL = true;
	public static final boolean DEFAULT_TTS_WARNING_LOW_SIGNAL = false;
	public static final boolean DEFAULT_TTS_WARNING_AUTOPILOT_WARNING = true;
    private static final String DEFAULT_USB_BAUD_RATE = "57600";
    private static final String DEFAULT_TCP_SERVER_IP = "192.168.40.100";
    private static final String DEFAULT_TCP_SERVER_PORT = "5763";
    private static final String DEFAULT_UDP_SERVER_PORT = "14550";
    private static final int DEFAULT_UNIT_SYSTEM = UnitSystem.AUTO;
    private static final boolean DEFAULT_WARNING_GROUND_COLLISION = false;
    private static final boolean DEFAULT_ENABLE_MAP_ROTATION = true;
    private static final boolean DEFAULT_ENABLE_KILL_SWITCH = false;
    private static final boolean DEFAULT_ENABLE_UDP_PING = false;

    // Public for legacy usage
	public SharedPreferences prefs;
	private Context context;

	public DroidPlannerPrefs(Context context) {
		this.context = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public boolean isLiveUploadEnabled() {
		// FIXME: Disabling live upload as it often causes the app to freeze on
		// disconnect.
		// return
		// prefs.getBoolean(context.getString(R.string.pref_live_upload_enabled_key),
		// false);
		return false;
	}

	public String getDroneshareLogin() {
		return prefs.getString(context.getString(R.string.pref_dshare_username_key), "").trim();
	}

	public void setDroneshareLogin(String b) {
		prefs.edit().putString(context.getString(R.string.pref_dshare_username_key), b.trim()).apply();
	}

	public String getDroneshareEmail() {
		return prefs.getString("dshare_email", "").trim();
	}

	public void setDroneshareEmail(String b) {
		prefs.edit().putString("dshare_email", b.trim()).apply();
	}

	public String getDronesharePassword() {
		return prefs.getString(context.getString(R.string.pref_dshare_password_key), "").trim();
	}

	public void setDronesharePassword(String b) {
		prefs.edit().putString(context.getString(R.string.pref_dshare_password_key), b.trim()).apply();
	}

	public boolean isDroneshareEnabled() {
        return !TextUtils.isEmpty(getDroneshareLogin()) && !TextUtils.isEmpty(getDronesharePassword());
	}

    public String getDroneshareApiKey(){
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
	 * Return a unique ID for the vehicle controlled by this tablet. FIXME,
	 * someday let the users select multiple vehicles
	 */
	public String getVehicleId() {
		String r = prefs.getString("vehicle_id", "").trim();

		// No ID yet - pick one
		if (r.isEmpty()) {
			r = UUID.randomUUID().toString();

			prefs.edit().putString("vehicle_id", r).apply();
		}
		return r;
	}

	/**
	 * @return true if google analytics reporting is enabled.
	 */
	public boolean isUsageStatisticsEnabled() {
		return prefs.getBoolean(context.getString(R.string.pref_usage_statistics_key),
				DEFAULT_USAGE_STATISTICS);
	}

    public void setConnectionParameterType(int connectionType){
        prefs.edit().putString(context.getString(R.string.pref_connection_type_key),
                String.valueOf(connectionType)).apply();
    }

    /**
     * @return the selected mavlink connection type.
     */
    public int getConnectionParameterType(){
        return Integer.parseInt(prefs.getString(context.getString(R.string
                .pref_connection_type_key), DEFAULT_CONNECTION_TYPE));
    }

    public int getUnitSystemType() {
        String unitSystem = prefs.getString(context.getString(R.string.pref_unit_system_key), null);
        if(unitSystem == null)
            return DEFAULT_UNIT_SYSTEM;

        return Integer.parseInt(unitSystem);
    }

    public void setUsbBaudRate(int baudRate){
        prefs.edit().putString(context.getString(R.string.pref_baud_type_key),
                String.valueOf(baudRate)).apply();
    }

    public int getUsbBaudRate(){
        return Integer.parseInt(prefs.getString(context.getString(R.string.pref_baud_type_key),
                DEFAULT_USB_BAUD_RATE));
    }

    public void setTcpServerIp(String serverIp){
        prefs.edit().putString(context.getString(R.string.pref_server_ip_key), serverIp).apply();
    }

    public String getTcpServerIp(){
        return prefs.getString(context.getString(R.string.pref_server_ip_key),
                DEFAULT_TCP_SERVER_IP);
    }

    public void setTcpServerPort(int serverPort){
        prefs.edit().putString(context.getString(R.string.pref_server_port_key),
                String.valueOf(serverPort)).apply();
    }

    public int getTcpServerPort(){
        return Integer.parseInt(prefs.getString(context.getString(R.string.pref_server_port_key),
                DEFAULT_TCP_SERVER_PORT));
    }

    public void setUdpServerPort(int serverPort){
        prefs.edit().putString(context.getString(R.string.pref_udp_server_port_key),
                String.valueOf(serverPort)).apply();
    }

    public int getUdpServerPort(){
        return Integer.parseInt(prefs.getString(context.getString(R.string
                        .pref_udp_server_port_key), DEFAULT_UDP_SERVER_PORT));
    }

    public boolean isUdpPingEnabled(){
        return prefs.getBoolean(context.getString(R.string.pref_enable_udp_server_ping_key), DEFAULT_ENABLE_UDP_PING);
    }

    public String getUdpPingReceiverIp(){
        return prefs.getString(context.getString(R.string.pref_udp_ping_receiver_ip_key), null);
    }

    public int getUdpPingReceiverPort(){
        return Integer.parseInt(prefs.getString(context.getString(R.string.pref_udp_ping_receiver_port_key),
                DEFAULT_UDP_SERVER_PORT));
    }

    public String getBluetoothDeviceName(){
        return prefs.getString(context.getString(R.string.pref_bluetooth_device_name_key), null);
    }

    public void setBluetoothDeviceName(String deviceName){
        prefs.edit().putString(context.getString(R.string.pref_bluetooth_device_name_key), deviceName).apply();
    }

    public String getBluetoothDeviceAddress() {
        return prefs.getString(context.getString(R.string.pref_bluetooth_device_address_key), null);
    }

    public void setBluetoothDeviceAddress(String newAddress) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(context.getString(R.string.pref_bluetooth_device_address_key), newAddress)
                .apply();
    }

	/**
	 * @return true if the device screen should stay on.
	 */
	public boolean keepScreenOn() {
		return prefs.getBoolean(context.getString(R.string.pref_keep_screen_bright_key),
				DEFAULT_KEEP_SCREEN_ON);
	}

	/**
	 * @return true if Volume should be set to 100% on app start
	 */
	public boolean maxVolumeOnStart() {
		return prefs.getBoolean(context.getString(R.string.pref_request_max_volume_key),
				DEFAULT_MAX_VOLUME_ON_START);
	}

	/**
	 * @return true if the status bar notification should be permanent when
	 *         connected.
	 */
	public boolean isNotificationPermanent() {
		return prefs.getBoolean(context.getString(R.string.pref_permanent_notification_key),
				DEFAULT_PERMANENT_NOTIFICATION);
	}

	/**
	 * @return true if offline map is enabled (if supported by the map
	 *         provider).
	 */
	public boolean isOfflineMapEnabled() {
		return prefs.getBoolean(context.getString(R.string.pref_advanced_use_offline_maps_key),
				DEFAULT_OFFLINE_MAP_ENABLED);
	}

	/**
	 * @return the selected map type (if supported by the map provider).
	 */
	public String getMapType() {
		return prefs.getString(context.getString(R.string.pref_map_type_key), DEFAULT_MAP_TYPE);
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
		return prefs.getBoolean(context.getString(R.string.pref_ui_gps_hdop_key), false);
	}

	public boolean isEnglishDefaultLanguage() {
		return prefs.getBoolean(context.getString(R.string.pref_ui_language_english_key),
				DEFAULT_PREF_UI_LANGUAGE);
	}

	public boolean isRealtimeFootprintsEnabled() {
		return prefs.getBoolean(context.getString(R.string.pref_ui_realtime_footprints_key), false);
	}

	public String getMapProviderName() {
		return prefs.getString(context.getString(R.string.pref_maps_providers_key), null);
	}

	public SparseBooleanArray getPeriodicSpeechPrefs() {
		final SparseBooleanArray speechPrefs = new SparseBooleanArray(4);
		speechPrefs.put(R.string.pref_tts_periodic_bat_volt_key,
				prefs.getBoolean(context.getString(R.string.pref_tts_periodic_bat_volt_key), true));
		speechPrefs.put(R.string.pref_tts_periodic_alt_key,
				prefs.getBoolean(context.getString(R.string.pref_tts_periodic_alt_key), true));
		speechPrefs.put(R.string.pref_tts_periodic_airspeed_key,
				prefs.getBoolean(context.getString(R.string.pref_tts_periodic_airspeed_key), true));
		speechPrefs.put(R.string.pref_tts_periodic_rssi_key,
				prefs.getBoolean(context.getString(R.string.pref_tts_periodic_rssi_key), true));

		return speechPrefs;
	}

	public int getSpokenStatusInterval() {
		return Integer.parseInt(prefs.getString(
				context.getString(R.string.pref_tts_periodic_period_key), DEFAULT_SPEECH_PERIOD));
	}

	public boolean getWarningOn400ftExceeded() {
		return prefs.getBoolean(
				context.getString(R.string.pref_tts_warning_400ft_ceiling_exceeded_key),
				DEFAULT_TTS_CEILING_EXCEEDED);
	}

	public boolean getWarningOnLostOrRestoredSignal() {
		return prefs.getBoolean(context.getString(R.string.pref_tts_warning_lost_signal_key),
				DEFAULT_TTS_WARNING_LOST_SIGNAL);
	}

	public boolean getWarningOnLowSignalStrength() {
		return prefs.getBoolean(context.getString(R.string.pref_tts_warning_low_signal_key),
				DEFAULT_TTS_WARNING_LOW_SIGNAL);
	}

	public boolean getWarningOnAutopilotWarning() {
		return prefs.getBoolean(
				context.getString(R.string.pref_tts_warning_autopilot_warnings_key),
				DEFAULT_TTS_WARNING_AUTOPILOT_WARNING);
	}

    public boolean getImminentGroundCollisionWarning(){
        return prefs.getBoolean(context.getString(R.string.pref_ground_collision_warning_key),
                DEFAULT_WARNING_GROUND_COLLISION);
    }

    public boolean isMapRotationEnabled(){
        return prefs.getBoolean(context.getString(R.string.pref_map_enable_rotation_key), DEFAULT_ENABLE_MAP_ROTATION);
    }

    public boolean isAdvancedMenuEnabled(){
        return isKillSwitchEnabled();
    }

    public boolean isKillSwitchEnabled(){
        return prefs.getBoolean(context.getString(R.string.pref_enable_kill_switch_key), DEFAULT_ENABLE_KILL_SWITCH);
    }
}
