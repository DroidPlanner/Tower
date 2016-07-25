package org.droidplanner.android.droneshare;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import timber.log.Timber;

/**
 * When the device's network connectivity is restored, check and see if there's
 * anymore data to upload.
 */
public class NetworkConnectivityReceiver extends BroadcastReceiver {

	private static final String TAG = NetworkConnectivityReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
			final boolean noConnectivity = intent.getBooleanExtra(
					ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

			if (noConnectivity) {
				// No connectivity. Keep the receiver enabled to listen for
				// possible connectivity changes in the future.
				return;
			}

			// There is connectivity! Restart the droneshare uploader service,
			// and disable this connectivity receiver.
			UploaderService.kickStart(context);

			Timber.d("Disabling connectivity receiver.");
			enableConnectivityReceiver(context, false);
		}
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	/**
	 * Toggles the connectivity listener component of the app.
	 * 
	 * @param context
	 *            application context
	 * @param enable
	 *            true to enable
	 */
	public static void enableConnectivityReceiver(Context context, boolean enable) {
		final ComponentName receiver = new ComponentName(context, NetworkConnectivityReceiver.class);
		final int newState = enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
				: PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

		context.getPackageManager().setComponentEnabledSetting(receiver, newState,
				PackageManager.DONT_KILL_APP);
	}
}