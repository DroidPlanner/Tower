package org.droidplanner.android.communication.connection;

import java.io.File;
import java.io.IOException;

import org.droidplanner.core.MAVLink.connection.UdpConnection;
import org.droidplanner.core.model.Logger;

import android.content.Context;
import android.content.SharedPreferences;

public class AndroidUdpConnection extends AndroidMavLinkConnection {

	private final UdpConnection mConnectionImpl;

	public AndroidUdpConnection(Context context) {
		super(context);

		mConnectionImpl = new UdpConnection() {
			@Override
			protected int loadServerPort() {
				return Integer.parseInt(prefs.prefs.getString("pref_udp_server_port", "14550"));
			}

			@Override
			protected Logger initLogger() {
				return AndroidUdpConnection.this.initLogger();
			}

			@Override
			protected File getTempTLogFile() {
				return AndroidUdpConnection.this.getTempTLogFile();
			}

			@Override
			protected void commitTempTLogFile(File tlogFile) {
				AndroidUdpConnection.this.commitTempTLogFile(tlogFile);
			}
		};
	}

	@Override
	protected void closeAndroidConnection() throws IOException {
		mConnectionImpl.closeConnection();
	}

	@Override
	protected void loadPreferences(SharedPreferences prefs) {
		mConnectionImpl.loadPreferences();
	}

	@Override
	protected void openAndroidConnection() throws IOException {
		mConnectionImpl.openConnection();
	}

	@Override
	protected int readDataBlock(byte[] buffer) throws IOException {
		return mConnectionImpl.readDataBlock(buffer);
	}

	@Override
	protected void sendBuffer(byte[] buffer) throws IOException {
		mConnectionImpl.sendBuffer(buffer);
	}

	@Override
	public int getConnectionType() {
		return mConnectionImpl.getConnectionType();
	}
}
