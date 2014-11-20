package org.droidplanner.android.communication.connection;

import java.io.File;
import java.io.IOException;

import org.droidplanner.android.communication.service.UploaderService;
import org.droidplanner.android.utils.AndroidLogger;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.file.FileStream;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.core.MAVLink.connection.MavLinkConnection;
import org.droidplanner.core.model.Logger;

import android.content.Context;
import android.content.SharedPreferences;

import com.MAVLink.MAVLinkPacket;

public abstract class AndroidMavLinkConnection extends MavLinkConnection {

	private static final String TAG = AndroidMavLinkConnection.class.getSimpleName();

	protected abstract void closeAndroidConnection() throws IOException;

	protected abstract void loadPreferences(SharedPreferences prefs);

	protected abstract void openAndroidConnection() throws IOException;

	protected final Context mContext;
	protected final DroidPlannerPrefs prefs;

	private DroneshareClient uploader;

	public AndroidMavLinkConnection(Context applicationContext) {
		this.mContext = applicationContext;
		prefs = new DroidPlannerPrefs(applicationContext);
	}

	@Override
	protected final void loadPreferences() {
		loadPreferences(prefs.prefs);
	}

	@Override
	protected final Logger initLogger() {
		return AndroidLogger.getLogger();
	}

	@Override
	protected final void onLogSaved(MAVLinkPacket packet) throws IOException {
		if (uploader != null)
			uploader.filterMavlink(uploader.interfaceNum, packet.encodePacket());
	}

	@Override
	protected final void openConnection() throws IOException {
		openAndroidConnection();

		// Start a new ga analytics session. The new session will be tagged
		// with the mavlink connection mechanism, as well as whether the user
		// has an active droneshare account.
		GAUtils.startNewSession(mContext);

		String login = prefs.getDroneshareLogin();
		String password = prefs.getDronesharePassword();
		if (prefs.getLiveUploadEnabled() && !login.isEmpty() && !password.isEmpty()) {
			mLogger.logInfo(TAG, "Starting live upload");
			uploader = new DroneshareClient();
			uploader.connect(login, password);
		} else {
			mLogger.logWarning(TAG, "Skipping live upload");
		}
	}

	@Override
	protected final void closeConnection() throws IOException {
		try {
			closeAndroidConnection();
		} finally {
			// See if we can at least do a delayed upload
			mContext.startService(UploaderService.createIntent(mContext));

			if (uploader != null)
				uploader.close();
		}
	}

	@Override
	protected final File getTempTLogFile() {
		return FileStream.getTLogFile();
	}

	@Override
	protected final void commitTempTLogFile(File tlogFile) {
		FileStream.commitFile(tlogFile);
	}
}
