package org.droidplanner.android.droneshare;

import android.util.Log;

import com.geeksville.apiproxy.GCSHookImpl;

import java.io.IOException;
import java.util.UUID;

/**
 * The Droidplanner specific bindings for the drone api FIXME - need to auto
 * reconnect as needed like in the posixpilot version
 * 
 */
public class DroneshareClient extends GCSHookImpl {

	private static final String TAG = DroneshareClient.class.getSimpleName();

	public int interfaceNum = 0;

	public void connect(String login, String password) {
		try {
			super.connect();

			// Create user if necessary/possible
			if (isUsernameAvailable(login))
				createUser(login, password, null);
			else
				loginUser(login, password);

			int sysId = 1;
			setVehicleId("550e8400-e29b-41d4-a716-446655440000", interfaceNum, sysId, false);

			startMission(false, UUID.randomUUID());
		} catch (Exception ex) {
			Log.e(TAG, "Failed to connect due to " + ex);
		}
	}

	@Override
	public void close() throws IOException {
		stopMission(true);

		flush();
		super.close();
	}

}
