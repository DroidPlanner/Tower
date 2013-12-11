package com.droidplanner.utils;

import android.content.Context;
import com.droidplanner.connection.BluetoothConnection;
import com.droidplanner.connection.MAVLinkConnection;
import com.droidplanner.connection.TcpConnection;
import com.droidplanner.connection.UdpConnection;
import com.droidplanner.connection.UsbConnection;

/**
 * Contains application related functions.
 *
 * @author fhuya
 * @since 1.2.0
 */
public class Utils {

    /**
     * This enum represents the different connection types to access the mavlink data.
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
         * This returns the implementation of MAVLinkConnection for this connection type.
         * @param context application context
         * @return mavlink connection
         * @since 1.2.0
         */
        public abstract MAVLinkConnection getConnection(Context context);
    }
}
