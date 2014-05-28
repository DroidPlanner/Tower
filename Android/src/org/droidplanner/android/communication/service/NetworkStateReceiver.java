package org.droidplanner.android.communication.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.IntentFilter;

/**
 * When we see a network connection arrive, try to restart the upload
 */
public class NetworkStateReceiver extends BroadcastReceiver {

	private static NetworkStateReceiver registered = null;

	public void onReceive(Context context, Intent intent) {
		ConnectivityManager conn = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = conn.getActiveNetworkInfo();

		if (networkInfo != null) {
			context.startService(UploaderService.createIntent(context));
		}
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public static void register(Context context) {
		// If we are connected at startup, go ahead and scan
		if (isNetworkAvailable(context))
			context.startService(UploaderService.createIntent(context));

		if (registered == null) {
			IntentFilter filter = new IntentFilter(
					ConnectivityManager.CONNECTIVITY_ACTION);

			registered = new NetworkStateReceiver();
			context.registerReceiver(registered, filter);
		}
	}

	public static void unregister(Context context) {
		if (registered != null) {
			try {
				context.unregisterReceiver(registered);
				registered = null;
			} catch (Exception ex) {
				// Seems to be an android bug - this happens sometimes
				// BugSenseHandler.sendExceptionMessage("unregister",
				// "exception", ex)
			}
		}
	}
}