package org.droidplanner.android.utils;

import java.util.Locale;

import org.droidplanner.android.communication.connection.AndroidMavLinkConnection;
import org.droidplanner.android.communication.connection.AndroidTcpConnection;
import org.droidplanner.android.communication.connection.AndroidUdpConnection;
import org.droidplanner.android.communication.connection.BluetoothConnection;
import org.droidplanner.android.communication.connection.usb.UsbConnection;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.core.MAVLink.connection.MavLinkConnectionTypes;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

/**
 * Contains application related functions.
 */
public class Utils {

	/**
	 * This enum represents the different connection types to access the mavlink
	 * data.
	 */
	public enum ConnectionType {

		BLUETOOTH(MavLinkConnectionTypes.MAVLINK_CONNECTION_BLUETOOTH) {
			@Override
			public BluetoothConnection getConnection(Context context) {
				return new BluetoothConnection(context);
			}
		},
		UDP(MavLinkConnectionTypes.MAVLINK_CONNECTION_UDP) {
			@Override
			public AndroidUdpConnection getConnection(Context context) {
				return new AndroidUdpConnection(context);
			}
		},
		USB(MavLinkConnectionTypes.MAVLINK_CONNECTION_USB) {
			@Override
			public UsbConnection getConnection(Context context) {
				return new UsbConnection(context);
			}
		},
		TCP(MavLinkConnectionTypes.MAVLINK_CONNECTION_TCP) {
			@Override
			public AndroidTcpConnection getConnection(Context context) {
				return new AndroidTcpConnection(context);
			}
		};

		private final int mMavLinkConnectionType;

		private ConnectionType(int mavLinkConnectionType) {
			mMavLinkConnectionType = mavLinkConnectionType;
		}

		/**
		 * This returns the implementation of AndroidMavLinkConnection for this
		 * connection type.
		 * 
		 * @param context
		 *            application context
		 * @return mavlink connection
		 */
		public abstract AndroidMavLinkConnection getConnection(Context context);

		public int getConnectionType() {
			return mMavLinkConnectionType;
		}
	}

	/**
	 * Returns the map provider selected by the user.
	 * 
	 * @param context
	 *            application context
	 * @return selected map provider
	 */
	public static DPMapProvider getMapProvider(Context context) {
		DroidPlannerPrefs prefs = new DroidPlannerPrefs(context);
		final String mapProviderName = prefs.getMapProviderName();

		return mapProviderName == null ? DPMapProvider.DEFAULT_MAP_PROVIDER : DPMapProvider
				.getMapProvider(mapProviderName);
	}

	/**
	 * Used to update the user interface language.
	 * 
	 * @param context
	 *            Application context
	 */
	public static void updateUILanguage(Context context) {
		DroidPlannerPrefs prefs = new DroidPlannerPrefs(context);
		if (prefs.isEnglishDefaultLanguage()) {
			Configuration config = new Configuration();
			config.locale = Locale.ENGLISH;

			final Resources res = context.getResources();
			res.updateConfiguration(config, res.getDisplayMetrics());
		}
	}
}
