package org.droidplanner.android.utils;

import java.util.Locale;

import org.droidplanner.R;
import org.droidplanner.android.communication.connection.BluetoothConnection;
import org.droidplanner.android.communication.connection.MAVLinkConnection;
import org.droidplanner.android.communication.connection.TcpConnection;
import org.droidplanner.android.communication.connection.UdpConnection;
import org.droidplanner.android.communication.connection.UsbConnection;
import org.droidplanner.android.maps.providers.DPMapProvider;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;

/**
 * Contains application related functions.
 * 
 * @author fhuya
 * @since 1.2.0
 */
public class Utils {

	/**
	 * This enum represents the different connection types to access the mavlink
	 * data.
	 * 
	 * @since 1.2.0
	 */
	public enum ConnectionType {

		BLUETOOTH {
			@Override
			public MAVLinkConnection getConnection(Context context) {
				return new BluetoothConnection(context);
			}
		},
		UDP {
			@Override
			public MAVLinkConnection getConnection(Context context) {
				return new UdpConnection(context);
			}
		},
		USB {
			@Override
			public MAVLinkConnection getConnection(Context context) {
				return new UsbConnection(context);
			}
		},
		TCP {
			@Override
			public MAVLinkConnection getConnection(Context context) {
				return new TcpConnection(context);
			}
		};

		/**
		 * This returns the implementation of MAVLinkConnection for this
		 * connection type.
		 * 
		 * @param context
		 *            application context
		 * @return mavlink connection
		 * @since 1.2.0
		 */
		public abstract MAVLinkConnection getConnection(Context context);
	}

    /**
     * Returns the map provider selected by the user.
     * @param context application context
     * @return selected map provider
     */
    public static DPMapProvider getMapProvider(Context context){
        final String mapProviderName = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_maps_providers_key), null);

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
		final boolean isUiLanguageEnglish = PreferenceManager
				.getDefaultSharedPreferences(context).getBoolean(
						Constants.PREF_UI_LANGUAGE,
						Constants.DEFAULT_PREF_UI_LANGUAGE);

		if (isUiLanguageEnglish) {
			Configuration config = new Configuration();
			config.locale = Locale.ENGLISH;

			final Resources res = context.getResources();
			res.updateConfiguration(config, res.getDisplayMetrics());
		}
	}
}
