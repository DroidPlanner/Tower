package org.droidplanner.android.utils;

import org.droidplanner.core.model.Logger;

import android.util.Log;

/**
 * Android specific implementation for the {org.droidplanner.core.model.Logger}
 * interface.
 */
public class AndroidLogger implements Logger {
	private static Logger sLogger = new AndroidLogger();

	public static Logger getLogger() {
		return sLogger;
	}

	// Only one instance is allowed.
	private AndroidLogger() {
	}

	@Override
	public void logVerbose(String logTag, String verbose) {
		if (verbose != null) {
			Log.v(logTag, verbose);
		}
	}

	@Override
	public void logDebug(String logTag, String debug) {
		if (debug != null) {
			Log.d(logTag, debug);
		}
	}

	@Override
	public void logInfo(String logTag, String info) {
		if (info != null) {
			Log.i(logTag, info);
		}
	}

	@Override
	public void logWarning(String logTag, String warning) {
		if (warning != null) {
			Log.w(logTag, warning);
		}
	}

	@Override
	public void logWarning(String logTag, Exception exception) {
		if (exception != null) {
			Log.w(logTag, exception);
		}
	}

	@Override
	public void logWarning(String logTag, String warning, Exception exception) {
		if (warning != null && exception != null) {
			Log.w(logTag, warning, exception);
		}
	}

	@Override
	public void logErr(String logTag, String err) {
		if (err != null) {
			Log.e(logTag, err);
		}
	}

	@Override
	public void logErr(String logTag, Exception exception) {
		if (exception != null) {
			Log.e(logTag, exception.getMessage(), exception);
		}
	}

	@Override
	public void logErr(String logTag, String err, Exception exception) {
		if (err != null && exception != null) {
			Log.e(logTag, err, exception);
		}
	}
}
