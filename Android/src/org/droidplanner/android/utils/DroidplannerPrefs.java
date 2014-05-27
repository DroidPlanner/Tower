package org.droidplanner.android.utils;

import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Provides structured access to Droidplanner preferences
 * 
 * Over time it might be good to move the various places that are doing
 * prefs.getFoo(blah, default) here - to collect prefs in one place and avoid
 * duplicating string constants (which tend to become stale as code evolves).
 * This is called the DRY (don't repeat yourself) principle of software
 * development.
 * 
 * @author kevinh
 * 
 */
public class DroidplannerPrefs {

	// Public for legacy usage
	public SharedPreferences prefs;

	public DroidplannerPrefs(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public boolean getLogEnabled() {
		return prefs.getBoolean("pref_mavlink_log_enabled", false);
	}

	public boolean getLiveUploadEnabled() {
		return prefs.getBoolean("pref_live_upload_enabled", false);
	}

	public String getDroneshareLogin() {
		return prefs.getString("dshare_username", "").trim();
	}

	public String getDronesharePassword() {
		return prefs.getString("dshare_password", "").trim();
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

			prefs.edit().putString("vehicle_id", r).commit();
		}
		return r;
	}
}
